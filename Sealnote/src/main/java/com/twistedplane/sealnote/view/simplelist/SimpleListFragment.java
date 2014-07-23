package com.twistedplane.sealnote.view.simplelist;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.NoteActivity;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.fragment.SealnoteFragment;

/**
 * Fragment where all cards are listed in a simple List View
 */
public class SimpleListFragment extends SealnoteFragment implements AdapterView.OnItemClickListener {
    public final static String TAG = "SimpleListFragment";
    private final MultiChoiceCallback mMultiChoiceCallback = new MultiChoiceCallback();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListItemListeners();
    }

    private void initListItemListeners() {
        final ListView listView = (ListView) mAdapterView;
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemClickListener(this);
        listView.setMultiChoiceModeListener(mMultiChoiceCallback);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!mMultiChoiceCallback.isActionModeActive()) {
            NoteActivity.startForNoteId(getActivity(), (int) id, null);
        }
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
        Log.d(TAG, "Loading adapter into view");
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

    /**
     * MultiChoice callback for SimpleListView.
     */
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

