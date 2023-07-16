package com.gyso.gysotreeviewapplication.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "child_indexes")
public class ChildIndex {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "element_id")
    public int elementId;
    @ColumnInfo(name = "index", defaultValue = "1")
    public int index;
}
