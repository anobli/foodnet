#!/usr/bin/env python3
"""
One-time migration for the QRistal "Foods" Firestore collection.

Background
----------
Each food item's Firestore document used to be keyed by its own id alone:

    Foods/{id}

Two different users independently rolling the same random id would then
collide on the exact same document, and the second person's write would
silently overwrite the first person's item (Firestore .set() fully replaces
a document; there was no error, no conflict, just lost data).

The Android app has been changed to key documents by "{group}_{id}" instead,
which makes that cross-user collision structurally impossible. This script
migrates existing documents (written under the old "{id}" scheme) to the new
"{group}_{id}" scheme.

Reads elsewhere in the app already work by *querying* on the Id/Group fields
rather than looking up a document by path, so nothing is broken in the
meantime -- this script just cleans up the document keys so future edits
don't leave duplicate orphans behind (see the app-side commit message for
details).

Safety
------
- Defaults to a dry run: prints every planned action, writes nothing.
- Pass --live to actually perform the migration.
- In --live mode, each document is migrated individually and in a safe
  order: write the new document, read it back to confirm the data matches,
  and only THEN delete the old document. If anything looks wrong, that one
  document is skipped (logged as an error) and the old document is left
  alone -- nothing is ever deleted before a verified copy exists elsewhere.
- Idempotent: safe to re-run. Documents already at the "{group}_{id}" path,
  or missing the fields needed to compute it, are skipped and reported
  separately rather than touched.
- In --live mode you must additionally type "MIGRATE" at a confirmation
  prompt before anything is written.

Usage
-----
    pip install firebase-admin

    # 1. Download a service account key for the foodnet-294621 project:
    #    Firebase Console -> Project settings -> Service accounts
    #    -> Generate new private key
    #    Keep this file private -- it grants full read/write access to the
    #    whole project. Do not commit it, do not share it, delete it (or at
    #    least revoke it in the console) once you're done with this script.

    # 2. Dry run first -- always do this first, read the output carefully.
    python3 migrate_firestore_doc_ids.py --credentials /path/to/key.json

    # 3. If the dry run output looks right, run it for real.
    python3 migrate_firestore_doc_ids.py --credentials /path/to/key.json --live
"""

import argparse
import sys

import firebase_admin
from firebase_admin import credentials, firestore

COLLECTION = "Foods"
FIELD_ID = "Id"
FIELD_GROUP = "Group"


def new_document_id(group, item_id):
    """Mirrors the Android app's FirestoreDBHelper.add(): group_id + "_" + id."""
    return f"{group}_{item_id}"


def plan_migration(db):
    """
    Reads every document in the Foods collection and decides what to do
    with it. Returns four lists: (to_migrate, already_migrated, skipped,
    conflicts).

    - to_migrate: documents that need to move from their old id-only path
      to the new "{group}_{id}" path.
    - already_migrated: documents already sitting at the correct path
      (e.g. re-running this script, or items added after the app fix).
    - skipped: documents missing the Id/Group fields needed to compute
      their target path; left completely untouched.
    - conflicts: two or more OLD documents that map to the SAME new path
      (only possible if the same group already had two items sharing an
      id before this migration). Migrating these blindly would silently
      overwrite one with the other -- exactly the bug this migration
      exists to fix -- so they are left untouched and reported instead,
      for you to resolve by hand (e.g. decide which one is the real item
      and delete/rename the other in the Firebase console).
    """
    to_migrate = []
    already_migrated = []
    skipped = []

    # old_doc_id -> (target_id, data), used to detect two old documents
    # that would collide on the same target_id.
    candidates = []

    for doc in db.collection(COLLECTION).stream():
        data = doc.to_dict()
        item_id = data.get(FIELD_ID)
        group = data.get(FIELD_GROUP)

        if item_id is None or group is None:
            skipped.append((doc.id, data))
            continue

        target_id = new_document_id(group, item_id)

        if doc.id == target_id:
            already_migrated.append((doc.id, data))
            continue

        candidates.append((doc.id, target_id, data))

    targets_seen = {}
    for old_id, target_id, data in candidates:
        targets_seen.setdefault(target_id, []).append((old_id, data))

    conflicts = []
    for target_id, entries in targets_seen.items():
        if len(entries) > 1:
            conflicts.append((target_id, entries))
        else:
            old_id, data = entries[0]
            to_migrate.append((old_id, target_id, data))

    return to_migrate, already_migrated, skipped, conflicts


def describe(doc_id, data):
    """Short human-readable line for logging, e.g. for a food item."""
    food = data.get("Food", "<no name>")
    group = data.get(FIELD_GROUP, "<no group>")
    item_id = data.get(FIELD_ID, "<no id>")
    return f"{doc_id}  (Food={food!r}, Group={group}, Id={item_id})"


def run_dry_run(to_migrate, already_migrated, skipped, conflicts):
    print(f"\n=== DRY RUN -- nothing will be written or deleted ===\n")

    print(f"{len(to_migrate)} document(s) would be migrated:")
    for old_id, new_id, data in to_migrate:
        print(f"  {describe(old_id, data)}")
        print(f"    -> new document id: {new_id}")

    print(f"\n{len(already_migrated)} document(s) already at the new path (no action):")
    for doc_id, data in already_migrated:
        print(f"  {describe(doc_id, data)}")

    if skipped:
        print(f"\n{len(skipped)} document(s) SKIPPED (missing Id/Group field, left untouched):")
        for doc_id, data in skipped:
            print(f"  {doc_id}: {data}")

    if conflicts:
        print(
            f"\n{len(conflicts)} CONFLICT(S) -- multiple old documents would land on the "
            f"same new path. None of these will be touched by --live; resolve by hand first "
            f"(open the Firebase console, decide which document is the real item, and "
            f"delete/rename the other):"
        )
        for target_id, entries in conflicts:
            print(f"  target {target_id}:")
            for old_id, data in entries:
                print(f"    {describe(old_id, data)}")

    print(
        f"\nSummary: {len(to_migrate)} to migrate, "
        f"{len(already_migrated)} already done, {len(skipped)} skipped, "
        f"{len(conflicts)} conflict(s) needing manual resolution.\n"
        f"Re-run with --live to perform the migration."
    )


def run_live_migration(db, to_migrate):
    print(f"\n=== LIVE RUN -- migrating {len(to_migrate)} document(s) ===\n")

    migrated = 0
    errors = []

    for old_id, new_id, data in to_migrate:
        label = describe(old_id, data)
        try:
            new_ref = db.collection(COLLECTION).document(new_id)

            # 0. Defense in depth: if something unexpected is already sitting at
            #    the target path (e.g. this script was interrupted and partially
            #    re-run, or Firestore changed under us since the plan was made),
            #    do not blindly overwrite it -- that's exactly the bug we're
            #    fixing. Only proceed if it's empty or already an exact copy.
            existing = new_ref.get()
            if existing.exists and existing.to_dict() != data:
                errors.append((old_id, new_id, "target path already has different data, refused to overwrite"))
                print(f"  ERROR {label} -> {new_id}: target already exists with different data, old document left untouched")
                continue

            # 1. Write a copy at the new path.
            new_ref.set(data)

            # 2. Read it back and confirm it matches before touching the original.
            written = new_ref.get()
            if not written.exists or written.to_dict() != data:
                errors.append((old_id, new_id, "verification read-back did not match"))
                print(f"  ERROR verifying {label} -> {new_id}: data mismatch, old document left untouched")
                continue

            # 3. Only now is it safe to remove the old document.
            db.collection(COLLECTION).document(old_id).delete()

            migrated += 1
            print(f"  OK: {label} -> {new_id}")
        except Exception as e:  # noqa: BLE001 -- one bad document shouldn't abort the whole run
            errors.append((old_id, new_id, str(e)))
            print(f"  ERROR migrating {label} -> {new_id}: {e}")

    print(f"\nDone: {migrated} migrated, {len(errors)} error(s).")
    if errors:
        print("\nDocuments that still need attention (old document was left in place):")
        for old_id, new_id, reason in errors:
            print(f"  {old_id} -> {new_id}: {reason}")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument(
        "--credentials",
        required=True,
        help="Path to the Firebase service account key JSON file.",
    )
    parser.add_argument(
        "--live",
        action="store_true",
        help="Actually perform the migration. Without this flag, only a dry run is printed.",
    )
    args = parser.parse_args()

    cred = credentials.Certificate(args.credentials)
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    to_migrate, already_migrated, skipped, conflicts = plan_migration(db)

    if not args.live:
        run_dry_run(to_migrate, already_migrated, skipped, conflicts)
        return

    if conflicts:
        print(
            f"Refusing to run --live: {len(conflicts)} conflict(s) need manual resolution "
            f"first. Run without --live to see details."
        )
        sys.exit(1)

    if not to_migrate:
        print("Nothing to migrate.")
        return

    print(f"About to migrate {len(to_migrate)} document(s) for real.")
    print("This will write new documents and delete old ones in the live database.")
    confirmation = input("Type MIGRATE to proceed: ")
    if confirmation != "MIGRATE":
        print("Aborted -- nothing was changed.")
        sys.exit(1)

    run_live_migration(db, to_migrate)


if __name__ == "__main__":
    main()
