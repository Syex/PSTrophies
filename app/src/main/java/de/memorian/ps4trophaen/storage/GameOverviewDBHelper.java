package de.memorian.ps4trophaen.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.memorian.ps4trophaen.models.Game;

/**
 * Database support class for the game overview.
 *
 *
 * @since 09.10.2014
 */
public class GameOverviewDBHelper {

    /**
     * The singleton instance.
     */
    private static GameOverviewDBHelper instance;
    /**
     * The columns of the database.
     */
    private final String[] gameOverviewColumns;
    /**
     * Our DB class.
     */
    private final GameOverviewDatabase gameOverviewDatabase;
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
    public static GameOverviewDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GameOverviewDBHelper(context);
        }
        return instance;
    }

    /**
     * Creates a new instance of GameOverviewDBHelper.
     *
     * @param context A context.
     */
    private GameOverviewDBHelper(Context context) {
        gameOverviewDatabase = new GameOverviewDatabase(context);
        gameOverviewColumns = gameOverviewDatabase.getColumns();
        openWritableDatabase();
    }

    public Cursor searchByInputText(String inputText) {
        Cursor cursor = database.query(GameOverviewDatabase.TABLE_NAME,
                gameOverviewColumns,
                GameOverviewDatabase.NAME + " LIKE '" + inputText + "%'",
                null, null, null, GameOverviewDatabase.NAME + " ASC");
        cursor.moveToFirst();
        return cursor;

    }

    /**
     * Adds or replaces a game.
     *
     * @param values The values to insert.
     */
    public void addGame(ContentValues values) {
        database.replace(GameOverviewDatabase.TABLE_NAME, null, values);
    }

    /**
     * Returns the stored games.
     *
     * @return A list of Games.
     */
    public List<Game> getGames() {
        List<Game> games = new ArrayList<Game>();
        Cursor cursor = database.query(GameOverviewDatabase.TABLE_NAME,
                gameOverviewColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Game game = cursorToGame(cursor);
            games.add(game);
            cursor.moveToNext();
        }
        cursor.close();

        return games;
    }

    /**
     * Returns a game with the given name or null if no such game was found.
     *
     * @param gameName The name of the game to look for.
     * @return The corresponding Game or null.
     */
    public Game getGameByName(String gameName) {
        Game game = null;
        gameName = gameName.replace("'", "''");
        Cursor cursor = database.query(GameOverviewDatabase.TABLE_NAME,
                gameOverviewColumns,
                GameOverviewDatabase.NAME +
                        " = '" + gameName + "'",
                null, null, null, GameOverviewDatabase.NAME + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            game = cursorToGame(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        return game;
    }

    /**
     * Returns all games with the given names.
     *
     * @param gameNames The names of the games that shall be returned.
     * @return A list with the found games.
     */
    public List<Game> getGamesByName(List<String> gameNames) {
        ArrayList<Game> games = new ArrayList<Game>();
        if (!gameNames.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            // build where-statement
            for (int i = 0; i < gameNames.size(); i++) {
                String gameName = gameNames.get(i);
                gameName = gameName.replace("'", "''");
                sb.append(GameOverviewDatabase.NAME).
                        append(" = '").append(gameName).append("'");
                if (i + 1 < gameNames.size()) {
                    sb.append(" OR ");
                }
            }
            Cursor cursor = database.query(GameOverviewDatabase.TABLE_NAME,
                    gameOverviewColumns,
                    sb.toString(),
                    null, null, null, GameOverviewDatabase.NAME + " ASC");
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Game game = cursorToGame(cursor);
                games.add(game);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return games;
    }

    /**
     * Returns the games that start with the given letter.
     *
     * @param text The text which the games shall start with.
     * @return A list with found games.
     */
    public ArrayList<Game> getGamesStartingWithLetter(String text) {
        ArrayList<Game> games = new ArrayList<Game>();
        Cursor cursor = database.query(GameOverviewDatabase.TABLE_NAME,
                gameOverviewColumns,
                GameOverviewDatabase.NAME + " LIKE '" + text + "%'",
                null, null, null, GameOverviewDatabase.NAME + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Game game = cursorToGame(cursor);
            games.add(game);
            cursor.moveToNext();
        }
        cursor.close();

        return games;
    }

    /**
     * Returns the games that contain the given string.
     *
     * @param text The text to search in the title of games.
     * @return A list with found games.
     */
    public ArrayList<Game> getGamesContaining(String text) {
        ArrayList<Game> games = new ArrayList<Game>();
        Cursor cursor = database.query(GameOverviewDatabase.TABLE_NAME,
                gameOverviewColumns,
                GameOverviewDatabase.NAME + " LIKE '%" + text + "%'",
                null, null, null, GameOverviewDatabase.NAME + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Game game = cursorToGame(cursor);
            games.add(game);
            cursor.moveToNext();
        }
        cursor.close();

        return games;
    }

    /**
     * Transforms the current cursor row to a Game.
     *
     * @param cursor The cursor object.
     * @return The parsed Game.
     */
    private Game cursorToGame(Cursor cursor) {
        Game game = new Game();
        int i;
        for (String key : cursor.getColumnNames()) {
            if (GameOverviewDatabase.NAME.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setName(cursor.getString(i));
            } else if (GameOverviewDatabase.XREF.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setXref(cursor.getString(i));
            } else if (GameOverviewDatabase.INFOS.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setInfos(cursor.getString(i));
            } else if (GameOverviewDatabase.GAME_LOGO.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setLogoPath(cursor.getString(i));
            } else if (GameOverviewDatabase.BRONZE.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setBronze(cursor.getInt(i));
            } else if (GameOverviewDatabase.SILVER.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setSilver(cursor.getInt(i));
            } else if (GameOverviewDatabase.GOLD.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setGold(cursor.getInt(i));
            } else if (GameOverviewDatabase.PLATIN.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setPlatin(cursor.getInt(i));
            } else if (GameOverviewDatabase.POINTS.equals(key)) {
                i = cursor.getColumnIndex(key);
                game.setPoints(cursor.getInt(i));
            }
        }

        return game;
    }

    /**
     * Opens the SQLite database.
     */
    private void openWritableDatabase() {
        if (database != null) {
            if (!database.isOpen()) {
                database = gameOverviewDatabase.getWritableDatabase();
            }
        } else {
            database = gameOverviewDatabase.getWritableDatabase();
        }
    }
}
