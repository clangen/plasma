package org.clangen.gfx.plasma;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class ProfileLibrary {
    private static final String TAG = "ProfileLibrary";

    private static String TABLE_NAME = "profile";
    public static String ID_COLUMN = "_id";
    public static String NAME_COLUMN = "name";
    public static String SETTINGS_COLUMN = "settings";

    private static ProfileLibrary sInstance;
    private SQLiteDatabase mDatabase;
    private Application mContext;

    private ProfileLibrary(Context context) {
        mContext = (Application) context.getApplicationContext();
        loadDatabase();
    }

    public static ProfileLibrary getInstance(Context context) {
        synchronized (ProfileLibrary.class) {
            if (sInstance == null) {
                sInstance = new ProfileLibrary(context);
            }
        }

        return sInstance;
    }

    public synchronized Cursor getProfileNames() {
        String sortBy = String.format("LOWER(%s)", NAME_COLUMN);

        return mDatabase.query(
            TABLE_NAME,
            new String[] { ID_COLUMN, NAME_COLUMN },
            null,
            null,
            null,
            null,
            sortBy);
    }

    public synchronized String getSettingsById(long id) {
        return getColumnStringById(id, SETTINGS_COLUMN);
    }

    public synchronized String getNameById(long id) {
        return getColumnStringById(id, NAME_COLUMN);
    }

    public synchronized boolean deleteProfile(long id) {
        int rows = mDatabase.delete(
            TABLE_NAME,
            String.format("%s=%d", ID_COLUMN, id),
            null);

        return (rows > 0);
    }

    public synchronized void deleteAllProfiles() {
        mDatabase.delete(TABLE_NAME, null, null);
    }

    public synchronized long addProfile(String name, String settings) {
        ContentValues values = new ContentValues();
        values.put(NAME_COLUMN, name);
        values.put(SETTINGS_COLUMN, settings);

        return mDatabase.insert(TABLE_NAME, ID_COLUMN, values);
    }

    public synchronized boolean updateProfile(long id, String settings) {
        return updateColumnString(id, SETTINGS_COLUMN, settings);
    }

    public synchronized boolean renameProfile(long id, String name) {
        return updateColumnString(id, NAME_COLUMN, name);
    }

    public synchronized boolean addBuiltInProfiles() {
        try {
            JSONObject json = new JSONObject(
                IoUtility.rawResourceToString(mContext, R.raw.profiles));

            JSONArray keys = json.names();

            for (int i = 0; i < keys.length(); i++) {
                String name = keys.getString(i);
                String value = json.getJSONObject(name).toString();
                addProfile(name, value);
            }

            return true;
        }
        catch (JSONException ex) {
            Log.i(TAG, "addBuiltInProfiles failed", ex);
        }

        return false;
    }

    private boolean updateColumnString(long id, String column, String value) {
        ContentValues values = new ContentValues();
        values.put(column, value);

        int rows = mDatabase.update(
            TABLE_NAME,
            values,
            String.format("%s=%d", ID_COLUMN, id),
            null);

        return (rows > 0);
    }

    private String getColumnStringById(long id, String column) {
        Cursor cursor = mDatabase.query(
            TABLE_NAME,
            new String[] { column },
            String.format("%s=%d", ID_COLUMN, id),
            null,   // bind
            null,   // group by
            null,   // having
            null);  // order

        try {
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToNext();
                return cursor.getString(0);
            }
        }
        finally {
            cursor.close();
        }

        return null;
    }

    private void loadDatabase() {
        OpenHelper dbHelper = new OpenHelper(mContext, "ProfileLibrary", null, 1);
        mDatabase = dbHelper.getWritableDatabase();
    }

    private class OpenHelper extends SQLiteOpenHelper {
        public OpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                " %s INTEGER PRIMARY KEY AUTOINCREMENT," +
                " %s STRING," +
                " %s STRING);",
                TABLE_NAME,
                ID_COLUMN,
                NAME_COLUMN,
                SETTINGS_COLUMN);

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
