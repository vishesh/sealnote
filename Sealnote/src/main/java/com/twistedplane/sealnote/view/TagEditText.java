package com.twistedplane.sealnote.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.*;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import com.twistedplane.sealnote.R;

import java.util.HashSet;
import java.util.Set;

/**
 * View for editing tags. Provides auto-complete suggestions
 * from existing tags.
 *
 * TODO: Clean this up. Mostly a hack
 */
public class TagEditText extends MultiAutoCompleteTextView implements View.OnFocusChangeListener {
    private static final String TAG = "TagEditText";

    public TagEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        setSingleLine();
        setTextIsSelectable(false);
        setThreshold(2);

        setTokenizer(new TagTokenizer());
        addTextChangedListener(new TagTextWatcher());

        setOnFocusChangeListener(this);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        String selectedText = (String) selectedItem;
        SpannableString spannable = new SpannableString(selectedText);

        int length = selectedText.length();
        if (length > 0) {
            spannable.setSpan(getBubbleSpan((String)selectedItem), 0, length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            return spannable;
        }
        return super.convertSelectionToString(selectedItem);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selStart != selEnd) {
            return;
        }

        if (selStart > 0 && selStart < length() && getText().charAt(selStart - 1) == ' ') {
            setSelection(selStart - 1);
        }
    }

    /**
     * Update bubbles when we lose focus
     */
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            updateBubbles();
        }
    }

    /**
     * Returns a Set collection containing all tags
     */
    public Set<String> getTagSet() {
        String tags[] = getText().toString().split(" ");
        HashSet<String> tagSet = new HashSet<String>();
        for (String tag : tags) {
            tagSet.add(tag);
        }
        return tagSet;
    }

    /**
     * Takes a Set collection containing tags and load it into view
     */
    public void setTagSet(Set<String> tagSet) {
        StringBuilder builder = new StringBuilder();

        for (String tag : tagSet) {
            builder.append(tag);
            builder.append(" ");
        }

        setText(builder);
    }

    /**
     * Loads a list of tags for suggestions that will be shown for completion
     *
     * @param tags  A set of tags used for filtering and suggesting
     */
    public void loadSuggestions(Set<String> tags) {
        String tagArray[] = tags.toArray(new String[tags.size()]);
        setAdapter(
                new ArrayAdapter<String>(
                        getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        tagArray
                )
        );
    }

    /**
     * Returns an ImageSpan which has tag drawn in a bubble
     */
    private ImageSpan getBubbleSpan(String text) {
        View view = createBubbleTextView(text);
        BitmapDrawable bd = convertViewToDrawable(view);
        bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
        return new ImageSpan(bd);
    }

    /**
     * Returns TextView which contains just one tags. This View will be later
     * drawn on canvas to create ImageSpan for the main text view.
     */
    private TextView createBubbleTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(getTextSize());
        tv.setTextColor(getResources().getColor(R.color.tag_bubble_foreground));

        // Convert PADDING in dp to px and set it to view
        final int PADDING = 6;
        float scale = getResources().getDisplayMetrics().density;
        int dp = (int) (PADDING * scale + 0.5f);
        tv.setPadding(dp, dp/2, dp, dp/2);

        tv.setBackgroundResource(R.drawable.bubble);

        return tv;
    }

    /**
     * Converts given view to a Drawble object
     */
    private BitmapDrawable convertViewToDrawable(View view) {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(
                view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(canvas);

        view.setDrawingCacheEnabled(true);
        Bitmap viewBmp = view.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();

        return new BitmapDrawable(viewBmp);
    }

    /**
     * Create bubble spans and add it to text view. Whole text in
     * this is processed
     */
    private void updateBubbles() {
        String text = getText().toString().replaceAll(" +", " ");
        String tokens[] = text.split(" ");
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        /* Try to get position nearest to current selected tag */

        int selStart = getSelectionEnd();
        int selEnd = getSelectionEnd();

        // Find the current tags starting position
        CharSequence originalText = getText();
        while (selStart > 0 && originalText.charAt(selStart - 1) != ' ') {
            --selStart;
        }

        // Find current tags ending position
        while (selEnd < originalText.length() && originalText.charAt(selEnd) != ' ') {
            ++selEnd;
        }

        String originalToken = originalText.subSequence(selStart, selEnd).toString();

        int finalSelectionPostion = -1; /* We don't know the final selection position yet */
        int done = 0;                   /* Starting position of next token/spannable */

        // Iterate through all tokens i.e. tags, make their bubble spannables
        // and add them to text view at appropriate positions
        for (String token : tokens) {
            ImageSpan imageSpan = getBubbleSpan(token);
            ssb.setSpan(imageSpan, done, done + token.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            done = done + token.length() + 1;
            if (token.equals(originalToken)) {
                // This is it. We found the nearest token to one which
                // was previously selected
                finalSelectionPostion = done;
            }
        }

        setText(ssb);
        if (finalSelectionPostion < 0 || finalSelectionPostion > ssb.length() ||
                originalToken.equals("")) {
            // The second condition is for case when we delete last tag
            // Last condition is when we delete first bubble where its left bubble is nothing
            setSelection(length());
        } else {
            setSelection(finalSelectionPostion);
        }
    }

    /**
     * Deletes bubble/tag at current selection position
     *
     * @return  If a tag was found at current position and removed true, otherwise false
     */
    public boolean deleteSelectedTag() {
        Editable buffer = getText();

        int start = Selection.getSelectionStart(buffer);
        int end = Selection.getSelectionEnd(buffer);

        if (start == end) {
            ImageSpan spans[] = buffer.getSpans(start, end, ImageSpan.class);
            if (spans.length > 0) {
                buffer.replace(buffer.getSpanStart(spans[0]), buffer.getSpanEnd(spans[0]), "");
                buffer.removeSpan(spans[0]);
                return true;
            }
        }
        return false;
    }

    /**
     * This is where we manage all text changes to handle creating, deleting, selecting
     * bubbles/tags. This is batshit crazy code that needs real cleanup.
     */
    class TagTextWatcher implements TextWatcher {
        /**
         * Used when some text is added in b/w bubbles. Since we allow adding
         * new text just at end, we use this parameter to make things right
         */
        boolean     mShouldMove = false;

        /**
         * Number of characters that was added in b/w bubbles that will later
         * be moved to end. This is set along with mShouldMove = true
         */
        int         mMoveCount;

        /**
         * Position form where to start moving text that was added in b/w
         * bubbles that will later be moved to end. This is set along with
         * mShouldMove = true
         */
        int         mStartMovePosition;

        /**
         * Last position of replaced text in Editable just after change
         */
        int         mLastPosition;

        /**
         * Was a bubble just deleted?
         */
        boolean     mBubbleDeleted = false;

        /**
         * Checks if we are adding some text in between bubbles. If so, set flag
         * to move this new replaced text to end
         *
         * We don't allow editing in between bubbles except deleting them
         * as whole.
         *
         * @param s     CharSequence before text change
         * @param start Start position
         * @param count Length of character to be replaced
         * @param after Length of character that will be replaced with
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (after > count && getSelectionEnd() >= 0 && start < s.length() && !mBubbleDeleted &&
                    s.length() > 0) {
                // after > count          : We know text was inserted
                // getSelectionEnd() >= 0 : //CHECK
                // start < s.length()     : We are not adding text to end as in that case
                //                          move of text is not required //CHECK
                // !mBubbleDeleted        : Since we update bubbles on deleting, it triggers this
                // s.length() > 0         : The text is not empty, i.e. not first tag. We should not
                //                          to move stuff when we are creating first tag
                mShouldMove = true;
                mStartMovePosition = start;
                mMoveCount = after;
                setSelection(s.length());
            }
        }

        /**
         * Checks if we deleted something and if so, deleted the tag/bubble in
         * current selection.
         *
         * @param s         CharSequence after text change
         * @param start     Start position of changed text
         * @param before    Length of original subsequence that was replaced
         * @param count     Length of new subsequence that
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // The second condition makes sure that we will delete stuff only if
            // its a bubbled tag. If its currently not bubbled, it won't have spans
            if (before > count &&
                    ((Editable) s).getSpans(start, start + count, ImageSpan.class).length > 0) {
                deleteSelectedTag();
                mBubbleDeleted = true; // Let afterTextChanged know of this deletion
            }
            mLastPosition = start + count; // Save the end position of new replaced text
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mShouldMove) {
                // Some text was added between the tags, move stuff to end
                TagEditText.this.removeTextChangedListener(this);

                CharSequence s2 = s.subSequence(mStartMovePosition, mStartMovePosition + mMoveCount);
                s.delete(mStartMovePosition, mStartMovePosition + mMoveCount);

//                if (s.length() > 0 && s.charAt(s.length() - 1) != ' ') {
//                    s.append(" ");
//                }

                s.append(s2);
                if (s.length() > 0 && s.charAt(s.length() - 1) == ' ') {
                    updateBubbles(); //CHECK
                }

                TagEditText.this.addTextChangedListener(this);
                mShouldMove = false;
                return;
            } else if (!mBubbleDeleted && mLastPosition > 0 && mLastPosition <= s.length() &&
                    s.charAt(mLastPosition - 1) == ' ') {
                // Text has changed, but it was not due to bubble delete.
                // This is state where user has finished writing a tag which
                // is verified by comparing last character with ' '.
                TagEditText.this.removeTextChangedListener(this);
                updateBubbles();
                TagEditText.this.addTextChangedListener(this);
            } else if (mBubbleDeleted) {
                // A bubble was deleted. Update the bubble states
                TagEditText.this.removeTextChangedListener(this);
                updateBubbles();
                TagEditText.this.addTextChangedListener(this);
            }
            mBubbleDeleted = false;
        }
    }

    /**
     * This simple Tokenizer can be used for lists where the items are
     * separated by one or more space
     */
    class TagTokenizer implements Tokenizer {
        /**
         * Find starting position of token at current cursor
         */
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }

            return i;
        }

        /**
         * Find end position of token at current cursor
         */
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        /**
         * After getting token, make any changes if required like
         * appending deliminator
         */
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + " ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }
    }
}