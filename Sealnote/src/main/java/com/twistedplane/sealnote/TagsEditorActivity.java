package com.twistedplane.sealnote;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.utils.Misc;
import com.twistedplane.sealnote.utils.TimeoutHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity to delete/rename tags
 */
public class TagsEditorActivity extends ListActivity {
    final static public String TAG = "TagsEditorActivity";

    private DatabaseHandler         mHandler;
    private Map<String, Integer>    mTagMap;
    private TagsAdapter             mAdapter;
    private List<String>            mAdapterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Misc.secureWindow(this);

        mHandler = SealnoteApplication.getDatabase();
        mTagMap = mHandler.getAllTags();
        mAdapterData = new ArrayList<String>(mTagMap.keySet());
        mAdapter = new TagsAdapter(this, mAdapterData);
        setListAdapter(mAdapter);

        View view = getLayoutInflater().inflate(R.layout.empty_tags, null);
        ((ViewGroup) getListView().getParent()).addView(view);
        getListView().setEmptyView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TimeoutHandler.instance().resume(this)) {
            Log.d(TAG, "Timed out! Going to password activity");
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TimeoutHandler.instance().pause(this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        final String tag = mAdapterData.get(position);
        final int tagid = mTagMap.get(tag);

        final View view = getLayoutInflater().inflate(R.layout.tag_rename_dialog, null);
        final EditText input = (EditText) view.findViewById(R.id.input_rename);
        input.setText(tag);
        input.setSelection(tag.length());

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.rename_tag)
                .setView(view)
                .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = input.getText().toString().trim();

                        if (newName.indexOf(' ') >= 0) {
                            Toast.makeText(
                                    getBaseContext(),
                                    getResources().getString(R.string.whitespace_not_allowed),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (!newName.equals(tag)) {
                            renameTag(position, newName);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
        builder.show();
    }

    protected void renameTag(int position, String newName) {
        final String oldName = mAdapterData.get(position);
        final int tagid = mTagMap.get(oldName);

        mHandler.renameTag(tagid, newName);
        mAdapterData.set(position, newName);
        mTagMap.remove(oldName);
        mTagMap.put(newName, tagid);

        mAdapter.notifyDataSetChanged();
    }

    protected void onDeleteClick(ListView l, View v, int position, long id) {
        final String tag = mAdapterData.get(position);
        final int tagid = mTagMap.get(tag);

        mHandler.deleteTag(tagid);
        mAdapterData.remove(position);
        mTagMap.remove(tag);

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Adds a delete button, and handles its click. Click to
     * delete is dispatched to onDeleteClick() function in
     * activity with appropriate information
     */
    class TagsAdapter extends ArrayAdapter<String> {
        final private int mViewResourceId = R.layout.tags_edit_list_item;
        private LayoutInflater mInflater;

        TagsAdapter(Context context, List<String> arrayList) {
            super(context, R.layout.tags_edit_list_item, arrayList);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(mViewResourceId, null);
            }

            // Get delete button and update the position it represents in its
            // tag, which will later be used by its click handler to get
            // the runtime position
            ImageButton button = (ImageButton) view.findViewById(R.id.delete_button);
            button.setTag(position);

            TextView tv = (TextView) view.findViewById(R.id.text1);
            tv.setText(getItem(position));

            // We just created a new View, hence add a click listener which
            // will dynamically pick up id, position from view tag and
            // dispatch it to onDeleteClick in activity
            if (convertView == null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer taggedPosition =(Integer) v.getTag();
                        onDeleteClick(getListView(), v, taggedPosition, getItemId(taggedPosition));
                    }
                });
            }

            return view;
        }
    }
}
