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

package net.gordios.qristal;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.gordios.qristal.db.FirestoreDBHelper;
import net.gordios.qristal.db.FoodnetDBHelper;
import net.gordios.qristal.db.IFoodnetDBHelper;
import net.gordios.qristal.ui.ArrayItems;
import net.gordios.qristal.ui.ListItemAdapter;
import net.gordios.qristal.ui.ListItemBase;
import net.gordios.qristal.ui.ListItemDate;
import net.gordios.qristal.ui.ListItemSpinner;
import net.gordios.qristal.ui.ListItemTextValue;

public class FoodNetActivity extends AppCompatActivity
        implements View.OnClickListener, OnDataEventListener {
    private OpenDating openDating = null;
    private IFoodnetDBHelper db;
    private String id = null;
    private ArrayItems items;

    DateFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_net);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            db = new FoodnetDBHelper(this);
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("foodnet", MODE_PRIVATE);
            String group = sharedPreferences.getString("group", currentUser.getUid());
            db = new FirestoreDBHelper(this, group);
        }
        db.registerOnDataChange(this);
        df = DateFormat.getDateInstance(DateFormat.MEDIUM);

        items = new ArrayItems();
        items.add("Food", new ListItemTextValue(this, R.layout.item_content_simple_text, R.string.food));
        items.add("ProdDate", new ListItemDate(this, R.layout.item_content_simple_date, R.string.prodDate));
        items.add("ExpDate", new ListItemDate(this, R.layout.item_content_simple_date, R.string.expDate));
        items.add("OpeningDate", new ListItemDate(this, R.layout.item_content_simple_date, R.string.openingDate));
        items.add("Kcal", new ListItemTextValue(this, R.layout.item_content_simple_number, R.string.kcal));
        items.add("Location", new ListItemSpinner(this, new LocationAdapter(this)));
        ArrayAdapter<ListItemBase> listViewAdapter = new ListItemAdapter(this, items.getUiList());
        listViewAdapter.notifyDataSetChanged();
        ListView listView = findViewById(R.id.testListView);
        listView.setAdapter(listViewAdapter);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            id = data.getQueryParameter("id");

            String label = data.getQueryParameter("label");
            if (label != null && !label.isEmpty()) {
                HashMap<String, String> prefill = new HashMap<>();
                prefill.put(OpenDating.FOOD, label);
                items.updateUi(prefill);
            }

            db.requestGet(Long.parseLong(id));
        }

        ComponentName receiver = new ComponentName(this, FoodNetBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        findViewById(R.id.saveButton).setOnClickListener(this);
        findViewById(R.id.deleteButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            OpenDating newOpenDating = new OpenDating(items.getHashMap());
            if (!newOpenDating.check()) {
                Toast.makeText(this, R.string.invalid_food_item, Toast.LENGTH_LONG).show();
                return;
            }
            newOpenDating.setId(id);
            if (openDating == null) {
                newOpenDating.scheduleNotifications(this);
                db.add(newOpenDating);
            } else {
                openDating.cancelNotifications(this);
                newOpenDating.scheduleNotifications(this);
                db.update(newOpenDating);
            }
            finish();
        } else if (v.getId() == R.id.deleteButton) {
            if (openDating == null) {
                finish();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_item_title)
                    .setMessage(getString(R.string.delete_item_message, openDating.getFood()))
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        openDating.cancelNotifications(this);
                        db.delete(openDating);
                        finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    @Override
    public void onGetAllReady(ArrayList<OpenDating> list) {

    }

    @Override
    public void onGetReady(OpenDating openDating) {
        if (openDating != null) {
            items.updateUi(openDating.getHashMap());
            this.openDating = openDating;
        }
    }
}
