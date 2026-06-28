package com.mad.smartchef.Fragments;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.smartchef.R;
import com.mad.smartchef.activities.RecipeDetailActivity;
import com.mad.smartchef.adapter.RecipeAdapter;
import com.mad.smartchef.data.AppDatabase;
import com.mad.smartchef.data.RecipeEntity;
import com.mad.smartchef.models.RecipeModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        emptyView = view.findViewById(R.id.emptyFavoritesText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            // Open detail on click
            Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadFavorites();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites(); // refresh when returning
    }

    private void loadFavorites() {
        if (getContext() == null) return;
        new Thread(() -> {
            List<RecipeEntity> entities = AppDatabase.getInstance(getContext())
                    .recipeDao().getAll();
            requireActivity().runOnUiThread(() -> {
                List<RecipeModel> models = new ArrayList<>();
                for (RecipeEntity entity : entities) {
                    RecipeModel model = new RecipeModel();
                    model.setId(entity.getId());
                    model.setName(entity.getName());
                    model.setImageUrl(entity.getImageUrl());
                    model.setInstructions(entity.getInstructions());
                    // Convert ingredients string back to ArrayList
                    ArrayList<String> ingList = new ArrayList<>();
                    if (entity.getIngredients() != null && !entity.getIngredients().isEmpty()) {
                        String[] parts = entity.getIngredients().split(",");
                        for (String p : parts) ingList.add(p.trim());
                    }
                    model.setIngredients(ingList);
                    model.setMatchCount(0); // not used
                    models.add(model);
                }
                adapter.updateRecipes(models);
                emptyView.setVisibility(models.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }
}