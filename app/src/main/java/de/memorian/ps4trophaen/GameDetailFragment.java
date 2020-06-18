package de.memorian.ps4trophaen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import de.memorian.ps4trophaen.models.Game;
import de.memorian.ps4trophaen.models.Trophy;
import de.memorian.ps4trophaen.storage.GameOverviewDBHelper;
import de.memorian.ps4trophaen.storage.TrophyDBHelper;
import de.memorian.ps4trophaen.util.ExpandableListFragment;
import de.memorian.ps4trophaen.util.TrophyKeys;
import de.memorian.showcaseview.ShowcaseView;
import de.memorian.showcaseview.targets.Target;
import de.memorian.swipedismisslistviewtouchlistener.SwipeDismissListViewTouchListener;

/**
 * Shows a list of trophies.
 *
 *
 * @since 11.10.2014
 */
public class GameDetailFragment extends ExpandableListFragment
        implements SwipeDismissListViewTouchListener.DismissCallbacks {

    private static final String GAME_ARG = "game";
    private static final String IS_FAV = "isFav";
    private final String TUTORIAL_SHOWN = "gameDetailTutorialShown";

    /**
     * Static header view, so we can use the view from GamesListFragment.
     */
    private static View headerView;
    /**
     * The game this fragment is about.
     */
    private Game game;
    private OnFragmentInteractionListener mListener;
    /**
     * Whether this game is set as favorite;
     */
    private boolean isFavorite;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param view The header game view. To not rebuild the view again it is saved
     *             as a static variable here.
     * @param game The game this fragment is about.
     * @return A new instance of fragment GameDetailFragment.
     */
    public static GameDetailFragment newInstance(View view, Game game) {
        GameDetailFragment fragment = new GameDetailFragment();
        Bundle args = new Bundle();
        headerView = view;
        args.putParcelable(GAME_ARG, game);
        fragment.setArguments(args);
        return fragment;
    }

    public GameDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else if (getArguments() != null) {
            game = getArguments().getParcelable(GAME_ARG);
            SharedPreferences sharedPreferences =
                    getActivity().getSharedPreferences(MainActivity.FAVORITES_FILE, Context.MODE_PRIVATE);
            isFavorite = sharedPreferences.getBoolean(game.getName(), false);
        }
        if (game.getTrophies() == null || game.getTrophies().isEmpty()) {
            try {
                List<Trophy> trophies = TrophyDBHelper.getInstance(getActivity()).getTrophies(game.getXref());
                Collections.sort(trophies);
                game.setTrophies(trophies);
            } catch (SQLiteException e) {
                showTableNotFoundDialog();
            }
        }
        formatTrophyGuides();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().getActionBar().setTitle(game.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (headerView == null) { //the case when we are restoring the state
            headerView = inflater.inflate(R.layout.game_view, null, false);
            Typeface titleTypeface = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.TITLE_FONT);
            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.TEXT_FONT);
            ImageView imageView = (ImageView) headerView.findViewById(R.id.gameListLogo);
            Bitmap logo = decodeBase64(game.getLogoPath());
            imageView.setImageBitmap(logo);
            TextView textView = (TextView) headerView.findViewById(R.id.gameListName);
            textView.setText(game.getName());
            textView.setTypeface(titleTypeface);
            textView = (TextView) headerView.findViewById(R.id.gameListBronzeTV);
            textView.setText(game.getBronze() + "x");
            textView = (TextView) headerView.findViewById(R.id.gameListSilverTV);
            textView.setText(game.getSilver() + "x");
            textView = (TextView) headerView.findViewById(R.id.gameListGoldTV);
            textView.setText(game.getGold() + "x");
            textView = (TextView) headerView.findViewById(R.id.gameListPlatTV);
            textView.setText(game.getPlatin() + "x");
            textView = (TextView) headerView.findViewById(R.id.gameListPoints);
            String text = getResources().getString(R.string.points);
            textView.setText(text + " " + game.getPoints());
            textView.setTypeface(tf);
        }

        getActivity().getActionBar().setTitle(game.getName());
        return inflater.inflate(R.layout.fragment_game_detail, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GAME_ARG, game.getName());
        outState.putBoolean(IS_FAV, isFavorite);
    }

    private void showTableNotFoundDialog() {
        Activity mActivity = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.tableNotFoundTitle);
        builder.setMessage(R.string.tableNotFoundText);

        // Set up the buttons
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showTutorial() {
        SharedPreferences sharedPreferences = getActivity().
                getSharedPreferences(MainActivity.SETTINGS_FILE, Context.MODE_PRIVATE);
        if (getExpandableListView().getAdapter().getCount() > 1) {
            if (!sharedPreferences.getBoolean(TUTORIAL_SHOWN, false)) {
                ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
                co.hideOnClickOutside = true;
                ShowcaseView sv = ShowcaseView.insertShowcaseView(new Target() {
                    @Override
                    public Point getPoint() {
                        return new Point();
                    }
                }, getActivity(), R.string.gameDetailSwipeTitle, R.string.gameDetailSwipeText, co);
                sv.setHasNoTarget(true);

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = displaymetrics.heightPixels;
                int width = displaymetrics.widthPixels;
                sv.animateGesture(0, height - 400, width - 100, height - 400); // Gesture code
                sv.show();
                sharedPreferences.edit().putBoolean(TUTORIAL_SHOWN, true).commit();
            }
        }
    }

    private Bitmap decodeBase64(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedBitmap;
    }

    private void onRestoreInstanceState(Bundle savedInstanceState) {
        String gameName = savedInstanceState.getString(GAME_ARG);
        game = GameOverviewDBHelper.getInstance(getActivity()).getGameByName(gameName);
        isFavorite = savedInstanceState.getBoolean(IS_FAV);
    }

    private void updateFavoriteIcon(MenuItem item) {
        if (isFavorite) {
            item.setIcon(R.drawable.fav_on_ic);
        } else {
            item.setIcon(R.drawable.fav_off_ic);
        }
    }

    /**
     * Format every trophy in background. Doing this just when the user clicks on one
     * blocks the UI a little.
     */
    private void formatTrophyGuides() {
        for (final Trophy trophy : game.getTrophies()) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Spannable span = TrophyKeys.getIconedText(getActivity(), trophy.getGuide());
                    trophy.setFormattedGuide(span);
                }
            });
            t.start();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getExpandableListView().setDividerHeight(5);
        getExpandableListView().addHeaderView(headerView);
        DisplayMetrics metrics;
        int width;
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        // this code for adjusting the group indicator into right side of the view
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getExpandableListView().setIndicatorBounds(width - getPixels(30), width - getPixels(10));
        } else {
            getExpandableListView().setIndicatorBoundsRelative(width - getPixels(30), width - getPixels(10));
        }

        // SwipeListener
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(getExpandableListView(), this);
        getExpandableListView().setOnTouchListener(touchListener);
        getExpandableListView().setOnScrollListener(touchListener.makeScrollListener());

        loadHosts();
        showTutorial();
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
            Log.w(GameDetailFragment.class.getName(), activity.getClass().getName()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * dip -> px.
     */
    private int getPixels(int dipValue) {
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                r.getDisplayMetrics());
        return px;
    }

    private void loadHosts() {
        // Check for ExpandableListAdapter object
        if (this.getExpandableListAdapter() == null) {
            //Create ExpandableListAdapter Object
            final TrophyListAdapter mAdapter = new TrophyListAdapter();

            // Set Adapter to ExpandableList Adapter
            this.setListAdapter(mAdapter);
        } else {
            // Refresh ExpandableListView data
            ((TrophyListAdapter) getExpandableListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.game_detail_menu, menu);
        MenuItem favItem = menu.findItem(R.id.gameDetailFav);
        if (favItem != null) {
            updateFavoriteIcon(favItem);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gameDetailFav:
                SharedPreferences.Editor editor =
                        getActivity().getSharedPreferences(MainActivity.FAVORITES_FILE, Context.MODE_PRIVATE).edit();
                if (isFavorite) {
                    isFavorite = false;
                    Toast.makeText(getActivity(), R.string.gameDetailFavOff, Toast.LENGTH_SHORT).show();
                    if (mListener != null) {
                        mListener.onFavoriteDeleted(game);
                    }
                } else {
                    isFavorite = true;
                    Toast.makeText(getActivity(), R.string.gameDetailFavOn, Toast.LENGTH_SHORT).show();
                    if (mListener != null) {
                        mListener.onFavoriteAdded(game);
                    }
                }
                updateFavoriteIcon(item);
                editor.putBoolean(game.getName(), isFavorite).commit();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean canDismiss(int position) {
        if (position == 0) { // header
            return false;
        }
        return true;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions, SwipeDismissListViewTouchListener.Direction direction) {
        for (int position : reverseSortedPositions) {
            position--;
            if (position >= 0 && position < game.getTrophies().size()) {
                Trophy trophy = game.getTrophies().get(position);
                switch (trophy.getPriority()) {
                    case UNIMPORTANT:
                        if (direction == SwipeDismissListViewTouchListener.Direction.RIGHT) {
                            trophy.setPriority(Trophy.Priority.NORMAL);
                        }
                        break;
                    case NORMAL:
                        if (direction == SwipeDismissListViewTouchListener.Direction.RIGHT) {
                            trophy.setPriority(Trophy.Priority.IMPORTANT);
                        } else if (direction == SwipeDismissListViewTouchListener.Direction.LEFT) {
                            trophy.setPriority(Trophy.Priority.UNIMPORTANT);
                        }
                        break;
                    case IMPORTANT:
                        if (direction == SwipeDismissListViewTouchListener.Direction.LEFT) {
                            trophy.setPriority(Trophy.Priority.NORMAL);
                        }
                        break;
                }
                TrophyDBHelper.getInstance(getActivity()).
                        updatePriority(game.getXref(), trophy.getTitle(), trophy.getPriority());
            }
        }
        Collections.sort(game.getTrophies());
        loadHosts();
    }

    @Override
    public boolean canSwipe(int position, SwipeDismissListViewTouchListener.Direction direction) {
        if (position == 0) { // header
            return false;
        }
        position--;
        if (position < 0 || position >= game.getTrophies().size()) {
            return false;
        }
        Trophy trophy = game.getTrophies().get(position);
        switch (trophy.getPriority()) {
            case IMPORTANT:
                if (direction == SwipeDismissListViewTouchListener.Direction.RIGHT) {
                    return false;
                }
                break;
            case UNIMPORTANT:
                if (direction == SwipeDismissListViewTouchListener.Direction.LEFT) {
                    return false;
                }
                break;
        }
        return true;
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
        // TODO: Update argument type and name
        public void onFavoriteDeleted(Game game);

        public void onFavoriteAdded(Game game);
    }

    private class TrophyListAdapter extends BaseExpandableListAdapter {

        private final LayoutInflater inflater;
        Typeface titleTypeface = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.TITLE_FONT);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.TEXT_FONT);
        Typeface hiddenTypeface = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.HIDDEN_TROPHY_FONT);

        public TrophyListAdapter() {
            // Create Layout Inflator
            inflater = LayoutInflater.from(getActivity());
        }

        // This Function used to inflate parent rows view
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parentView) {
            ParentViewHolder holder;
            Trophy trophy = game.getTrophies().get(groupPosition);

            if (convertView == null) {
                holder = new ParentViewHolder();
                convertView = inflater.inflate(R.layout.fragment_game_detail_parent, parentView, false);
                holder.title = (TextView) convertView.findViewById(R.id.gameDetailRowTrophyTitle);
                holder.title.setTypeface(titleTypeface);

                TextView tView = (TextView) convertView.findViewById(R.id.gameDetailRowTrophyText);
                holder.text = tView;
                holder.text.setTypeface(tf);

                tView = (TextView) convertView.findViewById(R.id.gameDetailHiddenTrophy);
                holder.hidden = tView;
                holder.hidden.setTypeface(hiddenTypeface);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.gameDetailRowTrophyView);
                holder.trophyImage = imageView;

                convertView.setTag(holder);
            } else {
                holder = (ParentViewHolder) convertView.getTag();
            }

            evaluatePriority(convertView, trophy.getPriority());
            holder.title.setText(trophy.getTitle());
            holder.text.setText(trophy.getText());
            if (trophy.isSecret()) {
                holder.hidden.setVisibility(View.VISIBLE);
            } else {
                holder.hidden.setVisibility(View.GONE);
            }
            switch (trophy.getType()) {
                case BRONZE:
                    holder.trophyImage.setImageResource(R.drawable.trophy_br);
                    break;
                case SILVER:
                    holder.trophyImage.setImageResource(R.drawable.trophy_slv);
                    break;
                case GOLD:
                    holder.trophyImage.setImageResource(R.drawable.trophy_gld);
                    break;
                case PLATIN:
                    holder.trophyImage.setImageResource(R.drawable.trophy_pt);
                    break;
                default:
            }

            return convertView;
        }

        /**
         * Sets the background color of the given view depending on the priority.
         *
         * @param convertView The view to set the background color.
         * @param priority    The priority of the trophy.
         */
        @TargetApi(android.os.Build.VERSION_CODES.JELLY_BEAN)
        private void evaluatePriority(View convertView, Trophy.Priority priority) {
            int sdk = android.os.Build.VERSION.SDK_INT;
            switch (priority) {
                case IMPORTANT:
                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(getResources().getColor(R.color.important)); // Changes this drawbale to use a single color instead of a gradient
                    gd.setCornerRadius(5);
                    gd.setStroke(1, 0xFF000000);
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        convertView.setBackgroundDrawable(gd);
                    } else {
                        convertView.setBackground(gd);
                    }
                    break;
                case NORMAL:
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        convertView.setBackgroundDrawable(null);
                    } else {
                        convertView.setBackground(null);
                    }
                    break;
                case UNIMPORTANT:
                    gd = new GradientDrawable();
                    gd.setColor(getResources().getColor(R.color.unimportant)); // Changes this drawbale to use a single color instead of a gradient
                    gd.setCornerRadius(5);
                    gd.setStroke(1, 0xFF000000);
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        convertView.setBackgroundDrawable(gd);
                    } else {
                        convertView.setBackground(gd);
                    }
                    break;
            }
        }

        // This Function used to inflate child rows view
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastExercise,
                                 View convertView, ViewGroup parentView) {
            ChildViewHolder holder;
            final Trophy trophy = game.getTrophies().get(groupPosition);
            if (convertView == null) {
                Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), MainActivity.TEXT_FONT);
                holder = new ChildViewHolder();
                convertView = inflater.inflate(R.layout.fragment_game_detail_child, parentView, false);
                holder.text = (TextView) convertView.findViewById(R.id.gameDetailGuide);
                holder.text.setTypeface(tf);
                holder.text.setMovementMethod(LinkMovementMethod.getInstance());
                holder.youTube = (ImageButton) convertView.findViewById(R.id.gameDetailYouTube);
                holder.google = (ImageButton) convertView.findViewById(R.id.gameDetailGoogle);
                holder.report = (ImageButton) convertView.findViewById(R.id.gameDetailReport);
                convertView.setTag(holder);
            } else {
                holder = (ChildViewHolder) convertView.getTag();
            }

            Spannable span = trophy.getFormattedGuide(getActivity());
            holder.text.setText(span);
            holder.youTube.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String youTubePackage = "com.search_web_ic.android.video_search_ic";
                    if (isAppInstalled(youTubePackage)) {
                        String query = game.getName() + " " + trophy.getTitle();
                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        intent.setPackage(youTubePackage);
                        intent.putExtra(SearchManager.QUERY, query);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        showAppNotFoundDialog(youTubePackage);
                    }
                }
            });
            holder.google.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String query = game.getName() + " " + trophy.getTitle();
                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, query);
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        showAppNotFoundDialog("Google-Suche");
                    }
                }
            });
            holder.report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReportErrorDialog reportErrorDialog = new ReportErrorDialog(getActivity());
                    reportErrorDialog.showReportDialogue(game.getName(), trophy.getTitle());
                }
            });

            return convertView;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            Trophy trophy = game.getTrophies().get(groupPosition);
            return trophy;
        }

        //Call when child row clicked
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            Trophy trophy = game.getTrophies().get(groupPosition);
            return trophy;
        }

        @Override
        public int getGroupCount() {
            return game.getTrophies().size();
        }

        //Call when parent row clicked
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public boolean isEmpty() {
            return game.getTrophies().isEmpty();
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        private boolean isAppInstalled(String packageName) {
            Intent mIntent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
            if (mIntent != null) {
                return true;
            } else {
                return false;
            }
        }

        private void showAppNotFoundDialog(String packageName) {
            Activity mActivity = getActivity();
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.appNotFoundTitle);
            String message = getResources().getString(R.string.appNotFoundText, packageName);
            builder.setMessage(message);

            // Set up the buttons
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private class ChildViewHolder {
        TextView text;
        ImageButton youTube;
        ImageButton google;
        ImageButton report;
    }

    private class ParentViewHolder {
        TextView title;
        TextView hidden;
        ImageView trophyImage;
        TextView text;
    }

}
