package de.memorian.ps4trophaen.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

import de.memorian.ps4trophaen.models.Trophy;

/**
 * Database support class for the trophies.
 *
 * @author Tom-Philipp Seifert
 * @since 11.10.2014
 */
public class TrophyDBHelper {

    /**
     * The singleton instance.
     */
    private static TrophyDBHelper instance;
    /**
     * The columns of the database.
     */
    private final String[] trophyColumns;
    /**
     * Our DB class.
     */
    private final TrophyDatabase trophyDatabase;
    /**
     * The actual SQLite database.
     */
    private SQLiteDatabase database;

    /**
     * Returns the singleton instance.
     *
     * @param context A Context object.
     * @return The instance.
     */
    public static TrophyDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TrophyDBHelper(context);
        }
        return instance;
    }

    /**
     * Creates a new instance of TrophyDBHelper.
     *
     * @param context A context.
     */
    private TrophyDBHelper(Context context) {
        trophyDatabase = new TrophyDatabase(context);
        trophyColumns = trophyDatabase.getColumns();
        openWritableDatabase();
    }

    /**
     * Adds or replaces the trophies contained in the JSONObject.
     *
     * @param xref       The xref of the game.
     * @param valuesList The values to insert.
     */
    public void addTrophies(String xref, List<ContentValues> valuesList) {
        if (!tableExists(xref)) {
            String cmd = TrophyDatabase.getCreateText(xref);
            database.execSQL(cmd);
        } else {
            database.delete(xref, null, null);
        }
        for (ContentValues values : valuesList) {
            database.replace(xref, null, values);
        }
    }

    /**
     * Returns the stored trophies for the given cross reference.
     *
     * @return A list of Trophies.
     * @throws android.database.sqlite.SQLiteException Should be thrown if the given
     *                                                 cross reference doesn't exist as a table.
     */
    public List<Trophy> getTrophies(String xref) throws SQLiteException {
        List<Trophy> trophies = new ArrayList<Trophy>();
        Cursor cursor = null;
        try {
            cursor = database.query(xref,
                    trophyColumns, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Trophy game = cursorToTrophy(cursor);
                trophies.add(game);
                cursor.moveToNext();
            }
        } catch (SQLiteException e) {
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return trophies;
    }

    /**
     * Updates the priority of a trophy (e.g. atfer the user swiped the trophy).
     *
     * @param xref        The cross reference of the game the trophy belongs to.
     * @param trophyName  The title of the trophy.
     * @param newPriority The new priority value.
     */
    public void updatePriority(String xref, String trophyName, Trophy.Priority newPriority) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TrophyDatabase.PRIO, newPriority.name());
        database.update(xref, contentValues, TrophyDatabase.TITLE + " = '" + trophyName + "'", null);
    }

    /**
     * Checks if the given table exists.
     *
     * @param tableName The name of the table to check.
     * @return true, if it exists, false otherwise.
     */

    private boolean tableExists(String tableName) {
        Cursor cursor =
                database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /**
     * Transforms the current cursor row to a Trophy.
     *
     * @param cursor The cursor object.
     * @return The parsed Trophy.
     */
    private Trophy cursorToTrophy(Cursor cursor) {
        Trophy trophy = new Trophy();
        int i;
        for (String key : cursor.getColumnNames()) {
            if (TrophyDatabase.TITLE.equals(key)) {
                i = cursor.getColumnIndex(key);
                trophy.setTitle(cursor.getString(i));
            } else if (TrophyDatabase.TEXT.equals(key)) {
                i = cursor.getColumnIndex(key);
                trophy.setText(cursor.getString(i));
            } else if (TrophyDatabase.GUIDE.equals(key)) {
                i = cursor.getColumnIndex(key);
                trophy.setGuide(cursor.getString(i));
            } else if (TrophyDatabase.SECRET.equals(key)) {
                i = cursor.getColumnIndex(key);
                int val = cursor.getInt(i);
                boolean s = val == 1 ? true : false;
                trophy.setSecret(s);
            } else if (TrophyDatabase.TYPE.equals(key)) {
                i = cursor.getColumnIndex(key);
                String s = cursor.getString(i);
                if (s != null) {
                    trophy.setType(Trophy.Type.valueOf(s));
                } else trophy.setType(Trophy.Type.BRONZE);
            } else if (TrophyDatabase.ICON.equals(key)) {
                i = cursor.getColumnIndex(key);
                trophy.setBase64Icon(cursor.getString(i));
            } else if (TrophyDatabase.PRIO.equals(key)) {
                i = cursor.getColumnIndex(key);
                String value = cursor.getString(i);
                if (value != null && !value.isEmpty()) {
                    trophy.setPriority(Trophy.Priority.valueOf(value));
                }
            }
        }

        return trophy;
    }

    /**
     * Opens the SQLite database.
     */
    private void openWritableDatabase() {
        if (database != null) {
            if (!database.isOpen()) {
                database = trophyDatabase.getWritableDatabase();
            }
        } else {
            database = trophyDatabase.getWritableDatabase();
        }
    }
}
