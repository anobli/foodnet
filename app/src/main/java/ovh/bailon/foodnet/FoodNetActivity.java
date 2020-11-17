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

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FoodNetActivity extends AppCompatActivity
        implements View.OnClickListener, OnDataEventListener {
    private OpenDating openDating = null;
    private EditText editTextFood;
    private EditText editTextProdDate;
    private EditText editTextExpDate;
    private EditText editTextOpeningDate;
    private IFoodnetDBHelper db;
    private String id = null;
    private String saveExit = null;

    final Calendar prodCalendar = Calendar.getInstance();
    final Calendar expCalendar = Calendar.getInstance();
    final Calendar openingCalendar = Calendar.getInstance();

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

        db = new FoodnetDBHelper(this);
        db.registerOnDataChange(this);
        df = DateFormat.getDateInstance(DateFormat.MEDIUM);

        editTextFood = findViewById(R.id.editTextFood);
        editTextProdDate = findViewById(R.id.editTextProdDate);
        editTextExpDate = findViewById(R.id.editTextExpDate);
        editTextOpeningDate = findViewById(R.id.editTextOpeningDate);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (data != null) {
            id = data.getQueryParameter("id");
            saveExit = data.getQueryParameter("exit");
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

    private void updateLabel(EditText v, Calendar calendar,
                             int year, int monthOfYear, int dayOfMonth) {

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        v.setText(df.format(calendar.getTime()));
    }

    DatePickerDialog.OnDateSetListener prodCalendarListener = 
            new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            updateLabel(editTextProdDate, prodCalendar, year, monthOfYear,
                    dayOfMonth);
        }
    };

    DatePickerDialog.OnDateSetListener expCalendarListener =
            new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            updateLabel(editTextExpDate, expCalendar, year, monthOfYear,
                    dayOfMonth);
        }
    };

    DatePickerDialog.OnDateSetListener openingCalendarListener =
            new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            updateLabel(editTextOpeningDate, openingCalendar, year, monthOfYear,
                    dayOfMonth);
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            OpenDating newOpenDating = new OpenDating(
                    Integer.parseInt(id),
                    editTextFood.getText().toString(),
                    editTextProdDate.getText().toString(),
                    editTextExpDate.getText().toString(),
                    editTextOpeningDate.getText().toString());
            if (openDating == null) {
                newOpenDating.scheduleNotifications(this);
                db.add(newOpenDating);
            } else {
                db.update(newOpenDating);
            }
            if (saveExit != null && saveExit.compareTo("1") == 0) {
                finish();
            }
        } else if (v.getId() == R.id.editTextProdDate) {
            new DatePickerDialog(this, prodCalendarListener,
                    prodCalendar.get(Calendar.YEAR),
                    prodCalendar.get(Calendar.MONTH),
                    prodCalendar.get(Calendar.DAY_OF_MONTH)).show();
        } else if (v.getId() == R.id.editTextExpDate) {
            new DatePickerDialog(this, expCalendarListener,
                    expCalendar.get(Calendar.YEAR),
                    expCalendar.get(Calendar.MONTH),
                    expCalendar.get(Calendar.DAY_OF_MONTH)).show();
        } else if (v.getId() == R.id.editTextOpeningDate) {
            new DatePickerDialog(this, openingCalendarListener,
                    openingCalendar.get(Calendar.YEAR),
                    openingCalendar.get(Calendar.MONTH),
                    openingCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    }

    @Override
    public void onGetAllReady(ArrayList<OpenDating> list) {

    }

    @Override
    public void onGetReady(OpenDating openDating) {
        if (openDating != null) {
            editTextFood.setText(openDating.getFood());
            editTextProdDate.setText(openDating.getProdDate());
            editTextExpDate.setText(openDating.getExpDate());
            editTextOpeningDate.setText(openDating.getOpeningDate());
            this.openDating = openDating;
        }
    }
}
