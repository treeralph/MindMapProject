package com.gyso.gysotreeviewapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    public void insertUsers(User... users);
    @Delete
    public void deleteUsers(User... users);
    @Update
    public void updateUsers(User... users);
    @Query("select * from user")
    public List<User> getAllUsers();
}
