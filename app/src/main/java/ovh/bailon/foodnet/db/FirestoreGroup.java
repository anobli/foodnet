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

package ovh.bailon.foodnet.db;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ovh.bailon.foodnet.OnGroupEventListener;

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
}
