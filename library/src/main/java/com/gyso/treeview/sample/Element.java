package com.gyso.treeview.sample;

public class Element {

    public int id;
    public int parentId;
    public String imgUri;
    public String linkUrl;
    public String content;
    public String description;
    public String nodeInfo;
    public Boolean isImg;
    public Boolean isLink;

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
