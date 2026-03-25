/*
 * Copyright (C) 2020 Alexandre Bailon
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * If not, see <https://www.gnu.org/licenses/>.
 */

package net.gordios.qristal.db;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.gordios.qristal.OnGroupEventListener;

public class FirestoreGroup {
    private FirebaseFirestore db;

    private static final String GROUP_MEMBERS = "Members";
    private static final String TAG = "FirestoreGroup";
    private OnGroupEventListener listener;

    public FirestoreGroup() {
        db = FirebaseFirestore.getInstance();
    }

    public void create() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> group = new HashMap<>();
        Map<String, Object> members = new HashMap<>();
        members.put(currentUser.getUid(), currentUser.getEmail());
        group.put(GROUP_MEMBERS, members);

        db.collection("Groups").document(currentUser.getUid())
        .set(group)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
    }

    public void requestGetAll(String groupId) {
        db.collection("Groups").document(groupId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> emails = new ArrayList<String>();
                                HashMap<String, Object> members;
                                members = (HashMap<String, Object>) document.get(GROUP_MEMBERS);
                                for (String key : members.keySet()) {
                                    emails.add((String)members.get(key));
                                }
                                listener.onGetAllReady(emails);
                            } else {
                                create();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    public void add(final String groupId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> group = new HashMap<>();
        Map<String, Object> members = new HashMap<>();
        members.put(currentUser.getUid(), currentUser.getEmail());
        group.put(GROUP_MEMBERS,members);

        db.collection("Groups").document(groupId)
                .set(group, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        requestGetAll(groupId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void registerOnGroupChange(OnGroupEventListener listener) {
        this.listener = listener;
    }

    public void delete(String member) {

    }

    /**
     * Ensure a group document exists for the given group ID.
     * This creates a group with the current user as the only member if it doesn't exist.
     * If the group already exists, this does nothing (merge mode).
     *
     * @param groupId The group ID (usually the user's UID for personal groups)
     */
    public void ensureGroupExists(String groupId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Cannot ensure group exists: no current user");
            return;
        }

        Map<String, Object> group = new HashMap<>();
        Map<String, Object> members = new HashMap<>();
        members.put(currentUser.getUid(), currentUser.getEmail());
        group.put(GROUP_MEMBERS, members);

        db.collection("Groups").document(groupId)
                .set(group, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Group document ensured for: " + groupId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error ensuring group document", e);
                    }
                });
    }

    // ========== Helper methods for account deletion ==========

    /**
     * Callback interface for operations that return a list of group IDs.
     */
    public interface OnGroupsFoundCallback {
        void onSuccess(List<String> groupIds);
        void onFailure(Exception e);
    }

    /**
     * Callback interface for operations that complete without returning data.
     */
    public interface OnOperationCompleteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Find all groups where a specific user is a member.
     * Note: Firestore cannot query map keys directly, so we fetch all groups and filter in memory.
     *
     * @param userId The user ID to search for
     * @param callback Callback with list of group IDs
     */
    public void findGroupsForUser(String userId, OnGroupsFoundCallback callback) {
        db.collection("Groups")
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    List<String> groupIds = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> members = (Map<String, Object>) doc.get(GROUP_MEMBERS);
                        if (members != null && members.containsKey(userId)) {
                            groupIds.add(doc.getId());
                        }
                    }

                    Log.d(TAG, "Found " + groupIds.size() + " groups for user " + userId);
                    callback.onSuccess(groupIds);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to find groups for user", e);
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Remove a user from multiple groups in batch operations.
     * Handles Firestore's 500 operation limit by splitting into batches.
     *
     * @param groupIds List of group IDs to remove user from
     * @param userId User ID to remove
     * @param callback Callback for completion
     */
    public void removeUserFromGroups(List<String> groupIds, String userId, OnOperationCompleteCallback callback) {
        if (groupIds.isEmpty()) {
            callback.onSuccess();
            return;
        }

        final int BATCH_SIZE = 450; // Safe margin below Firestore's 500 limit
        List<WriteBatch> batches = new ArrayList<>();
        WriteBatch batch = db.batch();
        batches.add(batch);
        int operationCount = 0;

        for (String groupId : groupIds) {
            DocumentReference groupRef = db.collection("Groups").document(groupId);
            batch.update(groupRef, GROUP_MEMBERS + "." + userId, FieldValue.delete());
            operationCount++;

            if (operationCount >= BATCH_SIZE) {
                batch = db.batch();
                batches.add(batch);
                operationCount = 0;
            }
        }

        Log.d(TAG, "Removing user from " + groupIds.size() + " groups in " + batches.size() + " batches");
        commitBatchesSequentially(batches, 0, callback);
    }

    /**
     * Find which groups from a list are now empty (have no members).
     *
     * @param groupIds List of group IDs to check
     * @param callback Callback with list of empty group IDs
     */
    public void findEmptyGroups(List<String> groupIds, OnGroupsFoundCallback callback) {
        if (groupIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Fetch all groups in parallel
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String groupId : groupIds) {
            tasks.add(db.collection("Groups").document(groupId).get());
        }

        Tasks.whenAllComplete(tasks)
            .addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                @Override
                public void onSuccess(List<Task<?>> completedTasks) {
                    List<String> emptyGroupIds = new ArrayList<>();

                    for (int i = 0; i < completedTasks.size(); i++) {
                        Task<?> task = completedTasks.get(i);
                        if (task.isSuccessful()) {
                            @SuppressWarnings("unchecked")
                            Task<DocumentSnapshot> docTask = (Task<DocumentSnapshot>) task;
                            DocumentSnapshot doc = docTask.getResult();

                            if (doc.exists()) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> members = (Map<String, Object>) doc.get(GROUP_MEMBERS);
                                if (members == null || members.isEmpty()) {
                                    emptyGroupIds.add(groupIds.get(i));
                                }
                            }
                        }
                    }

                    Log.d(TAG, "Found " + emptyGroupIds.size() + " empty groups");
                    callback.onSuccess(emptyGroupIds);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to check for empty groups", e);
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Delete multiple groups in batch operations.
     * Handles Firestore's 500 operation limit by splitting into batches.
     *
     * @param groupIds List of group IDs to delete
     * @param callback Callback for completion
     */
    public void deleteGroups(List<String> groupIds, OnOperationCompleteCallback callback) {
        if (groupIds.isEmpty()) {
            callback.onSuccess();
            return;
        }

        final int BATCH_SIZE = 450;
        List<WriteBatch> batches = new ArrayList<>();
        WriteBatch batch = db.batch();
        batches.add(batch);
        int operationCount = 0;

        for (String groupId : groupIds) {
            DocumentReference groupRef = db.collection("Groups").document(groupId);
            batch.delete(groupRef);
            operationCount++;

            if (operationCount >= BATCH_SIZE) {
                batch = db.batch();
                batches.add(batch);
                operationCount = 0;
            }
        }

        Log.d(TAG, "Deleting " + groupIds.size() + " groups in " + batches.size() + " batches");
        commitBatchesSequentially(batches, 0, callback);
    }

    /**
     * Delete all food items belonging to a list of groups.
     * Uses whereIn queries chunked into slices of 10 (Firestore limit).
     *
     * @param groupIds List of group IDs whose foods should be deleted
     * @param callback Callback for completion
     */
    public void deleteFoodsForGroups(List<String> groupIds, OnOperationCompleteCallback callback) {
        if (groupIds.isEmpty()) {
            callback.onSuccess();
            return;
        }

        // Firestore whereIn supports at most 10 values at a time
        final int WHERE_IN_LIMIT = 10;
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < groupIds.size(); i += WHERE_IN_LIMIT) {
            chunks.add(groupIds.subList(i, Math.min(i + WHERE_IN_LIMIT, groupIds.size())));
        }

        deleteFoodsForChunks(chunks, 0, callback);
    }

    private void deleteFoodsForChunks(List<List<String>> chunks, int index,
                                      OnOperationCompleteCallback callback) {
        if (index >= chunks.size()) {
            callback.onSuccess();
            return;
        }

        db.collection("Foods")
            .whereIn("Group", chunks.get(index))
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    if (querySnapshot.isEmpty()) {
                        deleteFoodsForChunks(chunks, index + 1, callback);
                        return;
                    }

                    final int BATCH_SIZE = 450;
                    List<WriteBatch> batches = new ArrayList<>();
                    WriteBatch batch = db.batch();
                    batches.add(batch);
                    int count = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        batch.delete(doc.getReference());
                        count++;
                        if (count >= BATCH_SIZE) {
                            batch = db.batch();
                            batches.add(batch);
                            count = 0;
                        }
                    }

                    Log.d(TAG, "Deleting " + querySnapshot.size() + " foods for chunk " + index);
                    commitBatchesSequentially(batches, 0, new OnOperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            deleteFoodsForChunks(chunks, index + 1, callback);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to fetch foods for group chunk " + index, e);
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Helper method to commit multiple WriteBatches sequentially.
     * Sequential commits provide better error tracking than parallel commits.
     */
    private void commitBatchesSequentially(List<WriteBatch> batches, int index,
                                          OnOperationCompleteCallback callback) {
        if (index >= batches.size()) {
            callback.onSuccess();
            return;
        }

        batches.get(index).commit()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Commit next batch
                    commitBatchesSequentially(batches, index + 1, callback);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Batch commit failed at index " + index, e);
                    callback.onFailure(e);
                }
            });
    }
}
