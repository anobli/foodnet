package ovh.bailon.foodnet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodnetDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "FoodNetSQLite";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FoodNet";
    private static final String TABLE_NOTE = "OpenDating";

    private static final String COLUMN_ID = "Id";
    private static final String COLUMN_FOOD = "Food";
    private static final String COLUMN_PROD_DATE = "ProdDate";
    private static final String COLUMN_EXP_DATE = "ExpDate";
    private static final String COLUMN_OPENING_DATE = "OpeningDate";

    private Context context;
    private Locale locale;

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
                + COLUMN_OPENING_DATE + " TEXT" + ")";
        db.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void add(OpenDating openDating) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, openDating.getID());
        values.put(COLUMN_FOOD, openDating.getFood());
        values.put(COLUMN_PROD_DATE, openDating.getProdDate());
        values.put(COLUMN_EXP_DATE, openDating.getExpDate());
        values.put(COLUMN_OPENING_DATE, openDating.getOpeningDate());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NOTE, null, values);
        db.close();
    }

    public OpenDating get(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NOTE, new String[] {
                COLUMN_ID, COLUMN_FOOD, COLUMN_PROD_DATE, COLUMN_EXP_DATE, COLUMN_OPENING_DATE },
                COLUMN_ID + "=?", new String[] { String.valueOf(id) },
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        try {
            OpenDating openingDate = new OpenDating(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4),
                    locale);

            cursor.close();
            return openingDate;
        } catch (android.database.CursorIndexOutOfBoundsException ex) {
            Log.d(TAG, "get ... Failed to find a net for " + id);

            cursor.close();
            return null;
        }
    }

    public List<OpenDating> getAll() {
        List<OpenDating> noteList = new ArrayList<OpenDating>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                OpenDating openingDate = new OpenDating(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4),
                        locale);
                noteList.add(openingDate);
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
        values.put(COLUMN_FOOD, openDating.getFood());
        values.put(COLUMN_PROD_DATE, openDating.getProdDate());
        values.put(COLUMN_EXP_DATE, openDating.getExpDate());
        values.put(COLUMN_OPENING_DATE, openDating.getOpeningDate());

        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_NOTE, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(openDating.getID())});
    }

    public void delete(OpenDating openDating) {
        if (openDating == null) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTE, COLUMN_ID + " = ?",
                new String[] { String.valueOf(openDating.getID()) });
        db.close();
    }
}
