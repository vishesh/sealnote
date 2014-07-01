package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.zip.Inflater;


public class ColorDialogFragment extends DialogFragment {
    public interface ColorChangedListener {
        public void onColorChanged(int color);
    }

    ColorChangedListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] colorNames = getResources().getStringArray(R.array.note_colors_name);

        builder.setTitle(R.string.msg_pick_color)
               .setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.color_choose_list_item, colorNames) {
                   Inflater mInflator = new Inflater();

                   @Override
                   public View getView(int position, View convertView, ViewGroup parent) {
                       NoteActivity activity = (NoteActivity) getActivity();

                       View view;

                       if (convertView == null) {
                           view = activity.getLayoutInflater().inflate(R.layout.color_choose_list_item, null);
                       } else {
                           view = convertView;
                       }

                       ImageView checkedView = (ImageView) view.findViewById(R.id.color_picker_checked);
                       checkedView.setVisibility(activity.mBackgroundColor == position ? View.VISIBLE : View.INVISIBLE);

                       switch (position) {
                           case 0:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color0));
                               break;
                           case 1:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color1));
                               break;
                           case 2:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color2));
                               break;
                           case 3:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color3));
                               break;
                           case 4:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color4));
                               break;
                           case 5:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color5));
                               break;
                           case 6:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color6));
                               break;
                           case 7:
                               view.setBackgroundColor(getResources().getColor(R.color.card_background_color7));
                               break;
                       }
                       return view;
                   }
               }, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       mListener.onColorChanged(i);
                   }
               });
        return builder.create();
    }

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
