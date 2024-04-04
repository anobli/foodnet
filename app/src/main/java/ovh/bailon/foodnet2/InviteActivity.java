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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

import ovh.bailon.foodnet2.db.FirestoreGroup;
import ovh.bailon.foodnet2.utils.QrCodeScanActivity;

public class InviteActivity extends AppCompatActivity implements OnGroupEventListener, View.OnClickListener {

    private static final int QR_CODE_RESULT = 0;
    private FirestoreGroup db;
    private ArrayAdapter<String> listViewAdapter;
    private final ArrayList<String> members = new ArrayList<>();
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        db = new FirestoreGroup();
        db.registerOnGroupChange(this);
        ListView listView = findViewById(R.id.listViewGroup);
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
            editor.apply();
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

    private Bitmap createQrCode() {

        SharedPreferences sharedPreferences = getSharedPreferences("foodnet", MODE_PRIVATE);
        String group = sharedPreferences.getString("group", currentUser.getUid());
        String text = "foodnet://group.foodnet.bailon.ovh?group=" + group;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void showQrCode() {
        Bitmap bitmap = createQrCode();
        if (bitmap == null)
            return;

        AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog_qr_code, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.dialogQrCode);
        imageView.setImageBitmap(bitmap);
        alertadd.setView(view);
        alertadd.setNeutralButton(getString(R.string.done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });

        alertadd.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.inviteButton) {
            showQrCode();
        } else if (v.getId() == R.id.invite_scan_qr) {
            Intent intent = new Intent(this, QrCodeScanActivity.class);
            startActivityForResult(intent, QR_CODE_RESULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_CODE_RESULT) {
            if (resultCode == 0 && data.hasExtra("url")) {
                String url = data.getStringExtra("url");

                if (url.contains("group")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getStringExtra("url")));
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }
}