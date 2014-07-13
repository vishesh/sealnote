package com.twistedplane.sealnote.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.NoteContent;
import com.twistedplane.sealnote.utils.FontCache;

/**
 * View for simple plain text note content.
 */
public class NoteGenericView extends EditText implements NoteView {
    public NoteGenericView(Context context) {
        super(context);
        init();
    }

    public NoteGenericView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoteGenericView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        setTypeface(FontCache.getFont(getContext(), "RobotoSlab-Regular.ttf")); //LOOK
    }

    @Override
    public NoteContent getNoteContent() {
        return NoteContent.fromString(Note.Type.TYPE_GENERIC, getText().toString());
    }

    @Override
    public void setNoteContent(NoteContent noteContent) {
        setText(noteContent.toString());
    }
}
