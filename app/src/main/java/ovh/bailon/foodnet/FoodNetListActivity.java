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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class FoodNetListActivity extends AppCompatActivity implements View.OnClickListener {
    private FoodnetDBHelper db;
    private final ArrayList<OpenDating> netList = new ArrayList<OpenDating>();
    private ArrayAdapter<OpenDating> listViewAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_net_list);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        db = new FoodnetDBHelper(this);
        listView = findViewById(R.id.FoodNetList);
        listViewAdapter = new FoodNetAdapter(this, this.netList, db);
        this.listView.setAdapter(this.listViewAdapter);

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();

        netList.clear();
        netList.addAll(db.getAll());
        listViewAdapter.notifyDataSetChanged();
    }

    public Bitmap appendImages(Bitmap bmp1, Bitmap bmp2, boolean vertical) {
        int width;
        int height;
        int left;
        int top;

        if (vertical) {
            width = bmp1.getWidth();
            height = bmp1.getHeight() + bmp2.getHeight();
            top = bmp1.getHeight();
            left = 0;
        } else {
            width = bmp1.getWidth() + bmp2.getWidth();
            height = bmp1.getHeight();
            left = bmp1.getWidth();
            top = 0;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(bitmap);

        comboImage.drawBitmap(bmp1, 0, 0, null);
        comboImage.drawBitmap(bmp2, left, top, null);

        return bitmap;
    }

    private Bitmap createQrCode() {
        Random random = new Random();
        int sn;

        do {
            sn = random.nextInt(Integer.MAX_VALUE);
        } while (db.openDatingExists(sn));

        String text = "foodnet://foodnet.bailon.ovh?id=" + String.format(Locale.US, "%010d", sn);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap createQrCodeLine(int cols) {
        Bitmap line = createQrCode();
        for (int i = 1; i < cols; i++) {
            Bitmap qr = createQrCode();
            line = appendImages(line, qr, false);
        }

        return line;
    }

    private Bitmap createQrCodeSheet(int cols, int rows) {
        Bitmap sheet = createQrCodeLine(cols);
        for (int i = 1; i < rows; i++) {
            Bitmap line = createQrCodeLine(cols);
            sheet = appendImages(sheet, line, true);
        }

        return sheet;
    }

    @Override
    public void onClick(View v) {
        Bitmap sheet = createQrCodeSheet(4, 6);
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.printBitmap("QR code", sheet);
    }
}