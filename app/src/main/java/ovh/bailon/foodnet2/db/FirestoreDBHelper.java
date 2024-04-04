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

package ovh.bailon.foodnet2.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ovh.bailon.foodnet2.OnDataEventListener;
import ovh.bailon.foodnet2.OpenDating;

public class FirestoreDBHelper implements IFoodnetDBHelper {
    private static final String TAG = "FoodNetSQLite";

    private static final String COLUMN_ID = OpenDating.ID;
    private static final String COLUMN_GROUP = "Group";
    private static final String COLUMN_FOOD = OpenDating.FOOD;
    private static final String COLUMN_PROD_DATE = OpenDating.PROD_DATE;
    private static final String COLUMN_EXP_DATE = OpenDating.EXP_DATE;
    private static final String COLUMN_OPENING_DATE = OpenDating.OPENING_DATE;
    private static final String COLUMN_LOCATION = OpenDating.LOCATION;

    private FirebaseFirestore db;
    private OnDataEventListener listener;
    private String group_id;

    /* Used by delete to refresh the good tab */
    private int lastRequestedLocatation;

    public FirestoreDBHelper(Context context, String group_id) {
        this.group_id = group_id;

        db = FirebaseFirestore.getInstance();
    }

    public void add(OpenDating openDating) {
        Map<String, Object> food = new HashMap<>();
        food.put(COLUMN_ID, openDating.getID());
        food.put(COLUMN_GROUP, group_id);
        food.put(COLUMN_FOOD, openDating.getFood());
        food.put(COLUMN_PROD_DATE, openDating.getProdDate());
        food.put(COLUMN_EXP_DATE, openDating.getExpDate());
        food.put(COLUMN_OPENING_DATE, openDating.getOpeningDate());
        food.put(COLUMN_LOCATION, openDating.getLocation());

        db.collection("Foods").document(Long.toString(openDating.getID()))
                .set(food)
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

    public void requestGet(long id) {
        db.collection("Foods")
                .whereEqualTo(COLUMN_GROUP, group_id)
                .whereEqualTo(COLUMN_ID, id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> food = document.getData();
                                OpenDating openDating = new OpenDating(food);
                                listener.onGetReady(openDating);
                            }
                        } else {
                            listener.onGetReady(null);
                        }
                    }
                });
    }

    public void requestGetAll() {
        lastRequestedLocatation = 0;
        db.collection("Foods")
                .whereEqualTo(COLUMN_GROUP, group_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<OpenDating> list = new ArrayList<OpenDating>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> food = document.getData();
                                OpenDating openDating = new OpenDating(food);
                                list.add(openDating);
                            }
                            listener.onGetAllReady(list);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void requestGetAll(int location) {
        lastRequestedLocatation = location;
        db.collection("Foods")
                .whereEqualTo(COLUMN_GROUP, group_id)
                .whereEqualTo(COLUMN_LOCATION, Long.toString(location))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<OpenDating> list = new ArrayList<OpenDating>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> food = document.getData();
                                OpenDating openDating = new OpenDating(food);
                                list.add(openDating);
                            }
                            listener.onGetAllReady(list);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public int update(OpenDating openDating) {
        add(openDating);
        return 0;
    }

    public void delete(final OpenDating openDating) {
        Query query = db.collection("Foods")
                .whereEqualTo(COLUMN_GROUP, group_id);
        if (lastRequestedLocatation != 0)
            query = query.whereEqualTo(COLUMN_LOCATION, Long.toString(lastRequestedLocatation));
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<OpenDating> list = new ArrayList<OpenDating>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> food = document.getData();
                                if ((long)food.get(COLUMN_ID) == openDating.getID()) {
                                    db.collection("Foods").document(document.getId()).delete();
                                    continue;
                                }
                               OpenDating newOpenDating = new OpenDating(food);
                                list.add(newOpenDating);
                            }
                            listener.onGetAllReady(list);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void registerOnDataChange(OnDataEventListener listener) {
        this.listener = listener;
    }
}
