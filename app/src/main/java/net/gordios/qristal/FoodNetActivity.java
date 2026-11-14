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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.gordios.qristal.db.FirestoreDBHelper;
import net.gordios.qristal.db.FoodnetDBHelper;
import net.gordios.qristal.db.IFoodnetDBHelper;
import net.gordios.qristal.ui.ArrayItems;
import net.gordios.qristal.ui.ListItemAdapter;
import net.gordios.qristal.ui.ListItemBase;
import net.gordios.qristal.ui.ListItemDate;
import net.gordios.qristal.ui.ListItemSpinner;
import net.gordios.qristal.ui.ListItemTextValue;
import net.gordios.qristal.utils.QrCodeScanActivity;

public class FoodNetActivity extends AppCompatActivity
        implements View.OnClickListener, OnDataEventListener {
    static final String EXTRA_DUPLICATE_DATA = "duplicate_data";

    private OpenDating openDating = null;
    private IFoodnetDBHelper db;
    private String id = null;
    private ArrayItems items;
    private HashMap<String, String> savedState = new HashMap<>();
    private HashMap<String, String> pendingDuplicateExtras;
    private ActivityResultLauncher<Intent> duplicateQrLauncher;
    private String pendingTransferId;
    private ListenerRegistration transferListener;
    private AlertDialog giveDialog;

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
        items.add(OpenDating.COOKING_TEMP, new ListItemTextValue(this,
                R.layout.item_content_simple_number, R.string.cookingTemp, R.string.cookingTempHint));
        items.add(OpenDating.COOKING_TIME, new ListItemTextValue(this,
                R.layout.item_content_simple_number, R.string.cookingTime, R.string.cookingTimeHint));
        items.add(OpenDating.PORTIONS, new ListItemTextValue(this,
                R.layout.item_content_simple_number, R.string.portions, R.string.portionsHint));
        items.add("Location", new ListItemSpinner(this, new LocationAdapter(this)));
        ArrayAdapter<ListItemBase> listViewAdapter = new ListItemAdapter(this, items.getUiList());
        listViewAdapter.notifyDataSetChanged();
        ListView listView = findViewById(R.id.testListView);
        listView.setAdapter(listViewAdapter);

        duplicateQrLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != 0 || result.getData() == null
                            || !result.getData().hasExtra("url")) {
                        return;
                    }
                    Uri scanned = Uri.parse(result.getData().getStringExtra("url"));
                    String newId = scanned.getQueryParameter("id");
                    if (newId == null || !"foodnet".equals(scanned.getScheme())
                            || !"foodnet.bailon.ovh".equals(scanned.getHost())) {
                        Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (newId.equals(id)) {
                        Toast.makeText(this, R.string.duplicate_same_qr_code, Toast.LENGTH_LONG).show();
                        return;
                    }
                    checkQrCodeAvailable(newId, available -> {
                        if (!available) {
                            Toast.makeText(this, R.string.duplicate_qr_already_used, Toast.LENGTH_LONG).show();
                            return;
                        }
                        Intent duplicateIntent = new Intent(this, FoodNetActivity.class);
                        duplicateIntent.setData(scanned);
                        Bundle extras = new Bundle();
                        for (Map.Entry<String, String> entry : pendingDuplicateExtras.entrySet()) {
                            extras.putString(entry.getKey(), entry.getValue());
                        }
                        duplicateIntent.putExtra(EXTRA_DUPLICATE_DATA, extras);
                        startActivity(duplicateIntent);
                        finish();
                    });
                });

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

            Bundle duplicateExtras = intent.getBundleExtra(EXTRA_DUPLICATE_DATA);
            if (duplicateExtras != null) {
                HashMap<String, String> prefill = new HashMap<>();
                for (String key : duplicateExtras.keySet()) {
                    prefill.put(key, duplicateExtras.getString(key));
                }
                items.updateUi(prefill);
            }

            String transferId = data.getQueryParameter("transfer");
            if (transferId != null) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(this, R.string.give_sign_in_required, Toast.LENGTH_LONG).show();
                } else {
                    pendingTransferId = transferId;
                    HashMap<String, String> prefill = new HashMap<>();
                    for (String key : new String[]{OpenDating.FOOD, OpenDating.PROD_DATE,
                            OpenDating.EXP_DATE, OpenDating.OPENING_DATE, OpenDating.LOCATION, OpenDating.KCAL,
                            OpenDating.COOKING_TEMP, OpenDating.COOKING_TIME, OpenDating.PORTIONS}) {
                        String value = data.getQueryParameter(key);
                        if (value != null) {
                            prefill.put(key, value);
                        }
                    }
                    items.updateUi(prefill);
                }
            }

            savedState = new HashMap<>(items.getHashMap());
            // The Location spinner only settles to its real default after this layout
            // pass; re-snapshot once it has, unless onGetReady() already loaded real data.
            listView.post(() -> {
                if (openDating == null) {
                    savedState = new HashMap<>(items.getHashMap());
                }
            });

            db.requestGet(Long.parseLong(id));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (items.getHashMap().equals(savedState)) {
                    finish();
                    return;
                }
                new AlertDialog.Builder(FoodNetActivity.this)
                        .setTitle(R.string.discard_changes_title)
                        .setMessage(R.string.discard_changes_message)
                        .setPositiveButton(R.string.discard, (dialog, which) -> finish())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        ComponentName receiver = new ComponentName(this, FoodNetBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        findViewById(R.id.saveButton).setOnClickListener(this);
        findViewById(R.id.deleteButton).setOnClickListener(this);
        findViewById(R.id.duplicateButton).setOnClickListener(this);
        findViewById(R.id.giveButton).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transferListener != null) {
            transferListener.remove();
            transferListener = null;
        }
    }

    private interface OnQrAvailabilityListener {
        void onResult(boolean available);
    }

    /**
     * Checks whether {@code qrId} isn't already assigned to an existing item.
     * Uses a standalone helper/listener (not the activity's own {@code db}/{@code this}
     * registration) so this lookup can't race with or clobber the main item-load flow.
     */
    private void checkQrCodeAvailable(String qrId, OnQrAvailabilityListener callback) {
        IFoodnetDBHelper checkDb;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            checkDb = new FoodnetDBHelper(this);
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("foodnet", MODE_PRIVATE);
            String group = sharedPreferences.getString("group", currentUser.getUid());
            checkDb = new FirestoreDBHelper(this, group);
        }
        checkDb.registerOnDataChange(new OnDataEventListener() {
            @Override
            public void onGetAllReady(ArrayList<OpenDating> list) {
            }

            @Override
            public void onGetReady(OpenDating existing) {
                callback.onResult(existing == null);
            }
        });
        checkDb.requestGet(Long.parseLong(qrId));
    }

    /**
     * Validate and persist the data currently shown in the form.
     * @return the saved item, or null if the form data is invalid (a toast is shown in that case)
     */
    private OpenDating saveCurrentItem() {
        OpenDating newOpenDating = new OpenDating(items.getHashMap());
        if (!newOpenDating.check()) {
            Toast.makeText(this, R.string.invalid_food_item, Toast.LENGTH_LONG).show();
            return null;
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
        openDating = newOpenDating;
        savedState = new HashMap<>(items.getHashMap());
        return newOpenDating;
    }

    /**
     * Starts a "give" transfer: saves this item, creates a pending Transfers doc, and
     * shows a QR encoding the full item data plus the transfer id. The dialog listens
     * for that doc to flip to "completed" (written by the recipient after they scan
     * and save) and only then deletes this item -- never on scan alone, so a cancelled
     * or failed scan on the other end leaves this item untouched.
     */
    private void showGiveDialog() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, R.string.give_sign_in_required, Toast.LENGTH_LONG).show();
            return;
        }
        if (saveCurrentItem() == null) {
            return;
        }

        String transferId = Long.toString(new SecureRandom().nextLong() & Long.MAX_VALUE);
        Map<String, Object> transfer = new HashMap<>();
        transfer.put("status", "pending");
        transfer.put("itemId", id);

        FirebaseFirestore.getInstance().collection("Transfers").document(transferId)
                .set(transfer)
                .addOnSuccessListener(aVoid -> displayGiveDialog(transferId))
                .addOnFailureListener(e -> Toast.makeText(this, R.string.give_start_failed, Toast.LENGTH_LONG).show());
    }

    private void displayGiveDialog(String transferId) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("foodnet").authority("foodnet.bailon.ovh");
        builder.appendQueryParameter("id", id);
        builder.appendQueryParameter("transfer", transferId);
        HashMap<String, String> giveData = new HashMap<>(items.getHashMap());
        giveData.remove(OpenDating.ID);
        for (Map.Entry<String, String> entry : giveData.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Bitmap bitmap = createQrBitmap(builder.build().toString());
        if (bitmap == null) {
            Toast.makeText(this, R.string.give_start_failed, Toast.LENGTH_LONG).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_give_qr_code, null);
        ImageView imageView = view.findViewById(R.id.dialogQrCode);
        imageView.setImageBitmap(bitmap);

        giveDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.give_dialog_title)
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> cancelTransfer(transferId))
                .show();

        transferListener = FirebaseFirestore.getInstance()
                .collection("Transfers").document(transferId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }
                    if ("completed".equals(snapshot.getString("status"))) {
                        onTransferCompleted();
                    }
                });
    }

    private void onTransferCompleted() {
        if (transferListener != null) {
            transferListener.remove();
            transferListener = null;
        }
        if (giveDialog != null) {
            giveDialog.dismiss();
            giveDialog = null;
        }
        String food = openDating != null ? openDating.getFood() : "";
        if (openDating != null) {
            openDating.cancelNotifications(this);
            db.delete(openDating);
        }
        Toast.makeText(this, getString(R.string.give_completed, food), Toast.LENGTH_LONG).show();
        finish();
    }

    private void cancelTransfer(String transferId) {
        if (transferListener != null) {
            transferListener.remove();
            transferListener = null;
        }
        FirebaseFirestore.getInstance().collection("Transfers").document(transferId).delete();
        giveDialog = null;
    }

    private void completeTransfer(String transferId) {
        FirebaseFirestore.getInstance().collection("Transfers").document(transferId)
                .update("status", "completed")
                .addOnFailureListener(e -> Toast.makeText(this, R.string.give_complete_failed, Toast.LENGTH_LONG).show());
    }

    private Bitmap createQrBitmap(String text) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 512, 512);
            return new BarcodeEncoder().createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            if (saveCurrentItem() != null) {
                if (pendingTransferId != null) {
                    completeTransfer(pendingTransferId);
                }
                finish();
            }
        } else if (v.getId() == R.id.duplicateButton) {
            if (saveCurrentItem() == null) {
                return;
            }
            pendingDuplicateExtras = new HashMap<>(items.getHashMap());
            pendingDuplicateExtras.remove(OpenDating.ID);
            duplicateQrLauncher.launch(new Intent(this, QrCodeScanActivity.class));
        } else if (v.getId() == R.id.giveButton) {
            showGiveDialog();
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
            savedState = new HashMap<>(items.getHashMap());
        }
    }
}
