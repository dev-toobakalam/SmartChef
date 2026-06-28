package com.mad.smartchef.models;

import java.util.ArrayList;
import java.util.Objects;

public class RecipeModel {
    private String id;
    private String name;
    private String imageUrl;
    private String instructions;
    private ArrayList<String> ingredients;
    private ArrayList<String> dietary;
    private int matchCount;

    public RecipeModel() {
        // Required no-arg constructor for Firestore
    }

    // ---------- Constructor with dietary ----------
    public RecipeModel(String id, String name, String imageUrl, String instructions,
                       ArrayList<String> ingredients, ArrayList<String> dietary, int matchCount) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.dietary = dietary;
        this.matchCount = matchCount;
    }

    // ---------- Constructor without dietary (for backward compatibility) ----------
    public RecipeModel(String id, String name, String imageUrl, String instructions,
                       ArrayList<String> ingredients, int matchCount) {
        this(id, name, imageUrl, instructions, ingredients, null, matchCount);
    }

    // ---------- Getters and Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public ArrayList<String> getIngredients() { return ingredients; }
    public void setIngredients(ArrayList<String> ingredients) { this.ingredients = ingredients; }

    public ArrayList<String> getDietary() { return dietary; }
    public void setDietary(ArrayList<String> dietary) { this.dietary = dietary; }

    public int getMatchCount() { return matchCount; }
    public void setMatchCount(int matchCount) { this.matchCount = matchCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeModel)) return false;
        RecipeModel that = (RecipeModel) o;
        return matchCount == that.matchCount &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(instructions, that.instructions) &&
                Objects.equals(ingredients, that.ingredients) &&
                Objects.equals(dietary, that.dietary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, imageUrl, instructions, ingredients, dietary, matchCount);
    }
}