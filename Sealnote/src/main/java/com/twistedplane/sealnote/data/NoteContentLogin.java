package com.twistedplane.sealnote.data;


import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * Implementation of NoteContent for Note.Type = LOGIN.
 */
public class NoteContentLogin extends NoteContent {
    public final static String TAG = "NoteContentLogin";

    private String mUrl;
    private String mLogin;
    private String mPassword;
    private String mAdditionalNote;

    protected NoteContentLogin(String content) {
        super(content);
        mUrl = mLogin = mPassword = mAdditionalNote = "";
    }

    public NoteContentLogin(String url, String login, String password, String additionalNote) {
        super();
        mUrl = url;
        mLogin = login;
        mPassword = password;
        mAdditionalNote = additionalNote;
    }

    @Override
    public String getCardString() {
        //NOTE: We return HTML from this
        if (!mUpdated) {
            update();
        }
        String result = mUrl.trim();
        if (!result.equals("")) {
            result += "<br/>";
        }
        if (!mLogin.equals("")) {
            result += String.format("Login: <b>%s</b>", mLogin);
        }
        return result;
    }

    @Override
    public void update() {
        Scanner scanner = new Scanner(mContent);

        try {
            mUrl = scanner.nextLine();
            mLogin = scanner.nextLine();
            mPassword = scanner.nextLine();
            mAdditionalNote = scanner.nextLine();
            while (scanner.hasNextLine()) {
                mAdditionalNote += "\n";
                mAdditionalNote += scanner.nextLine();
            }
        } catch (NoSuchElementException e) {
            // Log.e(TAG, "Insufficient data in scanner stream. Done reading.");
        }

        mUpdated = true;
    }

    @Override
    public String toString() {
        final String format = "%s\n%s\n%s\n%s";
        return String.format(format,
                mUrl, mLogin, mPassword, mAdditionalNote
        );
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        if (!mUpdated) {
            update();
        }
        return mUrl;
    }

    public void setLogin(String login) {
        mLogin = login;
    }

    public String getLogin() {
        if (!mUpdated) {
            update();
        }
        return mLogin;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getPassword() {
        if (!mUpdated) {
            update();
        }
        return mPassword;
    }

    public void setAdditionalNote(String additionalNote) {
        mAdditionalNote = additionalNote;
    }

    public String getAdditionalNote() {
        if (!mUpdated) {
            update();
        }
        return mAdditionalNote;
    }
}
