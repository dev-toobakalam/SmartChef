package com.mad.smartchef.data;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ShoppingDao {
    @Insert
    void insert(ShoppingItemEntity item);

    @Update
    void update(ShoppingItemEntity item);

    @Query("SELECT * FROM shopping_items ORDER BY id ASC")
    List<ShoppingItemEntity> getAll();

    @Query("DELETE FROM shopping_items WHERE id = :id")
    void delete(long id);
}
