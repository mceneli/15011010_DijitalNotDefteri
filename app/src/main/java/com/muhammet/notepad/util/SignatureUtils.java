package com.muhammet.notepad.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

public class SignatureUtils {

    private SignatureUtils() {}

    public enum ReleaseType { PLAY_STORE, AMAZON, F_DROID, UNKNOWN }
    private static final String PLAY_STORE_SIGNATURE = "playstore";
    private static final String AMAZON_SIGNATURE = "amazon";
    private static final String F_DROID_SIGNATURE = "fdroid";

    @SuppressLint("PackageManagerGetSignatures")
    public static ReleaseType getReleaseType(Context context) {
        try {
            Signature playStore = new Signature(Base64.decode(PLAY_STORE_SIGNATURE, Base64.DEFAULT));
            Signature amazon = new Signature(Base64.decode(AMAZON_SIGNATURE, Base64.DEFAULT));
            Signature fDroid = new Signature(Base64.decode(F_DROID_SIGNATURE, Base64.DEFAULT));

            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures) {
                if(signature.equals(playStore))
                    return ReleaseType.PLAY_STORE;

                if(signature.equals(amazon))
                    return ReleaseType.AMAZON;

                if(signature.equals(fDroid))
                    return ReleaseType.F_DROID;
            }
        } catch (Exception e) { }

        return ReleaseType.UNKNOWN;
    }
}