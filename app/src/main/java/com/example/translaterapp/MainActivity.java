package com.example.translaterapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Spinner spinner, spinner2;
    TextInputEditText sourceText;
    ImageView mic;
    MaterialButton button;
    TextView translatedtext;
    String[] fromlanguage = {"From", "English", "Arabic", "Afrikaans", "Belarusian", "Bulgarian", "Bengali", "Czech", "Hindi", "Urdu", "French"};
    String[] tolanguage = {"To", "English", "Arabic", "Afrikaans", "Belarusian", "Bulgarian", "Bengali", "Czech", "Hindi", "Urdu", "French"};
    private final static int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromlanguageCode, tolanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.idspinner);
        spinner2 = findViewById(R.id.idspinner2);
        mic = findViewById(R.id.mic);
        button = findViewById(R.id.button);
        sourceText = findViewById(R.id.sourcetext);
        translatedtext = findViewById(R.id.translatedtext);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                fromlanguageCode = getlanguageCode(fromlanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter fromAdaptor = new ArrayAdapter(this, R.layout.spinner_item, fromlanguage);
        fromAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(fromAdaptor);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                tolanguageCode = getlanguageCode(tolanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, tolanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(toAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedtext.setText("");
                if (sourceText.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter your Text To Translate", Toast.LENGTH_SHORT).show();
                } else if (fromlanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please Select Source Language", Toast.LENGTH_SHORT).show();
                } else if (tolanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Enter Select the Language to make translation", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromlanguageCode, tolanguageCode, sourceText.getText().toString());
                }
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceText.setText(result.get(0));
            }
        }
    }

    public void translateText(int fromlanguageCode, int tolanguageCode, String source) {
        translatedtext.setText("Downloading Modal...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromlanguageCode)
                .setTargetLanguage(tolanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedtext.setText("Translating..");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedtext.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to Translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to Download modal"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //  String[] fromlanguage = {"From", "English", "Arabic", "Afrikaans", "Belarusian", "Bulgarian", "Bengali", "Czech", "Hindi", "Urdu", "French"};
     public int getlanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;

            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;

            case "Bulgarian":
                languageCode = FirebaseTranslateLanguage.BG;
                break;

            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;

            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;

            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;

            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;

            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;

            default:
                languageCode = 0;
        }
        return languageCode;
    }
}