package com.twistedplane.sealnote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.twistedplane.sealnote.data.DatabaseHandler;

class TimeoutHandler implements Runnable {
    private Activity mActivity;
    private Handler mHandler = new Handler();
    private boolean mTimedOut = true;
    private static TimeoutHandler mInstance = new TimeoutHandler();

    @Override
    public void run() {
        DatabaseHandler db = new DatabaseHandler(mActivity);
        db.recycle();
        mTimedOut = true;
    }

    public void passwordTimeoutClear() {
        mHandler.removeCallbacks(mInstance, null);
        mTimedOut = false;
    }

    private void passwordTimeoutStart() {
        passwordTimeoutClear();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        int timeout = Integer.parseInt(sharedPrefs.getString("pref_passwordTimeout", "10000")); //FIXME: Get integer
        mHandler.postDelayed(mInstance, timeout);
        mTimedOut = false;
    }

    public boolean resume(Activity activity) {
        mActivity = activity;
        if (mTimedOut == true) {
            Intent intent = new Intent(mActivity, PasswordActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
        } else {
            passwordTimeoutClear();
        }
        return mTimedOut;
    }

    public void expire(Activity activity) {
        mActivity = activity;
        run();
        resume(activity);
    }

    public void pause(Activity activity) {
        mActivity = activity;
        passwordTimeoutStart();
    }

    public static TimeoutHandler instance() {
        return mInstance;
    }
}
