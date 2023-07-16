package com.gyso.gysotreeviewapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ElementDao {
    @Update
    public void updateElements(Element... elements);
    @Insert
    public void insertElements(Element... elements);
    @Insert
    public long insertElement(Element element);
    @Delete
    public void deleteElement(Element element);
    @Query("select * from elements")
    public List<Element> getAllElements();
    @Query("select * from elements where parent_id = :parentId")
    public List<Element> getElementWithParentId(int parentId);
    @Query("select * from elements where id = :id")
    public Element getElementWithId(int id);
    @Query("select * from elements where node_info = :node_info")
    public Element getAutoRootNode(String node_info);
    @Query("select * from elements where content = :content")
    public Element getElementWithContent(String content);
}
