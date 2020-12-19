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

package ovh.bailon.foodnet.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Locale;
import java.util.Random;

public class QrCodeGenerator {
    private static Bitmap appendImages(Bitmap bmp1, Bitmap bmp2, boolean vertical) {
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

    private static Bitmap createQrCode(Uri.Builder builder) {
        Random random = new Random();
        int sn;

//        do {
        sn = random.nextInt(Integer.MAX_VALUE);
//        } while (db.openDatingExists(sn));

        builder.clearQuery();
        builder.appendQueryParameter("id", String.format(Locale.US, "%010d", sn));
        Uri uri = builder.build();

        String text = uri.toString();
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

    private static Bitmap createQrCodeLine(Uri.Builder builder, int cols) {
        Bitmap line = createQrCode(builder);
        for (int i = 1; i < cols; i++) {
            Bitmap qr = createQrCode(builder);
            line = appendImages(line, qr, false);
        }

        return line;
    }

    public static Bitmap createQrCodeSheet(Uri.Builder builder, int cols, int rows) {
        Bitmap sheet = createQrCodeLine(builder, cols);
        for (int i = 1; i < rows; i++) {
            Bitmap line = createQrCodeLine(builder, cols);
            sheet = appendImages(sheet, line, true);
        }

        return sheet;
    }
}
