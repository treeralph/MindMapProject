package com.gyso.gysotreeviewapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ChildIndexDao {
    @Insert
    public void insertChildIndexes(ChildIndex... childIndexes);
    @Update
    public void updateChildIndexes(ChildIndex... childIndexes);
    @Delete
    public void deleteChildIndex(ChildIndex childIndex);
    @Query("select * from child_indexes where element_id = :element_id")
    public ChildIndex getChildIndexWithElementId(int element_id);
}
