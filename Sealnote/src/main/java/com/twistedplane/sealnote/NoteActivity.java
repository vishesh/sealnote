package com.twistedplane.sealnote;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.utils.EasyDate;


public class NoteActivity extends Activity implements ColorDialogFragment.ColorChangedListener{
    private Note mNote;
    private int mBackgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("NOTE_ID");

        if (id != -1) {
            DatabaseHandler db = new DatabaseHandler(this);
            this.mNote = db.getNote(id);

            final EditText titleView = (EditText) findViewById(R.id.note_activity_title);
            final EditText textView = (EditText) findViewById(R.id.note_activity_note);
            final TextView editedView = (TextView) findViewById(R.id.note_activity_edited);
            final View root = editedView.getRootView();

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
        }

        mBackgroundColor = -1;
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
        Note note = this.mNote;

        if (note == null) {
            note = new Note();
        }
        note.setTitle(titleView.getText().toString());
        note.setNote(textView.getText().toString());
        note.setColor(mBackgroundColor);

        if (mNote == null) {
            note.setPosition(-1);
            handler.addNote(note);
        } else {
            handler.updateNote(note);
        }

        this.finish();
    }

    public void onColorChanged(int color) {
        if (color == -1) {
            return;
        }

        View view = findViewById(R.id.note_activity_title).getRootView();
        view.setBackgroundColor(color);
        mBackgroundColor = color;
    }
}
