package org.atthack.november16;

import android.net.Uri;

import com.nuance.speechkit.PcmFormat;

/**
 * Created by SWBRADSH on 11/12/2016.
 */

public class Configuration {

    //All fields are required.
    //Your credentials can be found in your Nuance Developers portal, under "Manage My Apps".
    public static final String APP_KEY = "45b112c3b606841d8c4239e3e0e712ea113b7471f8435e3f803b4627f8d1f73d820900380f84559cc68f7fa676919187cc05f9c4495704c9924af08a1be8c79d";
    public static final String APP_ID = "NMDPPRODUCTION_Hackathon_AttHack_1116_20161112111048";
    public static final String SERVER_HOST = "nmsps.dev.nuance.com";
    public static final String SERVER_PORT = "443";

    public static final String LANGUAGE = "eng-USA";

    public static final Uri SERVER_URI = Uri.parse("nmsps://" + APP_ID + "@" + SERVER_HOST + ":" + SERVER_PORT);

    //Only needed if using NLU
    public static final String CONTEXT_TAG = "ATTHACK";

    public static final PcmFormat PCM_FORMAT = new PcmFormat(PcmFormat.SampleFormat.SignedLinear16, 16000, 1);
    public static final String LANGUAGE_CODE = (Configuration.LANGUAGE.contains("!") ? "eng-USA" : Configuration.LANGUAGE);

}
