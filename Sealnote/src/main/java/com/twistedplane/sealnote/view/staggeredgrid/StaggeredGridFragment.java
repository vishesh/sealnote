package com.twistedplane.sealnote.view.staggeredgrid;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.fragment.SealnoteFragment;

/**
 * Fragment where all cards are listed in a staggered grid
 */
public class StaggeredGridFragment extends SealnoteFragment {
    public final static String TAG = "StaggeredGridFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    /**
     * Create and return adapter
     */
    @Override
    protected SealnoteAdapter createAdapter() {
        if (mAdapter == null) {
            mAdapter = new StaggeredGridAdapter(getActivity(), null);
        }
        return mAdapter;
    }

    /**
     * Inflate ViewStub with Staggered Grid View
     *
     * @param stub  ViewStub to be replaced with adapter view
     * @return      StaggeredGridView
     */
    @Override
    protected AdapterView inflateAdapterView(ViewStub stub) {
        stub.setLayoutResource(R.layout.staggeredgrid);
        return (AdapterView) stub.inflate();
    }

    /**
     * Load adapter to card grid view. Reload data from database. Also setup animations.
     */
    protected void loadAdapter(Cursor cursor) {
        setAnimationAdapter();
        mAdapter.changeCursor(cursor);
    }

    /**
     * Set animation adapter for card grid view and make it card grid's external adapter.
     */
    private void setAnimationAdapter() {
        StaggeredGridAdapter dataAdapter = (StaggeredGridAdapter) mAdapter;
        StaggeredGridView adapterView = (StaggeredGridView) mAdapterView;

        AnimationAdapter animCardArrayAdapter = new ScaleInAnimationAdapter(dataAdapter);

        animCardArrayAdapter.setAnimationDurationMillis(1000);
        animCardArrayAdapter.setAnimationDelayMillis(500);

        animCardArrayAdapter.setAbsListView(adapterView);
        adapterView.setExternalAdapter(animCardArrayAdapter, dataAdapter);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Shared preferences changed - " + key);
        ((StaggeredGridView) mAdapterView).updateGridColumnCount();
    }
}
