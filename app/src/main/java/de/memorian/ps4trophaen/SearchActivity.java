package de.memorian.ps4trophaen;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import de.memorian.ps4trophaen.models.Game;
import de.memorian.ps4trophaen.storage.GameOverviewDBHelper;

/**
 * Activity to search a game.
 *
 * @author Tom-Philipp Seifert
 * @since 04.11.2014
 */
public class SearchActivity extends FragmentActivity implements
        GamesListFragment.OnFragmentInteractionListener,
        FragmentManager.OnBackStackChangedListener,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    private GameOverviewDBHelper gameOverviewDBHelper;
    /**
     * Displays the search results.
     */
    private GamesListFragment gamesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        gameOverviewDBHelper = GameOverviewDBHelper.getInstance(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null) {
            gamesListFragment = new GamesListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(GamesListFragment.GAMES_ARG, new ArrayList<Game>());
            bundle.putBoolean(GamesListFragment.DISPLAYS_DLC_MODE, true);
            gamesListFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.searchContainer, gamesListFragment).commit();
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.title_activity_search);

        //prepare the SearchView
        SearchView searchView = (SearchView) findViewById(R.id.searchView);

        //Sets the default or resting state of the search field. If true, a single search icon is shown by default and
        // expands to show the text field and other buttons when pressed. Also, if the default state is iconified, then it
        // collapses to that state when the close button is pressed. Changes to this property will take effect immediately.
        //The default value is true.
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    /**
     * Method used for performing the search and displaying the results. This method is called every time a letter
     * is introduced in the search field.
     *
     * @param query Query used for performing the search
     */
    private void displayResults(String query) {
        List<Game> foundGames = gameOverviewDBHelper.getGamesStartingWithLetter(query);
        if (foundGames.isEmpty()) {
            foundGames = gameOverviewDBHelper.getGamesContaining(query);
        }
        gamesListFragment.setAllGames(foundGames);
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
        transaction.replace(R.id.searchContainer, gameDetailFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        findViewById(R.id.searchView).setVisibility(View.GONE);
    }

    @Override
    public void displayDLCGames(Game baseGame, ArrayList<Game> games) {

    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            findViewById(R.id.searchView).setVisibility(View.VISIBLE);
            getActionBar().setTitle(R.string.title_activity_search);
        }
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        displayResults(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.isEmpty()) {
            displayResults(newText);
        } else {
            gamesListFragment.setAllGames(new ArrayList<Game>());
        }

        return false;
    }
}
