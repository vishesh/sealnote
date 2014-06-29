package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.utils.EasyDate;


public class NoteActivity extends Activity implements ColorDialogFragment.ColorChangedListener{
    private Note mNote;
    int mBackgroundColor;

    private class NoteLoadTask extends AsyncTask<Integer, Void, Note> {
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(NoteActivity.this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected Note doInBackground(Integer... integers) {
            DatabaseHandler db = new DatabaseHandler(getBaseContext());
            return db.getNote(integers[0]);
        }

        @Override
        protected void onPostExecute(Note note) {
            super.onPostExecute(note);
            NoteActivity.this.mNote = note;
            mProgressDialog.dismiss();

            NoteActivity.this.setContentView(R.layout.activity_note);
            final TextView editedView = (TextView) findViewById(R.id.note_activity_edited);
            final EditText titleView = (EditText) findViewById(R.id.note_activity_title);
            final EditText textView = (EditText) findViewById(R.id.note_activity_note);

            titleView.setText(mNote.getTitle());
            textView.setText(mNote.getNote());

            EasyDate date = mNote.getEditedDate();
            if (date == null) {
                editedView.setText("Edited " + EasyDate.now().friendly());
            } else {
                editedView.setText("Edited " + mNote.getEditedDate().friendly());
            }

            mBackgroundColor = mNote.getColor();
            onColorChanged(mBackgroundColor);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            textView.requestFocus();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBackgroundColor = -1;

        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("NOTE_ID");
        if (id != -1) {
            new NoteLoadTask().execute(id);
        } else {
            setContentView(R.layout.activity_note);
            final EditText textView = (EditText) findViewById(R.id.note_activity_note);
            textView.requestFocus();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_activity_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save_note:
                saveNote();
                return true;
            case R.id.action_color:
                ColorDialogFragment cdf = new ColorDialogFragment();
                cdf.show(getFragmentManager(), "ColorDialogFragment");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveNote() {
        final DatabaseHandler handler = new DatabaseHandler(this);
        final EditText titleView = (EditText) findViewById(R.id.note_activity_title);
        final EditText textView = (EditText) findViewById(R.id.note_activity_note);
        final String title = titleView.getText().toString();
        final String text = textView.getText().toString();

        if ((title == null && text == null) || (title.equals("") && text.equals(""))) {
            Toast.makeText(this, getResources().getString(R.string.empty_note), Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = this.mNote;

        if (note == null) {
            note = new Note();
        }
        note.setTitle(title);
        note.setNote(text);
        note.setColor(mBackgroundColor);

        if (mNote == null) {
            note.setPosition(-1);
            handler.addNote(note);
        } else {
            handler.updateNote(note);
        }

        Toast.makeText(this, getResources().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
        this.finish();
    }

    public void onColorChanged(int color) {
        mBackgroundColor = color;

        if (color == -1) {
            return;
        }

        View view = findViewById(R.id.note_activity_title).getRootView();

        switch (color) {
            case 0:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color0_noalpha));
                break;
            case 1:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color1_noalpha));
                break;
            case 2:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color2_noalpha));
                break;
            case 3:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color3_noalpha));
                break;
            case 4:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color4_noalpha));
                break;
            case 5:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color5_noalpha));
                break;
            case 6:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color6_noalpha));
                break;
            case 7:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color7_noalpha));
                break;
            case 8:
                view.setBackgroundColor(getResources().getColor(R.color.card_background_color8_noalpha));
                break;
        }
    }
}
