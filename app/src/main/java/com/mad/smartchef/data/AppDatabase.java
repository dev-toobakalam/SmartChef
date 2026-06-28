package com.mad.smartchef.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {RecipeEntity.class, ShoppingItemEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecipeDao recipeDao();
    public abstract ShoppingDao shoppingDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "smartchef_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}