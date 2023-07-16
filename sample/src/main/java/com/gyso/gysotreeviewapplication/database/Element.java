package com.gyso.gysotreeviewapplication.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "elements")
public class Element implements Comparable<Element>{
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "parent_id")
    public int parentId;
    @ColumnInfo(name = "img_uri")
    public String imgUri;
    @ColumnInfo(name = "link_url")
    public String linkUrl;
    @ColumnInfo(name = "content")
    public String content;
    @ColumnInfo(name = "description")
    public String description;
    @ColumnInfo(name = "node_info")
    public String nodeInfo;
    @ColumnInfo(name = "is_img")
    public Boolean isImg;
    @ColumnInfo(name = "is_link")
    public Boolean isLink;
    @ColumnInfo(name = "line_num", defaultValue = "1")
    public int lineNum;

    @Override
    public int compareTo(Element element) {
        if(this.lineNum > element.lineNum){
            return 1;
        }else{
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Element{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", imgUri='" + imgUri + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", content='" + content + '\'' +
                ", description='" + description + '\'' +
                ", nodeInfo=" + nodeInfo +
                ", isImg=" + isImg +
                ", isLink=" + isLink +
                '}';
    }
}
