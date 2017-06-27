package com.twistedplane.sealnote.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.TextView;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.AdapterLoader;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.utils.Misc;

/**
 * Main fragment where all cards are listed in a staggered grid
 */
abstract public class SealnoteFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public final static String TAG = "SealnoteFragment";

    /**
     * Adapter used by Staggered Grid View to display note cards
     */
    protected SealnoteAdapter mAdapter;
    protected AdapterView mAdapterView;
    protected Note.Folder mCurrentFolder;
    protected int mCurrentTag;

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
     * Abstract method that should return the adapter for the view
     */
    abstract protected SealnoteAdapter createAdapter();

    /**
     * Abstract method that should load given cursor to adapter and
     * adapter to adapter view
     */
    abstract protected void loadAdapter(Cursor cursor);

    /**
     * The master layout has a View Stub to show all notes which is lazily
     * inflated to view depending upon the currently selected view mode.
     * Inflate ViewStub with the adapter view.
     *
     * @param stub ViewStub to be replaced with adapter view
     * @return AdapterView that replaced the stub
     */
    abstract protected AdapterView inflateAdapterView(ViewStub stub);

    /**
     * Called when the fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating SealNote fragment...");

        if (!Misc.isPasswordLoaded()){
            Misc.startPasswordActivity(getActivity());
            return;
        }

        // Get folder active in activity
        String folder = getArguments().getString("SN_FOLDER", Note.Folder.FOLDER_LIVE.name());
        int tagid = getArguments().getInt("SN_TAGID", -1);
        mCurrentFolder = Note.Folder.valueOf(folder);
        mCurrentTag = tagid;

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

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                Log.d(TAG, "Data set invalidated");
                if (isRemoving() || isDetached() || !isVisible()) {
                    return;
                }
                setFolder(mCurrentFolder, mCurrentTag);
            }
        });
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
        mAdapterView = inflateAdapterView((ViewStub) fragmentView.findViewById(R.id.notes_view));

        getLoaderManager().initLoader(0, null, this);

        return fragmentView;
    }

    /**
     * When coming back from foreground check if we are finishing and reload
     * the adapter
     */
    @Override
    public void onResume() {
        super.onResume();

        if (getActivity().isFinishing()) {
            return;
        }

        Log.d(TAG, "Reloading adapter during fragment resume. folder = " + mCurrentFolder);
        if (mCurrentFolder == Note.Folder.FOLDER_NONE) {
            setFolder(Note.Folder.FOLDER_LIVE, -1);
        } else {
            setFolder(mCurrentFolder, mCurrentTag);
        }
    }

    /**
     * Sets folder view of current fragment
     */
    public void setFolder(Note.Folder folder, int tagid) {
        Log.d(TAG, "Switching folder to " + folder);
        mCurrentFolder = folder;
        mCurrentTag = tagid;
        mAdapter.setFolder(folder, tagid);
        getLoaderManager().restartLoader(0, null, this);
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
            mEmptyGeneric.setText(
                    String.format(
                            getResources().getString(R.string.folder_is_empty),
                            getActivity().getActionBar().getTitle())
            );
        }

        final Drawable actionBarBg;
        switch (mCurrentFolder) {
            case FOLDER_LIVE:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background);
                break;
            case FOLDER_ARCHIVE:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background_archive);
                break;
            case FOLDER_TRASH:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background_trash);
                break;
            case FOLDER_TAG:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background_tag);
                break;
            default:
                actionBarBg = getResources().getDrawable(R.drawable.ab_background);
                break;
        }

        getActivity().getActionBar().setBackgroundDrawable(actionBarBg);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        layoutProgressHeader.setVisibility(View.VISIBLE);
        return new AdapterLoader(getActivity(), mCurrentFolder, mCurrentTag);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "Got result form loader : " + cursor.getCount());
        loadAdapter(cursor);
        layoutProgressHeader.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.clearCursor();
        layoutProgressHeader.setVisibility(View.VISIBLE);
    }
}
