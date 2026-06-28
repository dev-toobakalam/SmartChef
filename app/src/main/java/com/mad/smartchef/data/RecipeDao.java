package com.mad.smartchef.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RecipeDao {
    @Insert
    void insert(RecipeEntity recipe);

    @Delete
    void delete(RecipeEntity recipe);

    @Query("SELECT * FROM favorites ORDER BY name ASC")
    List<RecipeEntity> getAll();

    @Query("SELECT COUNT(*) FROM favorites WHERE id = :id")
    int isFavorite(String id);
}