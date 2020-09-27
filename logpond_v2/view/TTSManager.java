package com.infocomm.logpond_v2.view;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import com.infocomm.logpond_v2.R;

import java.util.Locale;

/**
 * Created by DoAsInfinity on 8/1/2017.
 */

public class TTSManager {

    private static TextToSpeech mTextToSpeech;

    public static void sayText(final Context context, final String message) {

        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                try {
                    if (mTextToSpeech != null && status == TextToSpeech.SUCCESS) {

                        float slowSpeechRate = Float.parseFloat(context.getString(R.string.speech_rate));
                        mTextToSpeech.setSpeechRate(slowSpeechRate);
                        mTextToSpeech.setLanguage(Locale.US);
                        mTextToSpeech.speak(message, TextToSpeech.QUEUE_ADD, null);
                    }
                } catch (Exception ex) {
                    System.out.print("Error handling TextToSpeech GCM notification " + ex.getMessage());
                }
            }
        });
        //mTextToSpeech.stop();
        //mTextToSpeech.shutdown();
    }

}
