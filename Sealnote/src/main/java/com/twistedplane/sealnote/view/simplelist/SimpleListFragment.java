package com.twistedplane.sealnote.view.simplelist;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.fragment.SealnoteFragment;

/**
 * Fragment where all cards are listed in a simple List View
 */
public class SimpleListFragment extends SealnoteFragment {
    public final static String TAG = "SimpleListFragment";

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
}

