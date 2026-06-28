package com.mad.smartchef.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mad.smartchef.R;
import com.mad.smartchef.utils.LocalHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GenerateRecipeActivity extends AppCompatActivity {

    private static final String TAG = "GenerateRecipeActivity";

    private TextView recipeTitleText, recipePrepTimeText, recipeIngredientsText, recipeStepsText;
    private View resultContainer;
    private ProgressBar progressBar;
    private MaterialButton generateButton;
    private Spinner cuisineSpinner, langSpinner;
    private List<String> ingredients;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_recipe);

        // Bind views
        resultContainer = findViewById(R.id.resultContainer);
        recipeTitleText = findViewById(R.id.recipeTitleText);
        recipePrepTimeText = findViewById(R.id.recipePrepTimeText);
        recipeIngredientsText = findViewById(R.id.recipeIngredientsText);
        recipeStepsText = findViewById(R.id.recipeStepsText);
        progressBar = findViewById(R.id.progressBar);
        generateButton = findViewById(R.id.generateButton);
        cuisineSpinner = findViewById(R.id.cuisineSpinner);
        langSpinner = findViewById(R.id.langSpinner);

        // Spinners are already populated via android:entries in the XML layout.
        // Re-binding adapters here in Java was redundant (and a source of confusion) — removed.

        String ingredientsString = getIntent().getStringExtra("ingredients");
        if (ingredientsString != null && !ingredientsString.isEmpty()) {
            ingredients = Arrays.asList(ingredientsString.split(","));
        } else {
            ingredients = Arrays.asList("chicken", "tomato", "onion");
        }

        generateButton.setOnClickListener(v -> generateRecipe());
    }

    private void generateRecipe() {
        progressBar.setVisibility(View.VISIBLE);
        resultContainer.setVisibility(View.GONE);
        generateButton.setEnabled(false);

        String selectedCuisine = cuisineSpinner.getSelectedItem().toString();
        String selectedLang = langSpinner.getSelectedItem().toString();
        String langInstruction = selectedLang.equals("Urdu")
                ? "Return all text values (title, ingredients, steps, prep_time) in Urdu language and script."
                : "Return the recipe in English language.";

        String prompt = "Create a " + selectedCuisine + " recipe using these ingredients: "
                + TextUtils.join(", ", ingredients) + ". "
                + langInstruction
                + " Return the recipe in JSON format with these exact fields: "
                + "'title' (string), 'ingredients' (array of strings), "
                + "'steps' (array of strings), and 'prep_time' (string). "
                + "Do not include any other text outside the JSON.";

        try {
            GenerativeModel gm = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.5-flash");

            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Executor executor = Executors.newSingleThreadExecutor();

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String rawText = result.getText();
                    if (rawText == null || rawText.isEmpty()) {
                        showFallback("Empty response from AI.");
                        return;
                    }

                    String cleanText = rawText;
                    int start = cleanText.indexOf("{");
                    int end = cleanText.lastIndexOf("}");
                    if (start >= 0 && end > start) {
                        cleanText = cleanText.substring(start, end + 1);
                    }

                    try {
                        JSONObject json = new JSONObject(cleanText);
                        String title = json.optString("title", "AI Recipe");
                        String prepTime = json.optString("prep_time", "N/A");
                        JSONArray ingredientsArray = json.optJSONArray("ingredients");
                        JSONArray stepsArray = json.optJSONArray("steps");

                        StringBuilder ingredientsBuilder = new StringBuilder();
                        if (ingredientsArray != null) {
                            for (int i = 0; i < ingredientsArray.length(); i++) {
                                if (i > 0) ingredientsBuilder.append("\n");
                                ingredientsBuilder.append("• ").append(stripMarkdown(ingredientsArray.getString(i)));
                            }
                        }

                        StringBuilder stepsBuilder = new StringBuilder();
                        if (stepsArray != null) {
                            for (int i = 0; i < stepsArray.length(); i++) {
                                if (i > 0) stepsBuilder.append("\n\n");
                                stepsBuilder.append(i + 1).append(". ").append(stripMarkdown(stepsArray.getString(i)));
                            }
                        }

                        final String finalTitle = stripMarkdown(title);
                        final String finalPrepTime = "Prep time: " + stripMarkdown(prepTime);
                        final String finalIngredients = ingredientsBuilder.toString();
                        final String finalSteps = stepsBuilder.toString();

                        runOnUiThread(() -> {
                            recipeTitleText.setText(finalTitle);
                            recipePrepTimeText.setText(finalPrepTime);
                            recipeIngredientsText.setText(finalIngredients);
                            recipeStepsText.setText(finalSteps);

                            progressBar.setVisibility(View.GONE);
                            resultContainer.setVisibility(View.VISIBLE);
                            generateButton.setEnabled(true);
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "JSON parse error", e);
                        runOnUiThread(() -> {
                            Toast.makeText(GenerateRecipeActivity.this,
                                    "Could not parse recipe. Showing raw text.", Toast.LENGTH_LONG).show();
                            recipeTitleText.setText(R.string.ai_generated_recipe_title);
                            recipePrepTimeText.setText("");
                            recipeIngredientsText.setText("");
                            recipeStepsText.setText(rawText);

                            progressBar.setVisibility(View.GONE);
                            resultContainer.setVisibility(View.VISIBLE);
                            generateButton.setEnabled(true);
                        });
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Gemini request failed", t);
                    showFallback(t.getMessage());
                }
            }, executor);

        } catch (Exception e) {
            Log.e(TAG, "Setup error", e);
            showFallback(e.getMessage());
        }
    }

    /**
     * Removes common Markdown emphasis/heading symbols that Gemini sometimes
     * includes even when plain text is requested, so the UI shows clean text
     * instead of literal *, **, _, or # characters.
     */
    private String stripMarkdown(String input) {
        if (input == null) return "";
        return input
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")  // **bold**
                .replaceAll("\\*(.*?)\\*", "$1")          // *italic*
                .replaceAll("__(.*?)__", "$1")            // __bold__
                .replaceAll("_(.*?)_", "$1")              // _italic_
                .replaceAll("^#+\\s*", "")                 // leading markdown headings
                .trim();
    }

    private void showFallback(String reason) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            if (reason != null && !reason.isEmpty()) {
                Toast.makeText(this, "AI error: " + reason, Toast.LENGTH_LONG).show();
            }

            StringBuilder ingredientsBuilder = new StringBuilder();
            for (String ing : ingredients) {
                ingredientsBuilder.append("• ").append(ing.trim()).append("\n");
            }

            recipeTitleText.setText(R.string.fallback_recipe_title);
            recipePrepTimeText.setText("");
            recipeIngredientsText.setText(ingredientsBuilder.toString().trim());
            recipeStepsText.setText(getString(R.string.fallback_recipe_steps));

            resultContainer.setVisibility(View.VISIBLE);
            generateButton.setEnabled(true);
        });
    }
}