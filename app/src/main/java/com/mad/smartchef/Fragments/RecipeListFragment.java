package com.mad.smartchef.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mad.smartchef.BuildConfig;
import com.mad.smartchef.R;
import com.mad.smartchef.adapter.RecipeAdapter;
import com.mad.smartchef.models.RecipeModel;
import com.mad.smartchef.activities.RecipeDetailActivity;
import com.mad.smartchef.activities.RecipeMatcher;
import com.mad.smartchef.activities.GenerateRecipeActivity;
import com.mad.smartchef.activities.SpoonacularDetailActivity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecipeListFragment extends Fragment {

    private static final String ARG_SEARCH_TERM = "search_term";
    private static final String ARG_DIETARY_FILTERS = "dietary_filters";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<RecipeModel> recipeList;
    private ProgressBar progressBar;
    private View emptyStateView;  // Will be inflated from result_empty.xml

    private String searchTerm = "";
    private ArrayList<String> dietaryFilters = new ArrayList<>();

    public RecipeListFragment() {}

    public static RecipeListFragment newInstance(String searchTerm, ArrayList<String> dietaryFilters) {
        RecipeListFragment fragment = new RecipeListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putStringArrayList(ARG_DIETARY_FILTERS, dietaryFilters);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        recyclerView = view.findViewById(R.id.recipeRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        recipeList = new ArrayList<>();

        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            if (getContext() != null) {
                boolean isSpoonacular = false;
                try {
                    Long.parseLong(recipe.getId());
                    isSpoonacular = true;
                } catch (NumberFormatException e) {
                    isSpoonacular = false;
                }
                Intent intent;
                if (isSpoonacular) {
                    intent = new Intent(getContext(), SpoonacularDetailActivity.class);
                } else {
                    intent = new Intent(getContext(), RecipeDetailActivity.class);
                }
                intent.putExtra("recipeId", recipe.getId());
                getContext().startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            searchTerm = getArguments().getString(ARG_SEARCH_TERM, "");
            dietaryFilters = getArguments().getStringArrayList(ARG_DIETARY_FILTERS);
        }

        loadRecipes();
        return view;
    }

    private void loadRecipes() {
        progressBar.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(searchTerm) || searchTerm.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter at least one ingredient", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            showEmptyState();
            return;
        }

        String[] inputIngredients = searchTerm.split(",");
        List<String> userIngredients = new ArrayList<>();
        for (String ing : inputIngredients) {
            String trimmed = ing.trim().toLowerCase();
            if (!trimmed.isEmpty() && !userIngredients.contains(trimmed)) {
                userIngredients.add(trimmed);
            }
            if (userIngredients.size() >= 10) break;
        }

        if (userIngredients.isEmpty()) {
            Toast.makeText(getContext(), "Please enter at least one valid ingredient", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            showEmptyState();
            return;
        }

        db.collection("recipes").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();

                    List<QueryDocumentSnapshot> docs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        docs.add(doc);
                    }

                    List<RecipeModel> matchedRecipes = RecipeMatcher.getMatchingRecipes(userIngredients, docs);

                    if (dietaryFilters != null && !dietaryFilters.isEmpty()) {
                        matchedRecipes = applyDietaryFilters(matchedRecipes);
                    }

                    Collections.sort(matchedRecipes, (r1, r2) -> Integer.compare(r2.getMatchCount(), r1.getMatchCount()));

                    if (matchedRecipes.size() > 5) {
                        matchedRecipes = matchedRecipes.subList(0, 5);
                    }

                    recipeList.addAll(matchedRecipes);
                    adapter.updateRecipes(recipeList);

                    progressBar.setVisibility(View.GONE);

                    if (recipeList.isEmpty()) {
                        showEmptyState();
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        if (emptyStateView != null) emptyStateView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load recipes", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private List<RecipeModel> applyDietaryFilters(List<RecipeModel> recipes) {
        if (dietaryFilters == null || dietaryFilters.isEmpty()) return recipes;

        List<RecipeModel> filtered = new ArrayList<>();
        for (RecipeModel r : recipes) {
            ArrayList<String> recipeDietary = r.getDietary();
            if (recipeDietary == null || recipeDietary.isEmpty()) continue;
            if (recipeDietary.contains("all")) {
                filtered.add(r);
                continue;
            }
            for (String filter : dietaryFilters) {
                if (recipeDietary.contains(filter.toLowerCase())) {
                    filtered.add(r);
                    break;
                }
            }
        }
        return filtered;
    }

    // ---------- SAFE EMPTY STATE ----------
    private void showEmptyState() {
        // Hide RecyclerView and ProgressBar
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        // Inflate empty state only once
        if (emptyStateView == null) {
            try {
                emptyStateView = LayoutInflater.from(getContext()).inflate(R.layout.result_empty, null);
                // Get the parent of RecyclerView (which is the root LinearLayout)
                ViewGroup parent = (ViewGroup) recyclerView.getParent();
                if (parent != null) {
                    parent.addView(emptyStateView);
                } else {
                    // Fallback: we can't add it – show error Toast
                    Toast.makeText(getContext(), "Cannot display empty state – parent not found", Toast.LENGTH_SHORT).show();
                    Log.e("RecipeListFragment", "Parent is null – cannot add empty state");
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error showing empty state: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
        }

        // Show empty state
        emptyStateView.setVisibility(View.VISIBLE);

        // Set button listeners
        Button btnSpoon = emptyStateView.findViewById(R.id.btnSpoonacular);
        Button btnGemini = emptyStateView.findViewById(R.id.btnGemini);

        if (btnSpoon == null || btnGemini == null) {
            Toast.makeText(getContext(), "Empty state layout is missing buttons", Toast.LENGTH_SHORT).show();
            return;
        }

        final String ingredients = searchTerm;

        btnSpoon.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            fetchFromSpoonacular(ingredients);
        });

        btnGemini.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), GenerateRecipeActivity.class);
                intent.putExtra("ingredients", ingredients);
                startActivity(intent);
            }
        });
    }

    // ---------- Spoonacular API ----------
    private void fetchFromSpoonacular(String ingredients) {
        String url = "https://api.spoonacular.com/recipes/findByIngredients?apiKey="
                + BuildConfig.SPOONACULAR_API_KEY
                + "&ingredients=" + ingredients.replace(",", "%2C")
                + "&number=5&ignorePantry=true";

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
                Type listType = new TypeToken<List<SpoonacularRecipe>>(){}.getType();
                List<SpoonacularRecipe> spoonRecipes = gson.fromJson(json, listType);

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (spoonRecipes != null && !spoonRecipes.isEmpty()) {
                        recipeList.clear();
                        for (SpoonacularRecipe sr : spoonRecipes) {
                            RecipeModel model = new RecipeModel();
                            model.setId(String.valueOf(sr.id));
                            model.setName(sr.title);
                            model.setImageUrl(sr.image);
                            model.setInstructions("Spoonacular recipe – see details");
                            ArrayList<String> ingList = new ArrayList<>();
                            if (sr.missedIngredients != null) {
                                for (SpoonacularIngredient ing : sr.missedIngredients) {
                                    ingList.add(ing.name);
                                }
                            }
                            if (sr.usedIngredients != null) {
                                for (SpoonacularIngredient ing : sr.usedIngredients) {
                                    ingList.add(ing.name);
                                }
                            }
                            model.setIngredients(ingList);
                            model.setMatchCount(sr.usedIngredientCount);
                            recipeList.add(model);
                        }
                        adapter.updateRecipes(recipeList);
                        recyclerView.setVisibility(View.VISIBLE);
                        if (emptyStateView != null) emptyStateView.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Found " + spoonRecipes.size() + " recipes from Spoonacular", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No recipes found on Spoonacular", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Spoonacular error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }
        }).start();
    }

    static class SpoonacularRecipe {
        int id;
        String title;
        String image;
        int usedIngredientCount;
        int missedIngredientCount;
        List<SpoonacularIngredient> missedIngredients;
        List<SpoonacularIngredient> usedIngredients;
    }

    static class SpoonacularIngredient {
        String name;
    }
}