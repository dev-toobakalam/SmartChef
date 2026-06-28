package com.mad.smartchef.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mad.smartchef.BuildConfig;
import com.mad.smartchef.R;
import com.mad.smartchef.utils.LocalHelper;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpoonacularDetailActivity extends AppCompatActivity {

    private static final String TAG = "SpoonacularDetail";

    private TextView nameText, instructionsText, ingredientsText;
    private ImageView imageView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        nameText = findViewById(R.id.detailName);
        instructionsText = findViewById(R.id.detailInstructions);
        ingredientsText = findViewById(R.id.detailIngredients);
        imageView = findViewById(R.id.detailImage);

        String recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Recipe ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchSpoonacularRecipe(recipeId);
    }

    private void fetchSpoonacularRecipe(String id) {
        String url = "https://api.spoonacular.com/recipes/" + id + "/information?apiKey="
                + BuildConfig.SPOONACULAR_API_KEY;

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String json = response.body().string();
                Gson gson = new Gson();
                JsonObject obj = gson.fromJson(json, JsonObject.class);

                String title = obj.get("title").getAsString();
                String imageUrl = obj.has("image") ? obj.get("image").getAsString() : null;

                // Instructions
                String instructions = "";
                if (obj.has("instructions") && !obj.get("instructions").isJsonNull()) {
                    instructions = obj.get("instructions").getAsString();
                } else if (obj.has("analyzedInstructions") && obj.get("analyzedInstructions").isJsonArray()) {
                    JsonArray steps = obj.get("analyzedInstructions").getAsJsonArray();
                    if (steps.size() > 0) {
                        JsonArray stepList = steps.get(0).getAsJsonObject().get("steps").getAsJsonArray();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < stepList.size(); i++) {
                            sb.append((i+1)).append(". ")
                                    .append(stepList.get(i).getAsJsonObject().get("step").getAsString())
                                    .append("\n");
                        }
                        instructions = sb.toString();
                    }
                }

                // Ingredients
                StringBuilder ingredientsBuilder = new StringBuilder();
                if (obj.has("extendedIngredients")) {
                    JsonArray ingArray = obj.get("extendedIngredients").getAsJsonArray();
                    for (int i = 0; i < ingArray.size(); i++) {
                        JsonObject ing = ingArray.get(i).getAsJsonObject();
                        String name = ing.get("name").getAsString();
                        String amount = ing.get("amount").getAsString();
                        String unit = ing.get("unit").getAsString();
                        ingredientsBuilder.append("• ").append(name)
                                .append(" – ").append(amount).append(" ").append(unit)
                                .append("\n");
                    }
                }

                final String finalTitle = title;
                final String finalImage = imageUrl;
                final String finalInstructions = instructions.isEmpty() ? "No instructions provided." : instructions;
                final String finalIngredients = ingredientsBuilder.toString().isEmpty() ? "No ingredients listed" : ingredientsBuilder.toString();

                runOnUiThread(() -> {
                    nameText.setText(finalTitle);
                    instructionsText.setText(finalInstructions);
                    ingredientsText.setText(finalIngredients);
                    if (finalImage != null && !finalImage.isEmpty()) {
                        Glide.with(this).load(finalImage)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(imageView);
                    } else {
                        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to load recipe details", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }
}