package com.mad.smartchef.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class RecipeEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String imageUrl;
    private String instructions;
    private String ingredients;

    public RecipeEntity() {}

    @Ignore
    public RecipeEntity(String id, String name, String imageUrl, String instructions, String ingredients) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.ingredients = ingredients;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
}