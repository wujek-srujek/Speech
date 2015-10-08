package com.test.rxandroid;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends Activity {

    private View containerView;

    private TextView textView;

    private SpeechRecognizer recognizer;

    private ViewPropertyAnimator textAnimator;

    private Map<String, Runnable> commands;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                String errorString;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorString = "audio";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorString = "client";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorString = "insufficient permissions";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorString = "network";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorString = "network timeout";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorString = "no match";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorString = "recognizer busy";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorString = "server";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorString = "speech timeout";
                        break;
                    default:
                        errorString = "unknown";
                        break;
                }
                Log.e("arse", "error " + errorString);
            }

            @Override
            public void onResults(Bundle results) {
                List<String> strings = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                animateText(strings.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        containerView = findViewById(R.id.container);
        containerView.setOnTouchListener((v, event) -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizer.startListening(intent);

            return true;
        });

        commands = new HashMap<>();
        commands.put("red", () -> animateColor(Color.RED));
        commands.put("rot", commands.get("red"));
        commands.put("czerwony", commands.get("red"));

        commands.put("green", () -> animateColor(Color.GREEN));
        commands.put("grün", commands.get("green"));
        commands.put("zielony", commands.get("green"));

        commands.put("blue", () -> animateColor(Color.BLUE));
        commands.put("blau", commands.get("blue"));
        commands.put("niebieski", commands.get("blue"));

        commands.put("white", () -> animateColor(Color.WHITE));
        commands.put("weiß", commands.get("white"));
        commands.put("biały", commands.get("white"));

        commands.put("vibrate", () -> {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
        });
        commands.put("please me", commands.get("vibrate"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recognizer.destroy();
    }

    private void animateText(String text) {
        if (textAnimator != null) {
            textAnimator.cancel();
        }

        textView.setText(text);

        Runnable action = commands.get(text.toLowerCase(Locale.getDefault()));
        if (action != null) {
            action.run();
        }

        textAnimator = textView
                .animate()
                .alpha(0F)
                .scaleXBy(15F)
                .scaleYBy(15F)
                .setDuration(5000L)
                .setListener(new AnimatorListenerAdapter() {

                    boolean cancelled;

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        cancelled = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        textView.setAlpha(1F);
                        textView.setScaleX(1F);
                        textView.setScaleY(1F);

                        textAnimator = null;

                        if (!cancelled) {
                            textView.setText(null);
                        }
                    }
                });
    }

    private void animateColor(int target) {
        int source = ((ColorDrawable) containerView.getBackground()).getColor();
        ValueAnimator colorAnimation = ValueAnimator
                .ofObject(new ArgbEvaluator(), source, target)
                .setDuration(1000L);

        colorAnimation.addUpdateListener(a -> containerView.setBackgroundColor((Integer) a.getAnimatedValue()));
        colorAnimation.start();
    }
}
