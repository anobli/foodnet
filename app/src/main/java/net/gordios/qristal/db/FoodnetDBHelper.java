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

package net.gordios.qristal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import net.gordios.qristal.OnDataEventListener;
import net.gordios.qristal.OpenDating;

public class FoodnetDBHelper extends SQLiteOpenHelper implements IFoodnetDBHelper {
    private static final String TAG = "FoodNetSQLite";

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "FoodNet";
    private static final String TABLE_NOTE = "OpenDating";

    private static final String COLUMN_ID = "Id";
    private static final String COLUMN_FOOD = "Food";
    private static final String COLUMN_PROD_DATE = "ProdDate";
    private static final String COLUMN_EXP_DATE = "ExpDate";
    private static final String COLUMN_OPENING_DATE = "OpeningDate";
    private static final String COLUMN_LOCATION = "Location";
    private static final String COLUMN_KCAL = "Kcal";
    private static final String COLUMN_COOKING_TEMP = OpenDating.COOKING_TEMP;
    private static final String COLUMN_COOKING_TIME = OpenDating.COOKING_TIME;
    private static final String COLUMN_PORTIONS = OpenDating.PORTIONS;

    private Context context;
    private Locale locale;
    private OnDataEventListener listener;

    /* Used by delete to refresh the good tab */
    private int lastRequestedLocatation;

    private static final String DATABASE_UPDATE_V2 = "ALTER TABLE "
            + TABLE_NOTE + " ADD COLUMN " + COLUMN_LOCATION + " TEXT;";
    private static final String DATABASE_UPDATE_V3 = "ALTER TABLE "
            + TABLE_NOTE + " ADD COLUMN " + COLUMN_KCAL + " TEXT;";
    private static final String[] DATABASE_UPDATE_V4 = {
            "ALTER TABLE " + TABLE_NOTE + " ADD COLUMN " + COLUMN_COOKING_TEMP + " TEXT;",
            "ALTER TABLE " + TABLE_NOTE + " ADD COLUMN " + COLUMN_COOKING_TIME + " TEXT;",
            "ALTER TABLE " + TABLE_NOTE + " ADD COLUMN " + COLUMN_PORTIONS + " TEXT;",
    };

    public FoodnetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.locale = Locale.getDefault();
    }

    public FoodnetDBHelper(Context context, Locale locale) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.locale = locale;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String script = "CREATE TABLE " + TABLE_NOTE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_FOOD + " TEXT,"
                + COLUMN_PROD_DATE + " TEXT," + COLUMN_EXP_DATE + " TEXT,"
                + COLUMN_OPENING_DATE + " TEXT," + COLUMN_LOCATION + " TEXT,"
                + COLUMN_KCAL + " TEXT," + COLUMN_COOKING_TEMP + " TEXT,"
                + COLUMN_COOKING_TIME + " TEXT," + COLUMN_PORTIONS + " TEXT" + ")";
        db.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(DATABASE_UPDATE_V2);
        }
        if (oldVersion < 3) {
            db.execSQL(DATABASE_UPDATE_V3);
        }
        if (oldVersion < 4) {
            for (String sql : DATABASE_UPDATE_V4) {
                db.execSQL(sql);
            }
        }
    }

    /** Build an OpenDating from the current cursor row, looking columns up by name. */
    private OpenDating fromCursor(Cursor cursor) {
        OpenDating openDating = new OpenDating(
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOOD)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPENING_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)), locale);
        openDating.setKcal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KCAL)));
        openDating.setCookingTemp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COOKING_TEMP)));
        openDating.setCookingTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COOKING_TIME)));
        openDating.setPortions(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PORTIONS)));
        return openDating;
    }

    private void putValues(ContentValues values, OpenDating openDating) {
        values.put(COLUMN_FOOD, openDating.getFood());
        values.put(COLUMN_PROD_DATE, openDating.getProdDate());
        values.put(COLUMN_EXP_DATE, openDating.getExpDate());
        values.put(COLUMN_OPENING_DATE, openDating.getOpeningDate());
        values.put(COLUMN_LOCATION, openDating.getLocation());
        values.put(COLUMN_KCAL, openDating.getKcal());
        values.put(COLUMN_COOKING_TEMP, openDating.getCookingTemp());
        values.put(COLUMN_COOKING_TIME, openDating.getCookingTime());
        values.put(COLUMN_PORTIONS, openDating.getPortions());
    }

    public void add(OpenDating openDating) {
        add(openDating, null);
    }

    public void add(OpenDating openDating, OnAddCompleteListener listener) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, openDating.getID());
        putValues(values, openDating);

        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_NOTE, null, values);
        db.close();

        if (listener != null) {
            if (result == -1) {
                listener.onFailure(new Exception("Failed to insert into local database"));
            } else {
                listener.onSuccess();
            }
        }
    }

    @Override
    public void requestGet(long id) {
        OpenDating openDating = get(id);
        listener.onGetReady(openDating);
    }

    @Override
    public void requestGetAll() {
        lastRequestedLocatation = 0;
        ArrayList<OpenDating> list = getAll();
        listener.onGetAllReady(list);
    }

    @Override
    public void requestGetAll(int location) {
        lastRequestedLocatation = location;
        ArrayList<OpenDating> noteList = new ArrayList<OpenDating>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTE + " WHERE " + COLUMN_LOCATION + " == " + location;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                noteList.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        listener.onGetAllReady(noteList);
    }
    private OpenDating get(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NOTE, new String[] {
                COLUMN_ID, COLUMN_FOOD, COLUMN_PROD_DATE, COLUMN_EXP_DATE, COLUMN_OPENING_DATE,
                COLUMN_LOCATION, COLUMN_KCAL, COLUMN_COOKING_TEMP, COLUMN_COOKING_TIME,
                COLUMN_PORTIONS },
                COLUMN_ID + "=?", new String[] { String.valueOf(id) },
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        try {
            OpenDating openingDate = fromCursor(cursor);
            cursor.close();
            return openingDate;
        } catch (android.database.CursorIndexOutOfBoundsException ex) {
            Log.d(TAG, "get ... Failed to find a net for " + id);

            cursor.close();
            return null;
        }
    }

    public boolean openDatingExists(int id) {
        OpenDating openDating = get(id);

        if (openDating == null) {
            return false;
        }

        return true;
    }

    @Override
    public ArrayList<OpenDating> getAll() {
        ArrayList<OpenDating> noteList = new ArrayList<OpenDating>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                noteList.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    }

    public int update(OpenDating openDating) {
        if (openDating == null) {
            return 0;
        }

        ContentValues values = new ContentValues();
        putValues(values, openDating);

        SQLiteDatabase db = this.getWritableDatabase();
        int ret = db.update(TABLE_NOTE, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(openDating.getID())});
        if (lastRequestedLocatation != 0)
            requestGetAll(lastRequestedLocatation);
        else
            requestGetAll();

        return ret;
    }

    public void delete(OpenDating openDating) {
        if (openDating == null) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTE, COLUMN_ID + " = ?",
                new String[] { String.valueOf(openDating.getID()) });
        db.close();

        if (lastRequestedLocatation != 0)
            requestGetAll(lastRequestedLocatation);
        else
            requestGetAll();
    }

    @Override
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTE, null, null);
        db.close();
    }

    @Override
    public void registerOnDataChange(OnDataEventListener listener) {
        this.listener = listener;
    }
}
