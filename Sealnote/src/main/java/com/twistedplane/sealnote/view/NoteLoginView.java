package com.twistedplane.sealnote.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.NoteContent;
import com.twistedplane.sealnote.data.NoteContentLogin;
import com.twistedplane.sealnote.utils.FontCache;

/**
 * View for editing note content of Note.Type = Login
 */
public class NoteLoginView extends LinearLayout implements NoteView {
    private EditText mUrl;
    private EditText mLogin;
    private EditText mPassword;
    private EditText mNote;

    public NoteLoginView(Context context) {
        super(context);
        init();
    }

    public NoteLoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoteLoginView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.note_type_login_layout, this);

        mUrl = (EditText) findViewById(R.id.note_login_url);
        mLogin = (EditText) findViewById(R.id.note_login_name);
        mPassword = (EditText) findViewById(R.id.note_login_password);
        mNote = (EditText) findViewById(R.id.note_additional_note);

        mNote.setTypeface(FontCache.getFont(getContext(), "RobotoSlab-Regular.ttf")); //LOOK
    }

    @Override
    public NoteContent getNoteContent() {
        return new NoteContentLogin(
                mUrl.getText().toString(),
                mLogin.getText().toString(),
                mPassword.getText().toString(),
                mNote.getText().toString()
        );
    }

    @Override
    public void setNoteContent(NoteContent noteContent) {
        NoteContentLogin noteContentLogin = (NoteContentLogin) noteContent;

        mUrl.setText(noteContentLogin.getUrl());
        mLogin.setText(noteContentLogin.getLogin());
        mPassword.setText(noteContentLogin.getPassword());
        mNote.setText(noteContentLogin.getAdditionalNote());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mUrl.setEnabled(enabled);
        mLogin.setEnabled(enabled);
        mPassword.setEnabled(enabled);
        mNote.setEnabled(enabled);
    }

    @Override
    public void addTextChangedListener(TextWatcher textWatcher) {
        mUrl.addTextChangedListener(textWatcher);
        mLogin.addTextChangedListener(textWatcher);
        mPassword.addTextChangedListener(textWatcher);
        mNote.addTextChangedListener(textWatcher);
    }
}

