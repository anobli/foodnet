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

package ovh.bailon.foodnet;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirestoreDBHelper implements IFoodnetDBHelper {
    private static final String TAG = "FoodNetSQLite";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FoodNet";
    private static final String TABLE_NOTE = "OpenDating";

    private static final String COLUMN_ID = "Id";
    private static final String COLUMN_GROUP = "Group";
    private static final String COLUMN_FOOD = "Food";
    private static final String COLUMN_PROD_DATE = "ProdDate";
    private static final String COLUMN_EXP_DATE = "ExpDate";
    private static final String COLUMN_OPENING_DATE = "OpeningDate";

    private static final String GROUP_OWNER = "Owner";
    private static final String GROUP_MEMBERS = "Members";

    private Context context;
    private Locale locale;
    private FirebaseFirestore db;
    private OnDataEventListener listener;

    public FirestoreDBHelper(Context context) {
        this.context = context;
        this.locale = Locale.getDefault();

        db = FirebaseFirestore.getInstance();
    }

    public void add(OpenDating openDating) {
        Map<String, Object> food = new HashMap<>();
        food.put(COLUMN_ID, openDating.getID());
        food.put(COLUMN_FOOD, openDating.getFood());
        food.put(COLUMN_PROD_DATE, openDating.getProdDate());
        food.put(COLUMN_EXP_DATE, openDating.getExpDate());
        food.put(COLUMN_OPENING_DATE, openDating.getOpeningDate());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> members = new HashMap<>();
        members.put(currentUser.getUid(), currentUser.getEmail());
        food.put(GROUP_MEMBERS,Arrays.asList(currentUser.getUid()));

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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("Foods")
                .whereArrayContains(GROUP_MEMBERS, currentUser.getUid())
                .whereEqualTo(COLUMN_ID, id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> food = document.getData();
                                OpenDating openDating = new OpenDating(
                                        (long)food.get(COLUMN_ID),
                                        (String) food.get(COLUMN_FOOD),
                                        (String) food.get(COLUMN_PROD_DATE),
                                        (String) food.get(COLUMN_EXP_DATE),
                                        (String) food.get(COLUMN_OPENING_DATE));
                                listener.onGetReady(openDating);
                            }
                        } else {
                            listener.onGetReady(null);
                        }
                    }
                });
    }

    public void requestGetAll() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("Foods")
                .whereArrayContains(GROUP_MEMBERS, currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<OpenDating> list = new ArrayList<OpenDating>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> food = document.getData();
                                OpenDating openDating = new OpenDating(
                                        (long)food.get(COLUMN_ID),
                                        (String) food.get(COLUMN_FOOD),
                                        (String) food.get(COLUMN_PROD_DATE),
                                        (String) food.get(COLUMN_EXP_DATE),
                                        (String) food.get(COLUMN_OPENING_DATE));
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("Foods")
                .whereArrayContains(GROUP_MEMBERS, currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

                                OpenDating newOpenDating = new OpenDating(
                                        (long)food.get(COLUMN_ID),
                                        (String) food.get(COLUMN_FOOD),
                                        (String) food.get(COLUMN_PROD_DATE),
                                        (String) food.get(COLUMN_EXP_DATE),
                                        (String) food.get(COLUMN_OPENING_DATE));
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
