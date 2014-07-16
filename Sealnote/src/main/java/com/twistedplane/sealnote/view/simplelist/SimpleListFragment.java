package com.twistedplane.sealnote.view.simplelist;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.fragment.SealnoteFragment;

/**
 * Fragment where all cards are listed in a simple List View
 */
public class SimpleListFragment extends SealnoteFragment {
    public final static String TAG = "SimpleListFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListItemListeners();
    }

    private void initListItemListeners() {
        final ListView listView = (ListView) mAdapterView;
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceCallback());
    }

    /**
     * Create and return adapter
     */
    @Override
    protected SealnoteAdapter createAdapter() {
        if (mAdapter == null) {
            mAdapter = new SimpleListAdapter(getActivity(), null);
        }
        return mAdapter;
    }

    /**
     * Inflate ViewStub with Simple List View. See super docstring.
     *
     * @param stub  ViewStub to be replaced with adapter view
     * @return      ListView
     */
    @Override
    protected AdapterView inflateAdapterView(ViewStub stub) {
        stub.setLayoutResource(R.layout.simplelist);
        return (AdapterView) stub.inflate();
    }

    /**
     * Load adapter to card grid view. Reload data from database. Also setup animations.
     */
    protected void loadAdapter(Cursor cursor) {
        setAnimationAdapter();
//        if (mAdapterView.getAdapter() == null) {
//            mAdapterView.setAdapter(mAdapter);
//        }
        mAdapter.changeCursor(cursor);
    }

    /**
     * Set animation adapter for ListView and make its external adapter.
     */
    private void setAnimationAdapter() {
        SimpleListAdapter dataAdapter = (SimpleListAdapter) mAdapter;
        ListView adapterView = (ListView) mAdapterView;

        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(dataAdapter);

        animAdapter.setAnimationDurationMillis(1000);
        animAdapter.setAnimationDelayMillis(500);

        animAdapter.setAbsListView(adapterView);
        adapterView.setAdapter(animAdapter);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Shared preferences changed - " + key);
    }

    private class MultiChoiceCallback extends com.twistedplane.sealnote.internal.MultiChoiceCallback
            implements AbsListView.MultiChoiceModeListener {
        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        protected int getSelectedItemCount() {
            return ((ListView) mAdapterView).getCheckedItemCount();
        }

        @Override
        protected SparseBooleanArray getSelectedItems() {
            SparseBooleanArray sba = new SparseBooleanArray();
            long[] checkedIds = ((ListView) mAdapterView).getCheckedItemIds();
            for (long id : checkedIds) {
                sba.put((int)id, true);
            }
            return sba;
        }

        @Override
        protected Note.Folder getCurrentFolder() {
            return mCurrentFolder;
        }

        @Override
        protected void done() {
            mActionMode = null;
            notifyDataSetChanged();
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
            mActionMode.invalidate();
            notifyDataSetChanged();
        }

        @Override
        protected void notifyDataSetChanged() {
            ((SimpleCursorAdapter) mAdapter).notifyDataSetChanged();
        }

        @Override
        protected void notifyDataSetInvalidated() {
            ((SimpleCursorAdapter) mAdapter).notifyDataSetInvalidated();
        }
    }
}

