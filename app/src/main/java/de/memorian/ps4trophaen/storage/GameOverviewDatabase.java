package de.memorian.ps4trophaen.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database containing the games and general information about them.
 *
 *
 * @since 09.10.2014
 */
public class GameOverviewDatabase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "games";
    public static final String NAME = "name";
    public static final String XREF = "xref";
    public static final String INFOS = "infos";
    public static final String PLATIN = "platin";
    public static final String GOLD = "gold";
    public static final String SILVER = "silver";
    public static final String BRONZE = "bronze";
    public static final String POINTS = "points";
    public static final String GAME_LOGO = "logo";
    /**
     * Create SQL-Statement.
     */
    private final String DATABASE_CREATE_GAMES = "create table "
            + TABLE_NAME + "("
            + NAME + " text primary key, "
            + XREF + " text, "
            + INFOS + " text, "
            + PLATIN + " integer, "
            + GOLD + " integer, "
            + SILVER + " integer, "
            + BRONZE + " integer, "
            + POINTS + " integer, "
            + GAME_LOGO + " text);";
    /**
     * DB Name.
     */
    private static final String DATABASE_NAME = "games.db";
    /**
     * DB Version.
     */
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public GameOverviewDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Returns the columns of this table.
     *
     * @return A String[].
     */
    public String[] getColumns() {
        String[] arr = new String[] {
                NAME,
                XREF,
                INFOS,
                PLATIN,
                GOLD,
                SILVER,
                BRONZE,
                POINTS,
                GAME_LOGO
        };
        return arr;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_GAMES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
