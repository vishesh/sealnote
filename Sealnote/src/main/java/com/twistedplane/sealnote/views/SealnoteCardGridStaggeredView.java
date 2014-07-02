package com.twistedplane.sealnote.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListAdapter;
import com.twistedplane.sealnote.data.CardGridStaggeredCursorAdapter;
import com.twistedplane.sealnote.utils.PreferenceHandler;
import it.gmariotti.cardslib.library.extra.staggeredgrid.internal.CardGridStaggeredArrayAdapter;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;

public class SealnoteCardGridStaggeredView extends CardGridStaggeredView {
    protected CardGridStaggeredCursorAdapter mCursorAdapter;

    public SealnoteCardGridStaggeredView(Context context) {
        super(context);
    }

    public SealnoteCardGridStaggeredView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SealnoteCardGridStaggeredView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAdapter(CardGridStaggeredCursorAdapter adapter) {
        super.setAdapter(adapter);
        adapter.setRowLayoutId(list_card_layout_resourceID);
        adapter.setCardGridView(this);
        mCursorAdapter = adapter;
    }

    public void setExternalAdapter(ListAdapter adapter, CardGridStaggeredCursorAdapter cardCursorAdapter) {
        setAdapter(adapter);

        mCursorAdapter = cardCursorAdapter;
        mCursorAdapter.setCardGridView(this);
        mCursorAdapter.setRowLayoutId(list_card_layout_resourceID);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof CardGridStaggeredArrayAdapter) {
            setAdapter((CardGridStaggeredArrayAdapter)adapter);
        } else if (adapter instanceof CardGridStaggeredCursorAdapter) {
            setAdapter((CardGridStaggeredCursorAdapter)adapter);
        } else {
            Log.w(TAG, "You are using a generic adapter. Pay attention: your adapter has to call cardGridArrayAdapter#getView method.");
            super.setAdapter(adapter);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        updateGridColumnCount();
        super.onLayout(changed, l, t, r, b);
    }

    public void updateGridColumnCount() {
        if (!PreferenceHandler.isMultiColumnGridEnabled(getContext())) {
            setColumnCount(1);
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setColumnCountPortrait(2);
                setColumnCountLandscape(3);
            } else {
                setColumnCountLandscape(3);
                setColumnCountPortrait(2);
            }
        }
    }
}
