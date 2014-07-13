package com.twistedplane.sealnote.view;

import android.text.TextWatcher;
import com.twistedplane.sealnote.data.NoteContent;

public interface NoteView {
    public NoteContent  getNoteContent();
    public void         setNoteContent(NoteContent noteContent);
    public void         addTextChangedListener(TextWatcher textWatcher);
}
