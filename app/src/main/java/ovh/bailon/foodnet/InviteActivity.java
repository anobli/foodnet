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

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class InviteActivity extends AppCompatActivity implements OnGroupEventListener {

    private FirestoreGroup db;
    private ListView listView;
    private ArrayAdapter<String> listViewAdapter;
    private final ArrayList<String> members = new ArrayList<String>();
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        db = new FirestoreGroup();
        db.registerOnGroupChange(this);
        listView = findViewById(R.id.listViewGroup);
        listViewAdapter = new GroupAdapter(this, members, db);
        listView.setAdapter(this.listViewAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences("foodnet", MODE_PRIVATE);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String group = data.getQueryParameter("group");
            db.add(group);
            db.requestGetAll(group);
            // TOAST

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("group", group);
            editor.commit();
        } else {
            String group = sharedPreferences.getString("group", currentUser.getUid());
            db.requestGetAll(group);
        }
    }

    @Override
    public void onGetAllReady(List<String> list) {
        members.clear();
        members.addAll(list);
        listViewAdapter.notifyDataSetChanged();
    }
}