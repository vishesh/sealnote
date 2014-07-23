package com.twistedplane.sealnote.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.utils.Misc;


/**
 * Dialog for selecting color for note.
 */
public class ColorDialogFragment extends DialogFragment {
    public final static String TAG = "ColorDialogFragment";

    private ColorChangedListener mListener;
    private int mCurrentBackground;

    /**
     * Callback dispatched when color is changed
     */
    public interface ColorChangedListener {
        public void onColorChanged(int color);
    }

    /**
     * Constructor
     *
     * @param currentBackground    Current background will be ticked in view
     */
    public ColorDialogFragment(int currentBackground) {
        super();
        mCurrentBackground = currentBackground;
    }

    /**
     * ListAdapter used by ListView in dialog. Adapter contains
     * the color values.
     */
    class ColorAdapter extends ArrayAdapter<String> {
        final Context mContext;
        final int mBackground;
        final int mResourceId;

        public ColorAdapter(Context context, int background, int resource, String[] colorNames) {
            super(context, resource, colorNames);
            mContext = context;
            mBackground = background;
            mResourceId = resource;
        }

        /**
         * Get view at given position. Removes the text from ListItem and adds
         * background to that item equal to its value.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater layoutInflator = (LayoutInflater) mContext.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflator.inflate(mResourceId, parent, false);
            } else {
                view = convertView;
            }

            // If this is the color used curretly by note, make Check icon visible
            ImageView checkedView = (ImageView) view.findViewById(R.id.color_picker_checked);
            checkedView.setVisibility(mBackground == position ? View.VISIBLE : View.INVISIBLE);

            // Set background of current list item
            view.setBackgroundColor(Misc.getColorForCode(getContext(), position));

            return view;
        }
    }

    /**
     * Get colors and create AlertDialog using builder. Also sets listeners
     * and callbacks.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] colorNames = getResources().getStringArray(R.array.note_colors_name);

        ColorAdapter colorAdapter = new ColorAdapter(getActivity(), mCurrentBackground,
                R.layout.color_choose_list_item, colorNames);

        // Create and setup Grid View
        GridView gridView = new GridView(getActivity());
        gridView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        gridView.setNumColumns(4);
        gridView.setVerticalSpacing(0);
        gridView.setHorizontalSpacing(0);

        gridView.setAdapter(colorAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Simply call OnColorChanged callback.
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                mListener.onColorChanged(pos);
                ColorDialogFragment.this.dismiss();
            }
        });

        builder.setTitle(R.string.msg_pick_color)
               .setView(gridView);

        return builder.create();
    }

    /**
     * When fragment is attached to activity, use that activity as ColorChangedListener
     *
     * TODO: This is not clean
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (ColorChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ColorChangedListener");
        }
    }
}
