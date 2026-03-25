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

import android.Manifest;
import android.app.ProgressDialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.print.PrintHelper;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gordios.qristal.db.FirestoreDBHelper;
import net.gordios.qristal.db.FirestoreGroup;
import net.gordios.qristal.db.FoodnetDBHelper;
import net.gordios.qristal.db.IFoodnetDBHelper;
import net.gordios.qristal.utils.QrCodeGenerator;
import net.gordios.qristal.utils.QrCodeScanActivity;

import static net.gordios.qristal.LocationAdapter.CUPBOARD_ID;
import static net.gordios.qristal.LocationAdapter.FREEZER_ID;
import static net.gordios.qristal.LocationAdapter.FRIDGE_ID;

public class FoodNetListActivity extends AppCompatActivity
        implements View.OnClickListener, OnDataEventListener, TabLayout.OnTabSelectedListener {
    private IFoodnetDBHelper db;
    private final ArrayList<OpenDating> netList = new ArrayList<>();
    private ArrayAdapter<OpenDating> listViewAdapter;
    private TabLayout tabLayout;
    private static final int RC_SIGN_IN = 123;
    private static final int QR_CODE_RESULT = 0;
    private ActivityResultLauncher<Intent> qrCodeScanLauncher;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_net_list);

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
        tabLayout.addOnTabSelectedListener(this);

        qrCodeScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 0 && result.getData() != null && result.getData().hasExtra("url")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getData().getStringExtra("url")));
                        startActivity(intent);
                    }
                });

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            invalidateOptionsMenu();
                            SharedPreferences sharedPreferences = getSharedPreferences("foodnet", MODE_PRIVATE);
                            String group = sharedPreferences.getString("group", user.getUid());
                            db = new FirestoreDBHelper(this, group);
                            db.registerOnDataChange(this);
                            // Ensure the group document exists so the user can be found
                            // by findGroupsForUser (e.g. for GDPR account deletion).
                            new FirestoreGroup().ensureGroupExists(user.getUid());
                            requestGetAll();
                        }
                    }
                });

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
    public void onClick(@NonNull View v) {
        if (v.getId() ==  R.id.print_qr) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("foodnet").authority("foodnet.bailon.ovh");
            Bitmap sheet = QrCodeGenerator.createQrCodeSheet(builder, 4, 6);
            PrintHelper photoPrinter = new PrintHelper(this);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            photoPrinter.printBitmap("QR code", sheet);
        } else if(v.getId() == R.id.scan_qr) {
                Intent intent = new Intent(FoodNetListActivity.this, QrCodeScanActivity.class);
                qrCodeScanLauncher.launch(intent);
        }
    }

    @Override
    public void onGetAllReady(@NonNull ArrayList<OpenDating> list) {
        netList.clear();
        netList.addAll(list);
        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetReady(OpenDating openDating) {
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.connect) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            signInLauncher.launch(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build());
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
        } else if (item.getItemId() == R.id.delete_account) {
            showDeleteAccountDialog();
            return true;
        } else {
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account_title)
                .setMessage(R.string.delete_account_message)
                .setPositiveButton(R.string.delete_account_confirm, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.delete_account_cancel, null)
                .show();
    }

    @SuppressWarnings("deprecation")
    private void deleteAccount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        FirestoreGroup firestoreGroup = new FirestoreGroup();

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.delete_account_progress));
        progress.setCancelable(false);
        progress.show();

        firestoreGroup.findGroupsForUser(userId, new FirestoreGroup.OnGroupsFoundCallback() {
            @Override
            public void onSuccess(java.util.List<String> groupIds) {
                firestoreGroup.removeUserFromGroups(groupIds, userId, new FirestoreGroup.OnOperationCompleteCallback() {
                    @Override
                    public void onSuccess() {
                        firestoreGroup.findEmptyGroups(groupIds, new FirestoreGroup.OnGroupsFoundCallback() {
                            @Override
                            public void onSuccess(java.util.List<String> emptyGroupIds) {
                                // Include groups for food deletion:
                                // 1. All empty groups (no members left after removing this user)
                                // 2. The current group if it's the user's personal group (userId)
                                //    This handles the case where foods are stored under Group=userId
                                //    even when no group document exists.
                                java.util.List<String> groupsForFoodDeletion = new java.util.ArrayList<>(emptyGroupIds);

                                // Always include userId - foods may be stored there even without a group document
                                if (!groupsForFoodDeletion.contains(userId)) {
                                    groupsForFoodDeletion.add(userId);
                                }

                                // If currentGroup equals userId, it's already added above
                                // If currentGroup is in emptyGroupIds, it's already in the list
                                // If currentGroup is different and still has members, DON'T delete
                                // (those foods belong to the shared group, not just this user)
                                firestoreGroup.deleteFoodsForGroups(groupsForFoodDeletion, new FirestoreGroup.OnOperationCompleteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        firestoreGroup.deleteGroups(emptyGroupIds, new FirestoreGroup.OnOperationCompleteCallback() {
                                            @Override
                                            public void onSuccess() {
                                                currentUser.delete().addOnCompleteListener(task -> {
                                                    progress.dismiss();
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(FoodNetListActivity.this,
                                                                R.string.delete_account_success,
                                                                Toast.LENGTH_LONG).show();
                                                        getSharedPreferences("foodnet", MODE_PRIVATE)
                                                                .edit().remove("group").apply();
                                                        db = new FoodnetDBHelper(FoodNetListActivity.this);
                                                        db.registerOnDataChange(FoodNetListActivity.this);
                                                        invalidateOptionsMenu();
                                                        requestGetAll();
                                                    } else {
                                                        Exception e = task.getException();
                                                        if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                                                            Toast.makeText(FoodNetListActivity.this,
                                                                    R.string.delete_account_auth_required,
                                                                    Toast.LENGTH_LONG).show();
                                                        } else {
                                                            String msg = e != null ? e.getMessage() : "";
                                                            Toast.makeText(FoodNetListActivity.this,
                                                                    getString(R.string.delete_account_error, msg),
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                progress.dismiss();
                                                Toast.makeText(FoodNetListActivity.this,
                                                        getString(R.string.delete_account_error, e.getMessage()),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        progress.dismiss();
                                        Toast.makeText(FoodNetListActivity.this,
                                                getString(R.string.delete_account_error, e.getMessage()),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                progress.dismiss();
                                Toast.makeText(FoodNetListActivity.this,
                                        getString(R.string.delete_account_error, e.getMessage()),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progress.dismiss();
                        Toast.makeText(FoodNetListActivity.this,
                                getString(R.string.delete_account_error, e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                progress.dismiss();
                Toast.makeText(FoodNetListActivity.this,
                        getString(R.string.delete_account_error, e.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onTabSelected(@NonNull TabLayout.Tab tab) {
        requestGetAll();
    }

    @Override
    public void onTabUnselected(@NonNull TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(@NonNull TabLayout.Tab tab) {

    }
}