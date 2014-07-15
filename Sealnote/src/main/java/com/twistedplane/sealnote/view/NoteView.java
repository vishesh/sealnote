package com.twistedplane.sealnote.view;

import android.text.TextWatcher;
import com.twistedplane.sealnote.data.NoteContent;

/**
 * Interface for Views for editing note content.
 */
public interface NoteView {
    /**
     * Returns NoteContent object with latest changes.
     */
    public NoteContent  getNoteContent();

    /**
     * Load values into View from give NoteContent object
     */
    public void         setNoteContent(NoteContent noteContent);

    /**
     * Add Text Watcher to all input views within this View
     */
    public void         addTextChangedListener(TextWatcher textWatcher);
}
