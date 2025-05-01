package com.ridgebotics.ridgescout.utility;

import android.content.Context;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier;

import java.util.List;

// Class that runs a sentiment analysis model of a string
// This would be very cool if it was useful at all.
public class SentimentAnalysis {
    private static NLClassifier textClassifier;

    public static void init(Context context){
        try {
            textClassifier = NLClassifier.createFromFile(context, "text_classification_v2.tflite");
        } catch (Exception e) {
            AlertManager.error(e);
        }
    }

    public interface resultCallback {
        public void onFinish(float data);
    }

    public static void analyse(String input, resultCallback result){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Category> results = textClassifier.classify(input);

                for(int i = 0; i < results.size(); i++){
                    Category cat = results.get(i);
                    switch (cat.getLabel()){
                        case "Positive":
                            result.onFinish(cat.getScore());
                            break;
                    }
                }
            }
        }).start();
    }

    public static float analyse_sync(String input){
        List<Category> results = textClassifier.classify(input);

        for(int i = 0; i < results.size(); i++){
            Category cat = results.get(i);
            switch (cat.getLabel()){
                case "Positive":
                    return cat.getScore();
            }
        }
        return 0;
    }
}
