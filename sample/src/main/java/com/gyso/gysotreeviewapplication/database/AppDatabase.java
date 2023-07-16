package com.gyso.gysotreeviewapplication.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gyso.gysotreeviewapplication.Tool.Callback;

@Database(entities = {Element.class, User.class, ChildIndex.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ElementDao elementDao();
    public abstract UserDao userDao();
    public abstract ChildIndexDao childIndexDao();

    private static AppDatabase INSTANCE;
    public static AppDatabase getDBInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "AppDB")
                    .allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }

    public void makeElement(Element element, com.gyso.gysotreeviewapplication.Tool.Callback callback) {
        INSTANCE.runInTransaction(new Runnable() {
            @Override
            public void run() {
                int id = (int) INSTANCE.elementDao().insertElement(element);
                ChildIndex childIndex = new ChildIndex();
                childIndex.elementId = id;
                INSTANCE.childIndexDao().insertChildIndexes(childIndex);
                callback.onCallback(id);
            }
        });
    }

    public void updateElement(Element element, com.gyso.gysotreeviewapplication.Tool.Callback callback) {
        INSTANCE.runInTransaction(new Runnable() {
            @Override
            public void run() {




                callback.onCallback(null);
            }
        });
    }
}
