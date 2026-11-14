# Login feedback + duplicate-flow UX — design doc

Date: 2026-11-05
Platforms: Android (phone only, native Java / ListView+ArrayAdapter / ConstraintLayout — no Compose/Fragments)

## Goal
Two small, surgical fixes to existing flows in QRistal:
1. Make a successful sign-in visibly obvious (today it's silent — only a toolbar icon swap).
2. Let a user duplicate a container to a new one without leaving them stranded on a
   stale "original item" screen that looks identical to the new one.

Both are confusion/feedback problems, not missing features — the underlying logic
already works.

---

## Part 1 — Sign-in success feedback

### Current behavior (confirmed in code)
`FoodNetListActivity.java`, `signInLauncher` callback, lines 123-149:
- On success (`RESULT_OK` and `FirebaseAuth.getInstance().getCurrentUser() != null`):
  calls `invalidateOptionsMenu()` (swaps toolbar menu icon from `connect_menu` to
  `disconnect_menu` — see `onPrepareOptionsMenu`, lines 328-341) and
  `syncLocalToFirebase(user)` (lines 236-254). **No visible confirmation.**
- On failure: shows a `Toast` with `R.string.sign_in_failed` or
  `R.string.sign_in_failed_reason` (lines 132, 144-148). This already establishes
  Toast as the app's pattern for sign-in-related feedback.

`syncLocalToFirebase()` (lines 236-254) is not cosmetic — on first login it moves
every local-only item into Firestore (`firestoreDb.add(item)` per item) and then
**deletes the local copies** (`localDb.deleteAll()`, line 248). This is a one-time,
somewhat destructive-looking migration the user has no visibility into today. If a
user had items before ever signing in, and then signs in, their data silently moves
and the local DB gets wiped — worth confirming explicitly, not just assuming "signed
in" is enough.

### Design decision: Toast, not Snackbar
Use `Toast`, matching the existing failure-path pattern in the same callback (consistency: user sees the same affordance whether sign-in succeeds or fails). A Snackbar would also work technically (Material is already a dependency), but:
- Snackbar anchors to a view and this screen's layout (`activity_food_net_list.xml`) has no `CoordinatorLayout`, so it would need extra plumbing for no real benefit here.
- This is a one-off, fire-and-forget confirmation with no action to take (no "Undo") — Toast's simpler semantics fit better. Reserve Snackbar for cases with an actionable button.
- Keeps the file's existing Toast-only convention from drifting into mixed patterns for the same feature.

### Copy
Two cases, both still single Toast calls (don't stack two toasts):

- **No local data existed to migrate** (`localData` was empty — see note on `syncLocalToFirebase` below for how to detect this cheaply):
  `"Signed in as {displayName}"` → new string `signed_in_success`, e.g.:
  `"Signed in as %s"` with `user.getDisplayName()` (fall back to `user.getEmail()` if display name is null — Google sign-in should always populate one, but guard anyway).

- **Local data existed and was migrated:**
  `"Signed in as {displayName}. Your local items were synced to the cloud."` → new string `signed_in_success_with_sync`:
  `"Signed in as %s. Your local items were synced to the cloud."`

Use `Toast.LENGTH_LONG` (matches the failure path's duration for consistency).

### Implementation notes (for the dev, not prescriptive on exact code)
- `syncLocalToFirebase` currently has no return value / callback hook to tell the
  caller whether anything was actually migrated. Cheapest change: have it return
  `boolean` (or the migrated count) — `localData.isEmpty()` right before the
  `deleteAll()` call (line 248) tells you which Toast string to show. Show the Toast
  *after* `syncLocalToFirebase()` returns, from the `signInLauncher` callback (line
  130 area), not inside `syncLocalToFirebase` itself — keeps the method focused on
  data, keeps UI feedback centralized where the failure-path Toast already lives.
- Don't block the Toast on sync completion if `syncLocalToFirebase` ever becomes
  async (it's currently synchronous Firestore calls via `firestoreDb.add(item)` —
  check whether `IFoodnetDBHelper.add()` is fire-and-forget or awaits; if it's
  fire-and-forget, the "synced" wording is technically optimistic — acceptable for a
  Toast, not worth a loading spinner for this).

### Edge cases
- **Sign-in success but `syncLocalToFirebase` throws/fails partway** (e.g. offline
  mid-migration): out of scope for this doc to redesign error handling, but at
  minimum don't show the "synced" success copy if migration didn't actually
  complete — fall back to the plain "Signed in as %s" string so you're not lying to
  the user. Flagged as an open question below since today there's no error path in
  `syncLocalToFirebase` at all.
- **Offline sign-in**: Firebase Auth sign-in itself will fail before reaching this
  code path (existing failure Toast already covers it) — no new case to handle.
- **Re-sign-in on an already-synced account** (no local data left because it was
  already migrated or there never was any): always hits the "no data to migrate"
  branch — just "Signed in as %s", which is the common/expected case after the
  first login.

---

## Part 2 — Duplicate flow redesign

### Current behavior (confirmed in code)
1. List row tap → `FoodNetAdapter.java` lines 68-82 → opens `FoodNetActivity` via
   `ACTION_VIEW` with `?id=<id>`, showing that item's fields.
2. Inside `FoodNetActivity`, user taps "Duplicate" (`duplicateButton`,
   `activity_food_net.xml` lines 45-55; handler `onClick`, lines 223-229):
   - Silently calls `saveCurrentItem()` first (saves any in-progress edits to the
     *original* item).
   - Stashes the current form's data (minus `OpenDating.ID`) in
     `pendingDuplicateExtras`.
   - Launches `QrCodeScanActivity` via `duplicateQrLauncher`, asking the user to
     scan a **new, blank** QR label.
3. `duplicateQrLauncher` callback (lines 104-130): validates the scanned code
   (correct scheme/host, and rejects scanning the *same* QR via
   `duplicate_same_qr_code` string, line 118-121), then does
   `startActivity(duplicateIntent)` — **does not call `finish()`** on the current
   (original item's) `FoodNetActivity`. So the original activity stays on the back
   stack underneath the new one.
4. New `FoodNetActivity` opens, prefilled from `EXTRA_DUPLICATE_DATA` (lines 144-151),
   with the new scanned QR's id.
5. User edits/reviews, taps **Save** → `onClick` line 219-222 → `saveCurrentItem()`
   then `finish()`. Because the original activity was never finished in step 3, this
   `finish()` pops back to **the original item's `FoodNetActivity`**, which looks
   near-identical to what they just saved (same layout, similar data) — this is the
   reported confusion.

### Root cause
This is a **navigation/back-stack bug**, not just a missing shortcut. Even if you
add a list-row duplicate button (addressing discoverability), the save-after-duplicate
path still needs fixing, because:
- The detail-view duplicate entry point should stay (useful when a user is already
  reviewing an item and decides mid-review to duplicate it — removing it would be a
  regression for that case).
- Both entry points funnel through the same scan → prefilled-form → save sequence,
  so the fix belongs in one place: what happens after the *new* item is saved.

### Fix: new item's "Save" should always return to the list, never to a stale screen
Change what "Save" does when the `FoodNetActivity` instance was opened as a
duplicate target (i.e. it was launched with `EXTRA_DUPLICATE_DATA`):
- Instead of plain `finish()` (which pops to whatever is underneath on the back
  stack — ambiguous, and currently wrong), the duplicate-target activity should
  navigate explicitly back to `FoodNetListActivity`, clearing everything above/below
  it on the stack, so there's exactly one way this flow ends: the list, showing both
  the original and the new duplicated item.
- Concretely: when launching `FoodNetActivity` as a duplicate target (both from the
  existing detail-view path, lines 122-129, and the new list-row path below), add
  `Intent.FLAG_ACTIVITY_CLEAR_TOP` or launch the eventual "return to list" via an
  explicit `Intent` to `FoodNetListActivity` with `FLAG_ACTIVITY_CLEAR_TOP |
  FLAG_ACTIVITY_SINGLE_TOP` instead of relying on `finish()` + implicit back-stack
  pop. Simplest implementation: tag the duplicate-target activity (e.g. a boolean
  extra `EXTRA_IS_DUPLICATE_TARGET` or just check `pendingDuplicateExtras`/intent
  extras aren't null) and in `onClick` for `saveButton` (line 219-222), if this
  activity is a duplicate target, do:
  ```
  startActivity(new Intent(this, FoodNetListActivity.class)
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
  finish();
  ```
  (or simpler: `finishAffinity()` after starting the list activity) so every
  activity between the list and the new save is cleared, regardless of whether the
  user got here from the list directly or from an original item's detail view.
- This also incidentally fixes a secondary latent issue: today, duplicating
  *again* from inside the duplicate-target screen (duplicate-of-a-duplicate) would
  stack a third activity on top of the first two — same bug, compounding. The
  "always return to list" fix resolves this too since it's not back-stack-position
  dependent.

### Addition: "Duplicate" action directly on list rows
Add a third `ImageButton` to `item_food_net.xml`, following the exact same pattern
as the existing `delete` button (lines 46-56) and `FoodNetAdapter.java`'s wiring for
it (lines 84-102):

- **Icon**: reuse `@drawable/ic_duplicate_24dp` (already exists, used by
  `duplicateButton` in `activity_food_net.xml` line 55) — keeps the "duplicate" icon
  visually consistent between detail view and list row.
- **Placement**: left of the existing `delete` button, same row, same vertical
  centering (`app:layout_constraintTop_toTopOf="parent"` /
  `app:layout_constraintBottom_toBottomOf="parent"`), anchored
  `app:layout_constraintEnd_toStartOf="@+id/delete"` (delete stays rightmost/most
  destructive-action-last; duplicate is non-destructive so it's fine closer to the
  content). `food`/`date` TextViews' `app:layout_constraintEnd_toStartOf` need to
  change from `@+id/delete` to the new duplicate button id so text doesn't run
  under the two icons.
- **Touch target**: `wrap_content` + `?attr/selectableItemBackgroundBorderless`
  matches the delete button's existing ~48dp effective target (standard
  `ImageButton` padding); no custom sizing needed.
- **Content description**: new string, e.g. `duplicate_row_button_content_desc` =
  `"Duplicate to another container"` (reuse the existing
  `duplicate_button_content_desc` string verbatim — same action, same wording).
- **Behavior on tap** (`FoodNetAdapter.java`, new handler alongside `del.setOnClickListener`, lines 86-102):
  - No confirmation dialog needed (duplicating is non-destructive — unlike delete,
    there's nothing to lose, so skip the `AlertDialog` step delete uses).
  - Directly launches `QrCodeScanActivity` to scan a new blank label, using that
    row's `OpenDating` data (minus `ID`) as the prefill payload — same
    `EXTRA_DUPLICATE_DATA` bundle shape as the existing detail-view path, so
    `FoodNetActivity`'s existing prefill logic (lines 144-151) needs no changes.
  - **This requires moving the scan-launch + result handling out of
    `FoodNetActivity` and into somewhere `FoodNetAdapter`/`FoodNetListActivity` can
    trigger it** — `ActivityResultLauncher` must be registered on an `Activity` or
    `Fragment`, not inside an `ArrayAdapter`. Cleanest approach: `FoodNetAdapter`
    takes a callback interface (e.g. `onDuplicateRequested(OpenDating item)`)
    implemented by `FoodNetListActivity`, which owns a `duplicateQrLauncher`
    registered there (parallel to its existing `qrCodeScanLauncher`, lines 114-121)
    and, on scan result, starts `FoodNetActivity` with `EXTRA_DUPLICATE_DATA` exactly
    as `FoodNetActivity`'s own launcher does today (lines 122-129) — just triggered
    from the list instead of from a detail screen.
  - After scanning and saving the new item, lands back on **the list** — already
    true for this entry point since there's no "original item screen" underneath to
    begin with; just make sure the new `FoodNetActivity` instance still applies the
    same "duplicate-target save returns to list" logic from the section above, for
    consistency (and so a stray back-press mid-edit doesn't leave weirdness either —
    see discard-changes dialog, lines 165-179, which still applies unchanged).

### Keep both entry points
Per the brief: keep the in-detail-view `duplicateButton` (useful mid-review) *and*
add the list-row one (useful for the common case — duplicate without needing to
open the item at all). They converge on the same scan → prefilled-form → save code
path, and both now resolve to "land on the list" after save, so there's no
behavioral split between them other than how you got to the scan step.

### Flow summary (after fix)

**Path A — duplicate from the list (new):**
List → tap duplicate icon on a row → scan new QR → prefilled new-item form → Save → **List** (both items visible)

**Path B — duplicate from inside an item (existing, fixed):**
List → open item → tap Duplicate → scan new QR → prefilled new-item form → Save → **List** (not the stale original-item screen)

### Edge cases
- **No items in the list**: the new list-row duplicate button simply doesn't exist
  yet (no rows to put it on) — nothing to design for; `empty_list_view` TextView
  (already shown via `listView.setEmptyView`, `FoodNetListActivity.java` line 110)
  covers this state as-is.
- **User cancels the QR scan mid-duplicate** (back button from
  `QrCodeScanActivity`): `qrCodeScanLauncher`/`duplicateQrLauncher` callbacks already
  guard on `result.getResultCode() != 0` / missing `url` extra and simply `return`
  (lines 107-110) — no activity was started, user lands back exactly where they
  tapped duplicate from (list or detail view). No change needed here.
- **User scans the same QR they're duplicating from**: already handled —
  `duplicate_same_qr_code` Toast (line 118-121). Same validation needs to apply
  identically when duplicate is triggered from the list row (the "same QR" check
  needs the source item's id available for comparison regardless of entry point).
- **User scans a QR that's already assigned to a different existing item**: not
  explicitly checked today in the scan-validation block (lines 111-121) — only
  checks scheme/host and "is it the *same* id as the source." If this can happen
  (e.g. user scans an old/already-used physical label by mistake), that's a
  pre-existing gap, not introduced by this change — flagged as an open question
  below rather than silently fixed here.
- **Offline while duplicating** (Firestore-backed `db`): `db.add()` /
  `db.update()` behavior under offline conditions is inherited from whatever
  `FirestoreDBHelper`/local `FoodnetDBHelper` already do elsewhere in the app (no
  new offline-handling need is introduced specifically by the duplicate flow) —
  out of scope here.

---

## Platform notes
- No navigation pattern change beyond the back-stack fix above — the app stays
  single-Activity-per-screen with implicit-intent navigation (`ACTION_VIEW` +
  custom `foodnet://` scheme), consistent with existing code.
- Both changes are phone-only; no tablet/desktop layout considerations apply (this
  app has no responsive/desktop target).
- Icon reuse (`ic_duplicate_24dp`) avoids introducing a second visual language for
  the same action — keep using it anywhere "duplicate" shows up.

## Open questions
1. Should `syncLocalToFirebase` surface a failure state (partial migration, offline
   mid-sync)? Today it has no error handling at all — if this matters, it's a
   separate, slightly bigger fix than "add a Toast," flagging rather than bundling in.
2. Should scanning a QR code that's already assigned to a *different* existing item
   (not the source item, but some other one) be explicitly rejected before duplicate
   completes? Currently unguarded; pre-existing gap, not newly introduced.
3. Confirm icon choice for the new list-row duplicate button — reusing
   `ic_duplicate_24dp` as proposed, rather than designing a new one, unless there's a
   reason to differentiate "duplicate from list" vs "duplicate from detail" visually
   (recommendation: don't differentiate — same action, same icon).
