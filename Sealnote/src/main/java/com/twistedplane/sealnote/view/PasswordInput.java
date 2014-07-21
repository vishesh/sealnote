package com.twistedplane.sealnote.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.twistedplane.sealnote.R;

public class PasswordInput extends RelativeLayout {
    private EditText    mInput;
    private ProgressBar mProgress;
    private TextView    mStrengthText;
    private View        mContent;
    private boolean     mMeterEnabled = true;

    //TODO: Move to arrays.xml
    public static final String[] STRENGTH = new String[] {
        "",
        "Weak",
        "So-so",
        "Good",
        "Strong"
    };

    private static final int[] PROGRESS_BG = new int[] {
            R.drawable.passwordmeter_default,
            R.drawable.passwordmeter_weak,
            R.drawable.passwordmeter_ok,
            R.drawable.passwordmeter_good,
            R.drawable.passwordmeter_strong
    };

    private static final int[] STRENGTH_BG = new int[] {
            android.R.color.transparent,
            android.R.color.holo_red_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark,
            android.R.color.holo_green_dark
    };

    public PasswordInput(Context context) {
        super(context);
        init();
    }

    public PasswordInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PasswordInput(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_password_meter, this);

        mContent = findViewById(R.id.password_meter_content);
        mInput = (EditText) findViewById(R.id.password_meter_input);
        mProgress = (ProgressBar) findViewById(R.id.password_meter_progress);
        mStrengthText = (TextView) findViewById(R.id.password_meter_text);

        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateMeter();
            }
        });

        mProgress.setMax(4);

        updateMeter();
        setMeterEnabled(mMeterEnabled);
    }

    public void setText(String text) {
        mInput.setText(text);
    }

    public String getText() {
        return mInput.getText().toString();
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        mInput.setOnEditorActionListener(listener);
    }

    public void setHint(String hint) {
        mInput.setHint(hint);
    }

    private void updateMeter() {
        if (mMeterEnabled) {
            // calculate password strength and set level
        }
    }

    public void setLevel(int level) {
        if (level < 0 || level > 4) {
            throw new IllegalArgumentException("Level can be only [0, 4]. Given = " + level);
        }

        mProgress.setProgress(level);
        mStrengthText.setText(STRENGTH[level]);

        mProgress.setProgressDrawable(getResources().getDrawable(PROGRESS_BG[level]));
        mStrengthText.setTextColor(getResources().getColor(STRENGTH_BG[level]));
    }

    public void setMeterEnabled(boolean value) {
        mMeterEnabled = value;
        mContent.setVisibility(value ? VISIBLE : GONE);
    }
}
