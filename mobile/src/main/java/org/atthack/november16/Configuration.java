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

    public static final String APP_KEY_TTS = "648193c01859a31dfcf76a77fe88f9afb524d32424492f43ebb8f998a285f5060688c71ee16a3bdd62a554602d4d0fe271e207952b36d917eed2af9057a4975d";
    public static final String APP_ID_TTS = "NMDPTRIAL_swbradshaw_gmail_com20161101103007";
    public static final String SERVER_HOST_TTS = "sslsandbox-nmdp.nuancemobility.net";
    public static final String SERVER_PORT_TTS = "443";

    public static final Uri SERVER_URI_TTS = Uri.parse("nmsps://" + APP_ID_TTS + "@" + SERVER_HOST_TTS + ":" + SERVER_PORT_TTS);

    //Only needed if using NLU
    public static final String CONTEXT_TAG = "ATTHACK";

    public static final PcmFormat PCM_FORMAT = new PcmFormat(PcmFormat.SampleFormat.SignedLinear16, 16000, 1);
    public static final String LANGUAGE_CODE = (Configuration.LANGUAGE.contains("!") ? "eng-USA" : Configuration.LANGUAGE);

}
