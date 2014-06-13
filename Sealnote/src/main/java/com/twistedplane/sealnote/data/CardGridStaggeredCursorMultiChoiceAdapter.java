package com.twistedplane.sealnote.data;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import com.twistedplane.sealnote.SealnoteCard;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.multichoice.DefaultOptionMultiChoice;
import it.gmariotti.cardslib.library.internal.multichoice.MultiChoiceAdapter;
import it.gmariotti.cardslib.library.internal.multichoice.MultiChoiceAdapterHelperBase;
import it.gmariotti.cardslib.library.internal.multichoice.OptionMultiChoice;
import it.gmariotti.cardslib.library.view.CardView;

import java.util.ArrayList;

public abstract class CardGridStaggeredCursorMultiChoiceAdapter extends CardGridStaggeredCursorAdapter implements MultiChoiceAdapter, AbsListView.MultiChoiceModeListener {
    private MultiChoiceAdapterHelperBase mHelper = new MultiChoiceAdapterHelperBase(this);
    protected OptionMultiChoice mOptions;

    public CardGridStaggeredCursorMultiChoiceAdapter(Context context, Cursor cursor) {
        this(context, cursor, new DefaultOptionMultiChoice());
    }

    public CardGridStaggeredCursorMultiChoiceAdapter(Context context, Cursor cursor, OptionMultiChoice options) {
        super(context, cursor, 0);
        this.mOptions = options;
        mHelper.setMultiChoiceModeListener(this);
    }

    @Override
    public void setCardGridView(CardGridStaggeredView cardGridView) {
        super.setCardGridView(cardGridView);
        mHelper.setAdapterView(cardGridView);
    }

    @Override
    protected void setupMultichoice(View view, Card mCard, CardView mCardView, long position) {
        super.setupMultichoice(view, mCard, mCardView, position);
        mHelper.setupMultichoice(view,mCard,mCardView,position);
    }

    @Override
    public Card getItem(int position) {
        Card card = super.getItem(position);
        //card.mMultiChoiceEnabled = true;
        ((SealnoteCard) card).setMultichoiceEnabled(); //FIXME
        return card;
    }

    public boolean startActionMode(Activity activity) {
        return mHelper.startActionMode(activity);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return mHelper.onCreateActionMode(mode, menu);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mHelper.onDestroyActionMode(mode);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Log.w("DEBUG", "Item checked bitch!");
        mHelper.onItemCheckedStateChanged(mode, position, id, checked);
    }

    @Override
    public boolean isActionModeStarted() {
        return mHelper.isActionModeStarted();
    }

    protected ArrayList<Card> getSelectedCards() {
        return mHelper.getSelectedCards();
    }

    @Override
    public OptionMultiChoice getOptionMultiChoice() {
        return mOptions;
    }
}

