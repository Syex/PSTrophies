package de.memorian.ps4trophaen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.memorian.ps4trophaen.models.Game;
import de.memorian.ps4trophaen.storage.GameOverviewDBHelper;

/**
 * Activity for the favorites.
 *
 *
 * @since 19.10.2014
 */
public class FavoritesActivity extends FragmentActivity implements GamesListFragment.OnFragmentInteractionListener,
        GameDetailFragment.OnFragmentInteractionListener, FragmentManager.OnBackStackChangedListener {

    private final String GAME_DETAIL_FRAG_TAG = "gdfTag";
    private GameOverviewDBHelper gameOverviewDBHelper;
    private GamesListFragment gamesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        gameOverviewDBHelper = GameOverviewDBHelper.getInstance(this);
        if (savedInstanceState == null) {
            gamesListFragment = new GamesListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(GamesListFragment.GAMES_ARG, getFavoritedGames());
            bundle.putBoolean(GamesListFragment.DISPLAYS_DLC_MODE, true);
            gamesListFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.gameListContainer, gamesListFragment).commit();
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.gameFavorites);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        Log.i("ps4favact", "entry onRestore");
    }

    private ArrayList<Game> getFavoritedGames() {
        List<String> gameNames = new ArrayList<String>();
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.FAVORITES_FILE, MODE_PRIVATE);
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                boolean isFavorite = (Boolean) entry.getValue();
                if (isFavorite) {
                    gameNames.add(entry.getKey());
                }
            }
        }
        return (ArrayList<Game>) gameOverviewDBHelper.getGamesByName(gameNames);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGameClick(Game game, View v) {
        GameDetailFragment gameDetailFragment = GameDetailFragment.newInstance(v, game);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.gameListContainer, gameDetailFragment, GAME_DETAIL_FRAG_TAG);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void displayDLCGames(Game baseGame, ArrayList<Game> games) {

    }

    @Override
    public void onFavoriteDeleted(Game game) {
        if (gamesListFragment != null) {
            gamesListFragment.removeGame(game);
        }
    }

    @Override
    public void onFavoriteAdded(Game game) {
        if (gamesListFragment != null) {
            gamesListFragment.addGame(game);
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getActionBar().setTitle(R.string.gameFavorites);
        }
    }
}
