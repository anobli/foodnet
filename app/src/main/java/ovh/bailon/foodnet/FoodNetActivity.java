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

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.util.ArrayList;

import ovh.bailon.foodnet.db.FirestoreDBHelper;
import ovh.bailon.foodnet.db.FoodnetDBHelper;
import ovh.bailon.foodnet.db.IFoodnetDBHelper;
import ovh.bailon.foodnet.ui.ArrayItems;
import ovh.bailon.foodnet.ui.ListItemAdapter;
import ovh.bailon.foodnet.ui.ListItemBase;
import ovh.bailon.foodnet.ui.ListItemDate;
import ovh.bailon.foodnet.ui.ListItemSpinner;
import ovh.bailon.foodnet.ui.ListItemTextValue;

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

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
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
        items.add("Location", new ListItemSpinner(this, new LocationAdapter(this)));
        ArrayAdapter<ListItemBase> listViewAdapter = new ListItemAdapter(this, items.getUiList());
        listViewAdapter.notifyDataSetChanged();
        ListView listView = findViewById(R.id.testListView);
        listView.setAdapter(listViewAdapter);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            id = data.getQueryParameter("id");
            db.requestGet(Long.parseLong(id));
        }

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        ComponentName receiver = new ComponentName(this, FoodNetBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            OpenDating newOpenDating = new OpenDating(items.getHashMap());
            newOpenDating.setId(id);
            if (openDating == null) {
                newOpenDating.scheduleNotifications(this);
                db.add(newOpenDating);
            } else {
                db.update(newOpenDating);
            }
            finish();
        } else if (v.getId() == R.id.deleteButton) {
            db.delete(openDating);
            finish();
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
