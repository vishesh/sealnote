package com.twistedplane.sealnote.view.simplelist;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.utils.EasyDate;
import com.twistedplane.sealnote.utils.FontCache;
import com.twistedplane.sealnote.utils.Misc;

import java.text.ParseException;

/**
 * Adapter for SimpleListView.
 */
public class SimpleListAdapter extends SimpleCursorAdapter implements SealnoteAdapter {
    public final static String TAG = "SimpleListAdapter";

    public SimpleListAdapter(Context context, Cursor cursor) {
        super(
                context,
                R.layout.simple_list_item_2,
                cursor,
                new String[] {          /* From columns to pick from cursor */
                        DatabaseHandler.COL_TITLE,
                        DatabaseHandler.COL_NOTE_EXTRA,
                        DatabaseHandler.COL_EDITED
                },
                new int[] {             /* Mapped resource ids in layout to columns */
                        R.id.text1,
                        R.id.text2,
                        R.id.text3
                }
        );
    }

    /**
     * Close the cursor held by adapter
     */
    @Override
    public void clearCursor() {
        Cursor cursor = swapCursor(null);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        // Set strip color
        int color = cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COL_COLOR));
        View colorStrip = view.findViewById(R.id.list_item_color);
        colorStrip.setBackgroundColor(Misc.getColorForCode(context, color));
        if (color == 0) {
            colorStrip.setVisibility(View.GONE);
        } else {
            colorStrip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);

        TextView text1 = (TextView) view.findViewById(R.id.text1);
        TextView text2 = (TextView) view.findViewById(R.id.text2);
        TextView text3 = (TextView) view.findViewById(R.id.text3);

        text1.setTypeface(FontCache.getFont(context, "RobotoSlab-Regular.ttf"));
        text2.setTypeface(FontCache.getFont(context, "RobotoSlab-Regular.ttf"));
        text3.setTypeface(FontCache.getFont(context, "RobotoSlab-Regular.ttf"));

        return view;
    }

    @Override
    public void setViewText(TextView v, String text) {
        if (v.getId() == R.id.text3) {
            // Edited data
            try {
                v.setText(EasyDate.fromIsoString(text).friendly());
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing edited date from database.");
                v.setText("");
            }
        } else if (v.getId() == R.id.text2) {
            // Note content
            v.setText(Html.fromHtml(text).toString());
        } else {
            // Title
            v.setText(text);
        }
    }

    /**
     * Sets current folder in view
     */
    @Override
    public void setFolder(Note.Folder folder) {
        // no implementation required
    }

    @Override
    public void startActionMode() {
        // no implementation required
    }
}

