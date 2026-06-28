package com.mad.smartchef.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mad.smartchef.R;
import com.mad.smartchef.activities.CameraActivity;
import com.mad.smartchef.activities.RecipeListActivity;
import com.mad.smartchef.activities.GenerateRecipeActivity;

import java.util.ArrayList;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private static final int REQUEST_CODE_VOICE_INPUT = 1001;
    private static final int REQUEST_CAMERA = 1002;
    private static final String PREFS_NAME = "SmartChefPrefs";
    private static final String KEY_TIP_DISMISSED = "search_tip_dismissed";

    private EditText ingredientInput;
    private Button btnSearch, btnGenerate;
    private ImageButton btnCamera, btnVoice, btnDismissTip;
    private View rootView, tipBanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ingredientInput = rootView.findViewById(R.id.ingredientInput);
        btnSearch = rootView.findViewById(R.id.btnSearch);
        btnGenerate = rootView.findViewById(R.id.btnGenerateRecipe);
        btnCamera = rootView.findViewById(R.id.btnCamera);
        btnVoice = rootView.findViewById(R.id.btnVoice);
        tipBanner = rootView.findViewById(R.id.tipBanner);
        btnDismissTip = rootView.findViewById(R.id.btnDismissTip);

        // ---------- Tip Banner ----------
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        boolean tipDismissed = prefs.getBoolean(KEY_TIP_DISMISSED, false);
        if (tipDismissed && tipBanner != null) {
            tipBanner.setVisibility(View.GONE);
        }
        if (btnDismissTip != null) {
            btnDismissTip.setOnClickListener(v -> {
                if (tipBanner != null) tipBanner.setVisibility(View.GONE);
                prefs.edit().putBoolean(KEY_TIP_DISMISSED, true).apply();
            });
        }

        // ---------- Search Button ----------
        btnSearch.setOnClickListener(v -> performSearch());

        // ---------- Generate with AI Button ----------
        btnGenerate.setOnClickListener(v -> {
            String input = ingredientInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(getContext(), "Enter ingredients first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), GenerateRecipeActivity.class);
            intent.putExtra("ingredients", input);
            startActivity(intent);
        });

        // ---------- Camera Button ----------
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CameraActivity.class);
            startActivityForResult(intent, REQUEST_CAMERA);
        });

        // ---------- Voice Button ----------
        btnVoice.setOnClickListener(v -> startVoiceInput());

        return rootView;
    }

    // ---------- Perform Search ----------
    private void performSearch() {
        String inputText = ingredientInput.getText().toString().trim();
        if (inputText.isEmpty()) {
            Toast.makeText(getContext(), R.string.please_enter_ingredient, Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse ingredients
        String[] splitIngredients = inputText.split(",");
        ArrayList<String> userIngredients = new ArrayList<>();
        for (String ing : splitIngredients) {
            String trimmed = ing.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                userIngredients.add(trimmed);
            }
        }
        if (userIngredients.size() > 10) {
            Toast.makeText(getContext(), R.string.max_10_ingredients, Toast.LENGTH_SHORT).show();
            return;
        }

        // ---------- Launch RecipeListActivity ----------
        // Dietary filters removed permanently — no longer collected or passed.
        Intent intent = new Intent(getContext(), RecipeListActivity.class);
        intent.putExtra("searchTerm", String.join(",", userIngredients));
        startActivity(intent);
    }

    // ---------- Voice Input ----------
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));

        try {
            startActivityForResult(intent, REQUEST_CODE_VOICE_INPUT);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.no_voice_support, Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Handle Results ----------
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ---------- Voice Input Result ----------
        if (requestCode == REQUEST_CODE_VOICE_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                if (ingredientInput.getText().toString().isEmpty()) {
                    ingredientInput.setText(spokenText);
                } else {
                    ingredientInput.setText(ingredientInput.getText().toString() + ", " + spokenText);
                }
            }
        }

        // ---------- Camera Result ----------
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK && data != null) {
            String scannedText = data.getStringExtra("scanned_text");
            if (scannedText != null && !scannedText.isEmpty()) {
                ingredientInput.setText(scannedText);
            }
        }
    }
}