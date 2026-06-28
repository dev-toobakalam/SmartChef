package com.mad.smartchef.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mad.smartchef.R;
import com.mad.smartchef.models.RecipeModel;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RecipeModel recipe);
    }

    private List<RecipeModel> recipeList;
    private OnItemClickListener clickListener;

    // Constructor
    public RecipeAdapter(List<RecipeModel> recipeList, OnItemClickListener listener) {
        this.recipeList = recipeList != null ? recipeList : new ArrayList<>();
        this.clickListener = listener;
    }

    // Update adapter data and refresh list
    public void updateRecipes(List<RecipeModel> newRecipes) {
        if (newRecipes == null) newRecipes = new ArrayList<>();
        this.recipeList.clear();
        this.recipeList.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.ViewHolder holder, int position) {
        RecipeModel recipe = recipeList.get(position);
        holder.bind(recipe, clickListener);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recipeImage);
            nameView = itemView.findViewById(R.id.recipeName);
        }

        public void bind(RecipeModel recipe, OnItemClickListener listener) {
            // Set recipe name
            nameView.setText(recipe.getName() != null ? recipe.getName() : "No Name");

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(recipe);
                }
            });

            // Load image safely
            String url = recipe.getImageUrl();
            if (url != null) {
                url = url.trim();
                // Remove quotes if present
                if (url.startsWith("\"") && url.endsWith("\"") && url.length() > 1) {
                    url = url.substring(1, url.length() - 1);
                }
            }

            if (url != null && !url.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_dialog_alert)
                        .into(imageView);

                Log.d("RecipeAdapter", "Loaded image URL: " + url);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                Log.w("RecipeAdapter", "Image URL is empty or null");
            }
        }
    }
}
