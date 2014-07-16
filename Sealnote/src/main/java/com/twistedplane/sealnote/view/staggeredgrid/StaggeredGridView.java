package com.twistedplane.sealnote.view.staggeredgrid;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListAdapter;
import com.twistedplane.sealnote.data.CardGridStaggeredCursorAdapter;
import com.twistedplane.sealnote.utils.PreferenceHandler;
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
        updateGridColumnCount();
    }

    public StaggeredGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateGridColumnCount();
    }

    public StaggeredGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        updateGridColumnCount();
    }

    /**
     * NOTE:
     * Also restore the column counts. Somehow seems to fix the bug
     * where the items tend have same x values while different columns
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        updateGridColumnCount();
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

    /**
     * Update column count as per current orientation of device and SealNote
     * NoteListTypeView preference value.
     *
     * Currently this class serves for both Tiles and Single Column view.
     */
    public void updateGridColumnCount() {
        if (PreferenceHandler.getNoteListViewType(getContext()) ==
                PreferenceHandler.NoteListViewType.VIEW_COLUMN) {
            // Single-column mode is enabled
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
