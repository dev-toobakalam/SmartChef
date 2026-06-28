package com.mad.smartchef.data;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_items")
public class ShoppingItemEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public boolean checked; // purchased or not

    public ShoppingItemEntity() {}
}
