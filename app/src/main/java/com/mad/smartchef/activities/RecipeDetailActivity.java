package com.mad.smartchef.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad.smartchef.R;
import com.mad.smartchef.data.AppDatabase;
import com.mad.smartchef.data.RecipeEntity;
import com.mad.smartchef.utils.LocalHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailActivity";

    TextView nameText, instructionsText, ingredientsText;
    ImageView recipeImage;
    ImageButton favoriteButton;
    RatingBar ratingBar;
    Button submitRatingButton;

    FirebaseFirestore db;

    private boolean isFavorite = false;
    private String currentRecipeId, currentName, currentImageUrl, currentInstructions;
    private List<String> currentIngredients;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        nameText = findViewById(R.id.detailName);
        instructionsText = findViewById(R.id.detailInstructions);
        ingredientsText = findViewById(R.id.detailIngredients);
        recipeImage = findViewById(R.id.detailImage);
        favoriteButton = findViewById(R.id.favoriteButton);
        ratingBar = findViewById(R.id.ratingBar);
        submitRatingButton = findViewById(R.id.submitRatingButton);

        favoriteButton.setOnClickListener(v -> toggleFavorite());
        submitRatingButton.setOnClickListener(v -> submitRating());

        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }

            window.setNavigationBarColor(android.graphics.Color.BLACK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int flags = window.getDecorView().getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(flags);
            }
        }

        db = FirebaseFirestore.getInstance();

        String recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, R.string.recipe_id_not_provided, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchRecipeDetails(recipeId);
    }

    private void fetchRecipeDetails(String recipeId) {
        db.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String instructions = documentSnapshot.getString("instructions");
                        String imageUrl = documentSnapshot.getString("imgurl");
                        List<String> ingredients = (List<String>) documentSnapshot.get("ingredients");

                        currentRecipeId = recipeId;
                        currentName = name;
                        currentImageUrl = imageUrl;
                        currentInstructions = instructions;
                        currentIngredients = ingredients;

                        nameText.setText(name != null ? name : getString(R.string.no_name));
                        instructionsText.setText(instructions != null ? instructions : getString(R.string.no_instructions));

                        if (ingredients != null && !ingredients.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (String ing : ingredients) {
                                sb.append("• ").append(ing).append("\n");
                            }
                            ingredientsText.setText(sb.toString());
                        } else {
                            ingredientsText.setText(R.string.no_ingredients_listed);
                        }

                        // Check if favorite
                        new Thread(() -> {
                            int count = AppDatabase.getInstance(this).recipeDao().isFavorite(recipeId);
                            runOnUiThread(() -> {
                                isFavorite = count > 0;
                                updateFavoriteIcon();
                            });
                        }).start();

                        // Load image
                        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                            imageUrl = imageUrl.trim();
                            if (Patterns.WEB_URL.matcher(imageUrl).matches()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(android.R.drawable.ic_menu_gallery)
                                        .error(android.R.drawable.ic_dialog_alert)
                                        .into(recipeImage);
                            } else {
                                Toast.makeText(this, R.string.invalid_image_url, Toast.LENGTH_SHORT).show();
                                recipeImage.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        } else {
                            recipeImage.setImageResource(android.R.drawable.ic_menu_report_image);
                        }

                    } else {
                        Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching recipe", e);
                    Toast.makeText(this, R.string.error_fetching_recipe, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // ---------- Favorite Button Methods ----------
    private void updateFavoriteIcon() {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite);
        }
    }

    private void toggleFavorite() {
        if (isFavorite) {
            new Thread(() -> {
                RecipeEntity entity = new RecipeEntity();
                entity.setId(currentRecipeId);
                AppDatabase.getInstance(this).recipeDao().delete(entity);
                runOnUiThread(() -> {
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                });
            }).start();
        } else {
            new Thread(() -> {
                String ingredientsStr = TextUtils.join(",", currentIngredients);
                RecipeEntity entity = new RecipeEntity(currentRecipeId, currentName,
                        currentImageUrl, currentInstructions, ingredientsStr);
                AppDatabase.getInstance(this).recipeDao().insert(entity);
                runOnUiThread(() -> {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
                });
            }).start();
        }
    }

    // ---------- Rating ----------
    private void submitRating() {
        float rating = ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> update = new HashMap<>();
        update.put("averageRating", rating);
        db.collection("recipes").document(currentRecipeId)
                .update(update)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Rating submitted!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show());
    }
}