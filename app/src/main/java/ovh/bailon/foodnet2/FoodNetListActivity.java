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

package ovh.bailon.foodnet2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.print.PrintHelper;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ovh.bailon.foodnet2.db.FirestoreDBHelper;
import ovh.bailon.foodnet2.db.FoodnetDBHelper;
import ovh.bailon.foodnet2.db.IFoodnetDBHelper;
import ovh.bailon.foodnet2.utils.QrCodeGenerator;
import ovh.bailon.foodnet2.utils.QrCodeScanActivity;

import static ovh.bailon.foodnet2.LocationAdapter.CUPBOARD_ID;
import static ovh.bailon.foodnet2.LocationAdapter.FREEZER_ID;
import static ovh.bailon.foodnet2.LocationAdapter.FRIDGE_ID;

public class FoodNetListActivity extends AppCompatActivity
        implements View.OnClickListener, OnDataEventListener, TabLayout.OnTabSelectedListener {
    private IFoodnetDBHelper db;
    private final ArrayList<OpenDating> netList = new ArrayList<>();
    private ArrayAdapter<OpenDating> listViewAdapter;
    private TabLayout tabLayout;
    private static final int RC_SIGN_IN = 123;
    private static final int QR_CODE_RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_net_list);

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
        ListView listView = findViewById(R.id.FoodNetList);
        listViewAdapter = new FoodNetAdapter(this, this.netList, db);
        listView.setAdapter(this.listViewAdapter);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setOnTabSelectedListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String> request = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean isGranted) {
                            if (!isGranted) {
                                Toast toast = Toast.makeText(FoodNetListActivity.this, R.string.camera_permission_denied, Toast.LENGTH_LONG);
                                toast.show();

                                ImageButton qr_scan_btn = (ImageButton) findViewById(R.id.scan_qr);
                                qr_scan_btn.setEnabled(false);
                                qr_scan_btn.setVisibility(ImageButton.INVISIBLE);
                            }
                        }
                    });
            request.launch(Manifest.permission.CAMERA);
        }

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        ImageButton print_qr = findViewById(R.id.print_qr);
        print_qr.setOnClickListener(this);

        ImageButton scan_qr = findViewById(R.id.scan_qr);
        scan_qr.setOnClickListener(this);
    }

    private void requestGetAll() {
        if (tabLayout.getSelectedTabPosition() == 0) db.requestGetAll(FRIDGE_ID);
        if (tabLayout.getSelectedTabPosition() == 1) db.requestGetAll(FREEZER_ID);
        if (tabLayout.getSelectedTabPosition() == 2) db.requestGetAll(CUPBOARD_ID);
        if (tabLayout.getSelectedTabPosition() == 3) db.requestGetAll();
    }

    @Override
    protected void onStart() {
        super.onStart();

        requestGetAll();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() ==  R.id.print_qr) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("foodnet").authority("foodnet.bailon.ovh");
            Bitmap sheet = QrCodeGenerator.createQrCodeSheet(builder, 4, 6);
            PrintHelper photoPrinter = new PrintHelper(this);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            photoPrinter.printBitmap("QR code", sheet);
        } else if(v.getId() == R.id.scan_qr) {
                Intent intent = new Intent(FoodNetListActivity.this, QrCodeScanActivity.class);
                startActivityForResult(intent, QR_CODE_RESULT);
        }
    }

    @Override
    public void onGetAllReady(ArrayList<OpenDating> list) {
        netList.clear();
        netList.addAll(list);
        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetReady(OpenDating openDating) {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            inflater.inflate(R.menu.connect_menu, menu);
        } else {
            inflater.inflate(R.menu.disconnect_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.connect) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
            return true;
        } else if (item.getItemId() == R.id.disconnect) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            invalidateOptionsMenu();
                            db = new FoodnetDBHelper(FoodNetListActivity.this);
                            db.registerOnDataChange(FoodNetListActivity.this);
                            requestGetAll();
                        }
                    });
            return true;
        } else if (item.getItemId() == R.id.group) {
            Intent intent = new Intent(this, InviteActivity.class);
            startActivity(intent);
            return true;
        } else {
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                invalidateOptionsMenu();
                SharedPreferences sharedPreferences = getSharedPreferences("foodnet", MODE_PRIVATE);
                String group = sharedPreferences.getString("group", user.getUid());
                db = new FirestoreDBHelper(this, group);
                db.registerOnDataChange(this);
                requestGetAll();
            }
        } else if (requestCode == QR_CODE_RESULT) {
            if (resultCode == 0 && data != null && data.hasExtra("url")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getStringExtra("url")));
                startActivity(intent);
            }
        }
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        requestGetAll();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}