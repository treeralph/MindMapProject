package com.gyso.gysotreeviewapplication.database;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "nickname")
    public String nickname;
    @ColumnInfo(name = "password_in_app")
    public String passwordInApp;
    @ColumnInfo(name = "profile_image_uri")
    public String profileImageUri;
    @ColumnInfo(name = "user_content")
    public String userContent;
    @ColumnInfo(name = "last_mind_map_root_id")
    public int lastMindMapRootId;
}
