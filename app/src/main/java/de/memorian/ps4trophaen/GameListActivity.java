package de.memorian.ps4trophaen;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import de.memorian.ps4trophaen.models.Game;
import de.memorian.ps4trophaen.storage.GameOverviewDBHelper;

/**
 * An activity that is used to choose a letter and shows all games starting with this
 * letter afterwards in a GameListFragment.
 *
 *
 * @since 04.11.2014
 */
public class GameListActivity
        extends FragmentActivity
        implements GamesListFragment.OnFragmentInteractionListener, FragmentManager.OnBackStackChangedListener {

    private GameOverviewDBHelper gameOverviewDBHelper;
    private String gameNameActionBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.gameListContainer, new SelectLetterFragment()).commit();
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.gameList);
        gameOverviewDBHelper = GameOverviewDBHelper.getInstance(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
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
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            gameNameActionBarTitle = game.getName();
        }
        GameDetailFragment gameDetailFragment = GameDetailFragment.newInstance(v, game);

        commitFragment(gameDetailFragment);
    }

    @Override
    public void displayDLCGames(Game baseGame, ArrayList<Game> games) {
        gameNameActionBarTitle = baseGame.getName();
        GamesListFragment gamesListFragment = new GamesListFragment();
        Bundle args = new Bundle();
        args.putBoolean(GamesListFragment.DISPLAYS_DLC_MODE, true);
        args.putParcelableArrayList(GamesListFragment.GAMES_ARG, games);
        gamesListFragment.setArguments(args);

        commitFragment(gamesListFragment);
        getActionBar().setTitle(gameNameActionBarTitle);
    }

    public void showGamesWithLetter(String letter) {
        ArrayList<Game> games = gameOverviewDBHelper.getGamesStartingWithLetter(letter);
        Collections.sort(games);
        GamesListFragment gamesListFragment = new GamesListFragment();
        Bundle args = new Bundle();
        args.putString(GamesListFragment.HEADER_ARG, letter);
        args.putParcelableArrayList(GamesListFragment.GAMES_ARG, games);
        gamesListFragment.setArguments(args);

        commitFragment(gamesListFragment);
    }

    private void commitFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.gameListContainer, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onBackStackChanged() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) {
            getActionBar().setTitle(R.string.gameList);
        } else if (backStackEntryCount == 2) {
            getActionBar().setTitle(gameNameActionBarTitle);
        }
    }

    /**
     * Fragment that displays the alphabet to choose a starting letter.
     */
    public static class SelectLetterFragment extends Fragment {

        private GameListActivity mActivity;
        private final String[] letters = new String[]{
                "A", "B", "C", "D", "E",
                "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O",
                "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"};

        public SelectLetterFragment() {
        }

        @Override
        public void onAttach(Activity activity) {
            mActivity = (GameListActivity) activity;
            super.onAttach(activity);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_game_list_grid, container, false);
            GridView gridView = (GridView) rootView.findViewById(R.id.gameListGridView);
            final Typeface typeface = Typeface.createFromAsset(mActivity.getAssets(), MainActivity.TITLE_FONT);
            ArrayAdapter adapter = new ArrayAdapter(mActivity, R.layout.game_list_letter_style, letters) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTypeface(typeface);

                    return textView;
                }
            };
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    String letter = letters[position];
                    onGridClick(letter);
                }
            });

            return rootView;
        }

        private void onGridClick(String letter) {
            mActivity.showGamesWithLetter(letter);
        }
    }
}
