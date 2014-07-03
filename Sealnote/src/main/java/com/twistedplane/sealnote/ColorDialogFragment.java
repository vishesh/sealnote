package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.twistedplane.sealnote.utils.Misc;


/**
 * Dialog for selecting color for note.
 */
public class ColorDialogFragment extends DialogFragment {
    private ColorChangedListener mListener;

    /**
     * Callback dispatched when color is changed
     */
    public interface ColorChangedListener {
        public void onColorChanged(int color);
    }

    /**
     * ListAdapter used by ListView in dialog. Adapter contains
     * the color values.
     */
    class ColorAdapter extends ArrayAdapter<String> {
        final Context mContext;
        final int mBackground;

        public ColorAdapter(Context context, int background, int resource, String[] colorNames) {
            super(context, resource, colorNames);
            mContext = context;
            mBackground = background;
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
                view = layoutInflator.inflate(R.layout.color_choose_list_item, null);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] colorNames = getResources().getStringArray(R.array.note_colors_name);
        NoteActivity activity = (NoteActivity) getActivity();

        ColorAdapter colorAdapter = new ColorAdapter(activity, activity.mBackgroundColor,
                R.layout.color_choose_list_item, colorNames);

        builder.setTitle(R.string.msg_pick_color)
               .setAdapter(colorAdapter, new DialogInterface.OnClickListener() {
                   /**
                    * Simply call OnColorChanged callback.
                    */
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       mListener.onColorChanged(i);
                   }
               });
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
