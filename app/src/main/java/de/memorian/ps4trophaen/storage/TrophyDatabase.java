package de.memorian.ps4trophaen.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database for maintaining the trophies for every game.
 *
 * @author Tom-Philipp Seifert
 * @since 11.10.2014
 */
public class TrophyDatabase extends SQLiteOpenHelper {

    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String SECRET = "sec";
    public static final String GUIDE = "guide";
    public static final String ICON = "icon";
    public static final String PRIO = "prio";
    /**
     * DB Name.
     */
    private static final String DATABASE_NAME = "trophies.db";
    /**
     * DB Version.
     */
    private static final int DATABASE_VERSION = 2;
    private final Context context;

    public TrophyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static String getCreateText(String tableName) {
        String cmd = "create table "
                + tableName + "("
                + TITLE + " text primary key, "
                + TEXT + " text, "
                + GUIDE + " text, "
                + SECRET + " integer, "
                + TYPE + " text, "
                + ICON + " text, "
                + PRIO + ");";
        return cmd;
    }

    /**
     * Returns the columns of this table.
     *
     * @return A String[].
     */
    public String[] getColumns() {
        String[] arr = new String[]{
                TYPE,
                TITLE,
                TEXT,
                SECRET,
                GUIDE,
                ICON,
                PRIO
        };
        return arr;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                switch (newVersion) {
                    case 2:
                        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

                        if (c.moveToFirst()) {
                            while (!c.isAfterLast()) {
                                String tableName = c.getString(c.getColumnIndex("name"));
                                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + PRIO + " text ");
                                c.moveToNext();
                            }
                        }
                        break;
                }
                break;
        }
    }
}