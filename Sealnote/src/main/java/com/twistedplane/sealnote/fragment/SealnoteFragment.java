package com.twistedplane.sealnote.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.SealnoteApplication;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;

/**
 * Main fragment where all cards are listed in a staggered grid
 */
abstract public class SealnoteFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public final static String TAG = "SealnoteFragment";

    /**
     * Adapter used by Staggered Grid View to display note cards
     */
    protected SealnoteAdapter mAdapter;
    protected AdapterView mAdapterView;
    protected Note.Folder mCurrentFolder;

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
     * Abstract method that should return the adapter for the view
     */
    abstract protected SealnoteAdapter createAdapter();

    /**
     * Abstract method that should load given cursor to adapter and
     * adapter to adapter view
     */
    abstract protected void loadAdapter(Cursor cursor);

    /**
     * Called when the fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating SealNote fragment...");

        mCurrentFolder = Note.Folder.FOLDER_NONE;
        mAdapter = createAdapter();

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

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

    }

    /**
     * Return the root view of fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_sealnote, container, false);

        mEmptyGridLayout = fragmentView.findViewById(R.id.layout_empty_grid);
        mEmptyGeneric = (TextView) fragmentView.findViewById(R.id.empty_folder_generic);
        layoutProgressHeader = fragmentView.findViewById(R.id.layoutHeaderProgress);
        mAdapterView = (AdapterView) fragmentView.findViewById(R.id.notes_view);

        return fragmentView;
    }

    /**
     * When coming back from foreground check if we are finishing and reload
     * the adapter
     */
    @Override
    public void onResume() {
        super.onResume();

        if (!mAdapterLoading && !getActivity().isFinishing()) {
            Log.d(TAG, "Reloading adapter during fragment resume. folder = " + mCurrentFolder);
            if (mCurrentFolder == Note.Folder.FOLDER_NONE) {
                setFolder(Note.Folder.FOLDER_LIVE);
            } else {
                setFolder(mCurrentFolder);
            }
        }
    }

    /**
     * Fragment going background.
     */
    @Override
    public void onPause() {
        super.onPause();
        mAdapter.clearCursor();
    }

    /**
     * Sets folder view of current fragment
     */
    public void setFolder(Note.Folder folder) {
        Log.d(TAG, "Switching folder to " + folder);

        mCurrentFolder = folder;
        if (!mAdapterLoading) {
            new AdapterLoadTask(mCurrentFolder).execute();
        }
    }

    /**
     * Callback when dataset in card grid's adapter is changed.
     */
    private void onAdapterDataSetChanged() {
        Log.d(TAG, "Adapter dataset changed (" + mAdapter.getCursor() + ")");

        if (mAdapter.getCount() > 0) {
            mEmptyGridLayout.setVisibility(View.GONE);
            mEmptyGeneric.setVisibility(View.GONE);
        } else if (mCurrentFolder == Note.Folder.FOLDER_LIVE) {
            mEmptyGridLayout.setVisibility(View.VISIBLE);
            mEmptyGeneric.setVisibility(View.GONE);
        } else {
            mEmptyGridLayout.setVisibility(View.GONE);
            mEmptyGeneric.setVisibility(View.VISIBLE);
            mEmptyGeneric.setText(getActivity().getActionBar().getTitle() + " is empty!");
        }

        final Drawable actionBarBg;
        switch(mCurrentFolder) {
            case FOLDER_LIVE:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background);
                break;
            case FOLDER_ARCHIVE:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background_archive);
                break;
            case FOLDER_TRASH:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background_trash);
                break;
            default:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background);
                break;
        }

        getActivity().getActionBar().setBackgroundDrawable(actionBarBg);
    }

    /**
     * Asynchronous Task to load adapter.
     *
     * Hide the grid view in fragment and shows layoutProgressHeader.
     */
    private class AdapterLoadTask extends AsyncTask<Void, Void, Cursor> {
        private Note.Folder currentFolder;

        AdapterLoadTask(Note.Folder currentFolder) {
            super();
            this.currentFolder = currentFolder;
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
         */
        @Override
        protected Cursor doInBackground(Void... voids) {
            final DatabaseHandler db = SealnoteApplication.getDatabase();
            final Cursor cursor;

            switch(currentFolder) {
                case FOLDER_LIVE:
                    cursor = db.getNotesCursor();
                    break;
                case FOLDER_ARCHIVE:
                    cursor = db.getArchivedNotesCursor();
                    break;
                case FOLDER_TRASH:
                    cursor = db.getDeletedNotesCursor();
                    break;
                default:
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

            // Make the progress view gone and card grid visible
            layoutProgressHeader.setVisibility(View.GONE);
            loadAdapter(cursor);
            mAdapterLoading = false;
        }
    }
}
