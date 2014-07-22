package com.twistedplane.sealnote.crypto;

import android.util.Log;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quick and dirty class to measure password quality
 */
public class PasswordQuality {
    public final static String TAG = "PasswordQuality";
    private Set<String> mDictionary;
    final private Pattern mDictPattern = Pattern.compile("\\d+|[A-Za-z]+|[^\\d^A-Z^a-z]+");

    private class CharsetCount {
        int upper = 0;
        int lower = 0;
        int digits = 0;
        int symbol = 0;
    }

    private enum DictionaryAttackResultType {
        DICT_EXACT_MATCH,
        DICT_SUBSET_MATCH,
        DICT_NO_MATCH
    }

    private class DictionaryAttackResult {
        DictionaryAttackResultType type;
        CharSequence matched;

        DictionaryAttackResult(DictionaryAttackResultType type, CharSequence matched) {
            this.type = type;
            this.matched = matched;
        }
    }

    public PasswordQuality() {
        mDictionary = new HashSet<String>();
    }

    public void initDictionary(InputStream dict) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dict));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                mDictionary.add(line);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Dictionary not found in asset directory!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int score(CharSequence password) {
        if (password.length() == 0) {
            return 0;
        }

        double entropy = nistEntropy(password);

        if (entropy >= 40) {
            return 4;
        } else if (entropy > 30) {
            return 3;
        } else if (entropy > 25) {
            return 2;
        }

        return 1;
    }

    /**
     * Calculate entropy roughly based on NIST standard
     */
    private double nistEntropy(CharSequence password) {
        double score = 0;
        int length = password.length();

        // First character has 4 bit entropy
        if (length > 0) {
            score += 4;
        }

        // Next seven characters has 2 bit entropy/char
        if (length > 1) {
            score += Math.min(length - 1, 7) * 2;
        }

        // Characters from index 9 to 20 have entropy 1.5bit/char
        if (length > 8) {
            score += Math.min(length - 8, 12) * 1.5;
        }

        // Character from index 21 and above have entropy 1bit/char
        if (length > 20) {
            score += length - 20;
        }

        final CharsetCount counts = computeCharsetCount(password);
        if (counts.upper > 0 && counts.lower > 0 && (counts.digits > 0 || counts.symbol > 0)) {
            score += 6;
        }

        if (length < 20) {
            DictionaryAttackResult result = dictionaryAttack(password);
            switch (result.type) {
                case DICT_EXACT_MATCH:
                    score = 0;
                    break;
                case DICT_SUBSET_MATCH:
                    // Penalize for match
                    score -= Math.log(result.matched.length())/Math.log(2);
                    break;
                case DICT_NO_MATCH:
                    score += 6;
                    break;
            }
        }

        return score;
    }

    private CharsetCount computeCharsetCount(CharSequence password) {
        CharsetCount result = new CharsetCount();

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isUpperCase(ch)) {
                ++result.upper;
            } else if (Character.isLowerCase(ch)) {
                ++result.lower;
            } else if (Character.isDigit(ch)) {
                ++result.digits;
            } else if (Character.isDigit(ch)) {
                ++result.digits;
            } else {
                ++result.symbol;
            }
        }

        return result;
    }

    private DictionaryAttackResult dictionaryAttack(CharSequence _password) {
        String password = _password.toString().toLowerCase();

        if (mDictionary.contains(password)) {
            return new DictionaryAttackResult(DictionaryAttackResultType.DICT_EXACT_MATCH, password);
        }

        Matcher matcher = mDictPattern.matcher(password);
        while (matcher.find()) {
            String group = matcher.group();
            if (mDictionary.contains(group)) {
                return new DictionaryAttackResult(DictionaryAttackResultType.DICT_SUBSET_MATCH, group);
            }
        }

        return new DictionaryAttackResult(DictionaryAttackResultType.DICT_NO_MATCH, null);
    }
}