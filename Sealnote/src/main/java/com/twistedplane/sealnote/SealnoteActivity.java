package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.utils.FontCache;
import com.twistedplane.sealnote.utils.Misc;
import com.twistedplane.sealnote.utils.TimeoutHandler;
import com.twistedplane.sealnote.views.SealnoteCardGridStaggeredView;

//FIXME: Clean up code and update flag on settings changed.

/**
 * Main activity where all cards are listed in a staggered grid
 */
public class SealnoteActivity extends Activity {
    public final static String TAG = "SealnoteActivity";


    /**
     * Adapter used by Staggered Grid View to display note cards
     */
    private SealnoteAdapter mAdapter = new SealnoteAdapter(this, null);
    private SealnoteCardGridStaggeredView mNoteListView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Note.Folder mCurrentFolder;

    /**
     * FIXME: Hacks
     */
    private Note.Folder mLastFolder;
    private boolean mRefreshRequired = false;

    /**
     * View to show when AdapterLoadTask is running. Show activity
     * progress circle
     */
    private View layoutProgressHeader;

    /**
     * View shown where is no cards available.
     */
    private View mEmptyGridLayout;

    /**
     * TextView show when no cards is available in Trash or Archive
     */
    private TextView mEmptyGeneric;

    /**
     * If a task is already executing to load adapter
     */
    private boolean mAdapterLoading = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        Misc.secureWindow(this);

        mNoteListView = (SealnoteCardGridStaggeredView) findViewById(R.id.main_note_grid);
        mEmptyGridLayout = findViewById(R.id.layout_empty_grid);
        mEmptyGeneric = (TextView) findViewById(R.id.empty_folder_generic);
        layoutProgressHeader = findViewById(R.id.layoutHeaderProgress);

        if (SealnoteApplication.getDatabase().getPassword() == null) {
            // onResume will follow up which will start PasswordActivity and setup database password
            return;
        }

        /**
         * Called whenever there is change in dataset. Any future changes
         * will call this
         */
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onAdapterDataSetChanged();
            }
        });

        int currentFolderInt = 0;

        if (savedInstanceState != null) {
            currentFolderInt = savedInstanceState.getInt("FOLDER", 0);
        }

        switch (currentFolderInt) {
            case 0:
                mCurrentFolder = Note.Folder.FOLDER_LIVE;
                break;
            case 1:
                mCurrentFolder = Note.Folder.FOLDER_ARCHIVE;
                break;
            case 2:
                mCurrentFolder = Note.Folder.FOLDER_TRASH;
                break;
            default:
                mCurrentFolder = Note.Folder.FOLDER_LIVE;
                break;
        }
        mRefreshRequired = true;

        initNavigationDrawer();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(getResources().getStringArray(R.array.navigation_drawer)[currentFolderInt]);
    }

    /**
     * Set up drawer layout and listeners
     */
    private void initNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();

                if (!mAdapterLoading && mRefreshRequired) {
                    new AdapterLoadTask(mCurrentFolder).execute();
                    mRefreshRequired = false;
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        String[] itemStrings = getResources().getStringArray(R.array.navigation_drawer);
        TypedArray itemIcons = getResources().obtainTypedArray(R.array.navigation_drawer_icons);

        final ListView drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        drawerList.setAdapter(new NavigationDrawerAdapter(
                this,
                R.layout.drawer_list_item,
                itemStrings,
                itemIcons
        ));

        // Set first item selected and bold
        drawerList.setItemChecked(0, true);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            int oldChecked = 0;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (pos == 0) {
                    // FOLDER_LIVE
                    mCurrentFolder = Note.Folder.FOLDER_LIVE;
                } else if (pos == 1) {
                    // FOLDER_ARCHIVE
                    mCurrentFolder = Note.Folder.FOLDER_ARCHIVE;
                } else if (pos == 2) {
                    // FOLDER_TRASH
                    mCurrentFolder = Note.Folder.FOLDER_TRASH;
                }

                // FIXME: Hack used to check if we should refresh when drawer is closed
                mRefreshRequired = (mCurrentFolder != mLastFolder);
                mLastFolder = mCurrentFolder;

                drawerList.setItemChecked(pos, true);
                getActionBar().setTitle(getResources().getStringArray(R.array.navigation_drawer)[pos]);

                mDrawerLayout.closeDrawer(drawerList);
                ((NavigationDrawerAdapter) adapterView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * When coming back from foreground check if timeout has expired and if
     * so load logout to password activity. Otherwise reset the timeout status.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (TimeoutHandler.instance().resume(this)) {
            Log.d(TAG, "Timed out! Going to password activity");
            return;
        }

        if (!mAdapterLoading) {
            new AdapterLoadTask(mCurrentFolder).execute();
        }

        // preference may have changed, so do it again. Happens when coming
        // back from settings activity
        Misc.secureWindow(this);
    }

    /**
     * Schedules a delayed callback after configured password expiry timeout.
     */
    @Override
    public void onPause() {
        super.onPause();
        mAdapter.clearCursor();
        TimeoutHandler.instance().pause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Save state for activity recreation
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save current folder in use
        switch (mCurrentFolder) {
            case FOLDER_LIVE:
                outState.putInt("FOLDER", 0);
                break;
            case FOLDER_ARCHIVE:
                outState.putInt("FOLDER", 1);
                break;
            case FOLDER_TRASH:
                outState.putInt("FOLDER", 2);
                break;
        }
    }

    /**
     * Inflate the ActionBar menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        MenuItem addMenuItem = menu.findItem(R.id.action_new_note);
        addMenuItem.setVisible(mCurrentFolder == Note.Folder.FOLDER_LIVE);
        return result;
    }

    /**
     * An item is selected from ActionBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new_note:
                onCreateNoteClick(null);
                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_logout:
                TimeoutHandler.instance().expire(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Load adapter to card grid view. Reload data from database. Also setup animations.
     */
    private void loadAdapter() {
        setAnimationAdapter();

        // get fresh data and swap
        mNoteListView.requestLayout();
        mNoteListView.invalidate();
    }

    /**
     * Set animation adapter for card grid view and make it card grid's external adapter.
     */
    private void setAnimationAdapter() {
        AnimationAdapter animCardArrayAdapter = new ScaleInAnimationAdapter(mAdapter);

        animCardArrayAdapter.setAnimationDurationMillis(1000);
        animCardArrayAdapter.setAnimationDelayMillis(500);

        animCardArrayAdapter.setAbsListView(mNoteListView);
        mNoteListView.setExternalAdapter(animCardArrayAdapter, mAdapter);
    }

    /**
     * Create and show about dialog
     */
    private void showAboutDialog() {
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    /**
     * Start settings activity
     */
    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    /**
     * Callback when dataset in card grid's adapter is changed.
     */
    private void onAdapterDataSetChanged() {
        if (mAdapter.getCount() > 0) {
            mEmptyGridLayout.setVisibility(View.GONE);
            mEmptyGeneric.setVisibility(View.GONE);
        } else if (mCurrentFolder == Note.Folder.FOLDER_LIVE) {
            mEmptyGridLayout.setVisibility(View.VISIBLE);
            mEmptyGeneric.setVisibility(View.GONE);
        } else {
            mEmptyGridLayout.setVisibility(View.GONE);
            mEmptyGeneric.setVisibility(View.VISIBLE);
            mEmptyGeneric.setText(getActionBar().getTitle() + " is empty!");
        }

        // No cursor set reload
        if (mAdapter.getCursor() == null) {
            new AdapterLoadTask(mCurrentFolder).execute();
        }

    }

    /**
     * Called when any create new button/action is clicked.
     */
    public void onCreateNoteClick(View view) {
        NoteActivity.startForNoteId(SealnoteActivity.this, -1);
    }

    /**
     * Asynchronous Task to load adapter.
     *
     * Hide the grid view in activity and shows layoutProgressHeader.
     */
    private class AdapterLoadTask extends AsyncTask<Void, Void, Cursor> {
        private Note.Folder currentFolder;

        AdapterLoadTask(Note.Folder currentFolder) {
            super();
            this.currentFolder =  currentFolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdapterLoading = true;
            mAdapter.clearCursor();
            mAdapter.setCurrentFolder(currentFolder);

            // Before starting background task, update visibility of views
            layoutProgressHeader.setVisibility(View.VISIBLE);
        }

        /**
         * Loads database, get cursor to all notes given folder in database
         * and return a new cursor
         *
         * @return  Adapter object containing all notes
         */
        @Override
        protected Cursor doInBackground(Void... voids) {
            final DatabaseHandler db = SealnoteApplication.getDatabase();
            final Cursor cursor;

            if (currentFolder == Note.Folder.FOLDER_LIVE) {
                cursor = db.getNotesCursor();
            } else if (currentFolder == Note.Folder.FOLDER_ARCHIVE) {
                cursor = db.getArchivedNotesCursor();
            } else if (currentFolder == Note.Folder.FOLDER_TRASH) {
                cursor = db.getDeletedNotesCursor();
            } else {
                cursor = null;
            }

            return cursor;
        }

        /**
         * Takes the result of task ie. adapter and load it to the view.
         * Revert back the visibilities of views.
         *
         * @param cursor   Result containing cursor to notes
         */
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mAdapter.changeCursor(cursor);

            // Make the progress view gone and card grid visible
            layoutProgressHeader.setVisibility(View.GONE);

            SealnoteActivity.this.loadAdapter();
            mAdapterLoading = false;
        }
    }

    /**
     * Adapter for Navigation Drawer list containing folder names
     */
    private class NavigationDrawerAdapter extends ArrayAdapter<String> {
        private LayoutInflater mInflater;
        private String[] mStrings;
        private TypedArray mIcons;
        private int mViewResourceId;

        public NavigationDrawerAdapter(Context ctx, int viewResourceId, String[] strings, TypedArray icons) {
            super(ctx, viewResourceId, strings);
            mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mStrings = strings;
            mIcons = icons;
            mViewResourceId = viewResourceId;
        }

        @Override
        public int getCount() {
            return mStrings.length;
        }

        @Override
        public String getItem(int position) {
            return mStrings[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(mViewResourceId, null);
            }

            ImageView iv = (ImageView) view.findViewById(R.id.item_icon);
            iv.setImageDrawable(mIcons.getDrawable(position));

            TextView tv = (TextView) view.findViewById(R.id.text1);
            tv.setText(mStrings[position]);

            if (convertView == null) {
                tv.setTypeface(FontCache.getFont(getContext(), "RobotoCondensed-Regular.ttf"), Typeface.NORMAL);
            }

            return view;
        }
    }
}
