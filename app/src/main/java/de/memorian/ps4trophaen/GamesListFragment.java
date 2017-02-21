package de.memorian.ps4trophaen;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.memorian.ps4trophaen.models.Game;

/**
 * A ListFragment to display a set of Games.
 *
 * @author Tom-Philipp Seifert (Syex90@gmail.com)
 * @since 10.10.2014
 */
public class GamesListFragment extends ListFragment {

    public static final String HEADER_ARG = "header";
    public static final String GAMES_ARG = "allGames";
    public static final String DISPLAYS_DLC_MODE = "displaysDLC";
    /**
     * A list of all games.
     */
    private List<Game> allGames;
    /**
     * A map that maps each XRef of a game to its DLC.
     */
    private Map<String, Set<Game>> dlcGames = new HashMap<String, Set<Game>>();
    /**
     * The games that are actually displayed. Means if a game has a DLC only the main
     * game will be displayed.
     */
    private List<Game> displayedGames = new ArrayList<Game>();
    /**
     * A flag if the fragment should display DLC as a normal item.
     */
    private boolean displaysDLC;
    /**
     * The text that shall be displayed as a header. If it is empty no header will be shown.
     */
    private String headerText;
    private OnFragmentInteractionListener mListener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDividerHeight(5);
        Bundle args = getArguments();
        displaysDLC = args.getBoolean(DISPLAYS_DLC_MODE, false);
        allGames = (List<Game>) args.getSerializable(GAMES_ARG);
        if (allGames == null) {
            allGames = new ArrayList<Game>();
        }
        displayedGames.clear();
        if (!displaysDLC) {
            calculateDisplayedGames();
        } else {
            displayedGames.addAll(allGames);
        }
        headerText = args.getString(HEADER_ARG, "");

        if (!headerText.isEmpty()) {
            TextView textView = new TextView(getActivity());
            textView.setText(headerText);
            getListView().addHeaderView(textView);
        }

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // headerView has index 0, so decrease position by 1 to get the correct game
                int count = getListView().getHeaderViewsCount();
                Game clickedGame = displayedGames.get(position - count);
                String xRef = clickedGame.getXref();
                if (dlcGames.containsKey(xRef) && !displaysDLC) {
                    ArrayList<Game> games = new ArrayList<Game>(dlcGames.get(xRef));
                    Game baseGame = getGameByXRef(xRef);
                    games.add(baseGame);
                    mListener.displayDLCGames(baseGame, games);
                } else {
                    mListener.onGameClick(clickedGame, view);
                }
            }
        });

        loadHosts();
    }

    public void setAllGames(List<Game> allGames) {
        this.allGames = allGames;
        displayedGames.clear();
        displayedGames.addAll(allGames);
        loadHosts();
    }

    public void removeGame(Game game) {
        for (Game g : new ArrayList<Game>(allGames)) {
            if (g.getName().equals(game.getName())) {
                allGames.remove(g);
                displayedGames.remove(g);
                loadHosts();
                break;
            }
        }
    }

    public void addGame(Game game) {
        boolean add = true;
        for (Game g : new ArrayList<Game>(allGames)) {
            if (g.getName().equals(game.getName())) {
                add = false;
            }
        }
        if (add) {
            allGames.add(game);
            Collections.sort(allGames);
            displayedGames.clear();
            if (!displaysDLC) {
                calculateDisplayedGames();
            } else {
                displayedGames.addAll(allGames);
            }
            loadHosts();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private Game getGameByXRef(String xRef) {
        for (Game g : allGames) {
            if (g.getXref().equalsIgnoreCase(xRef)) {
                return g;
            }
        }
        return null;
    }

    private void calculateDisplayedGames() {
        displayedGames.clear();
        dlcGames.clear();
        for (Game g : new ArrayList<Game>(allGames)) {
            String possibleXRef = g.getInfos();
            if (possibleXRef != null && !possibleXRef.isEmpty()) {
                if (!dlcGames.containsKey(possibleXRef)) {
                    dlcGames.put(possibleXRef, new HashSet<Game>());
                }
                dlcGames.get(possibleXRef).add(g);
            } else {
                displayedGames.add(g);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * A game was clicked for details.
         *
         * @param g The clicked game.
         * @param v The header view of the game.
         */
        public void onGameClick(Game g, View v);

        /**
         * A game that has DLC was clicked.
         *
         * @param baseGame The base game of all the DLC.
         * @param games    The games that should be displayed now.
         */
        public void displayDLCGames(Game baseGame, ArrayList<Game> games);
    }

    private void loadHosts() {
        if (getListAdapter() == null) {
            final GameAdapter mAdapter = new GameAdapter();

            this.setListAdapter(mAdapter);
        } else {
            ((GameAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    private class GameAdapter extends BaseAdapter {

        private LayoutInflater inflater = null;

        public GameAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        public int getCount() {
            return displayedGames.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if(position >= getCount()) {
                return null;
            }
            ViewHolder viewHolder;
            if (convertView == null) {
                Typeface titleTypeface = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.TITLE_FONT);
                Typeface textTypeface = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.TEXT_FONT);
                convertView = inflater.inflate(R.layout.game_view, parent, false);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.gameListLogo);
                viewHolder.logoView = imageView;
                TextView textView = (TextView) convertView.findViewById(R.id.gameListName);
                viewHolder.nameView = textView;
                viewHolder.nameView.setTypeface(titleTypeface);
                textView = (TextView) convertView.findViewById(R.id.gameListBronzeTV);
                viewHolder.bronzeView = textView;
                textView = (TextView) convertView.findViewById(R.id.gameListSilverTV);
                viewHolder.silverView = textView;
                textView = (TextView) convertView.findViewById(R.id.gameListGoldTV);
                viewHolder.goldView = textView;
                textView = (TextView) convertView.findViewById(R.id.gameListPlatTV);
                viewHolder.platView = textView;
                textView = (TextView) convertView.findViewById(R.id.gameListPoints);
                viewHolder.pointsView = textView;
                viewHolder.pointsView.setTypeface(textTypeface);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Game game = displayedGames.get(position);
            Bitmap logo = decodeBase64(game.getLogoPath());
            viewHolder.logoView.setImageBitmap(logo);
            viewHolder.nameView.setText(game.getName());
            String text = getResources().getString(R.string.points);
            viewHolder.pointsView.setText(text + " " + game.getPoints());
            viewHolder.platView.setText(game.getPlatin() + "x");
            viewHolder.goldView.setText(game.getGold() + "x");
            viewHolder.silverView.setText(game.getSilver() + "x");
            viewHolder.bronzeView.setText(game.getBronze() + "x");

            return convertView;
        }

        private Bitmap decodeBase64(String encodedImage) {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            return decodedBitmap;
        }

        private class ViewHolder {
            ImageView logoView;
            TextView nameView;
            TextView bronzeView;
            TextView silverView;
            TextView goldView;
            TextView platView;
            TextView pointsView;
        }
    }
}
