package com.twistedplane.sealnote.view.staggeredgrid;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListAdapter;
import com.twistedplane.sealnote.data.CardGridStaggeredCursorAdapter;
import it.gmariotti.cardslib.library.extra.staggeredgrid.internal.CardGridStaggeredArrayAdapter;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;

/**
 * Specialized CardGridStaggeredView class used in SealNote.
 *
 *   + Add support for cursor adapter
 *   + Changes to make column mode change as per latest Sealnote settings
 *     preferences
 */
public class StaggeredGridView extends CardGridStaggeredView {
    public final static String TAG = "StaggeredGridView";

    public StaggeredGridView(Context context) {
        super(context);
    }

    public StaggeredGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StaggeredGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Uses CardGridStaggeredCursorAdapter to avoid dispatch to generic
     * ListAdapter version of this function which doesn't support this
     * kind of adapter.
     */
    public void setAdapter(CardGridStaggeredCursorAdapter adapter) {
        super.setAdapter(adapter);
        adapter.setRowLayoutId(list_card_layout_resourceID);
        adapter.setCardGridView(this);
    }

    /**
     * Uses CardGridStaggeredCursorAdapter to avoid dispatch to generic
     * ListAdapter version of this function which doesn't support this
     * kind of adapter.
     */
    public void setExternalAdapter(ListAdapter adapter, CardGridStaggeredCursorAdapter cardCursorAdapter) {
        setAdapter(adapter);
        cardCursorAdapter.setCardGridView(this);
        cardCursorAdapter.setRowLayoutId(list_card_layout_resourceID);
    }

    /**
     * Add support for CardGridStaggeredCursorAdapter.
     */
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
}
