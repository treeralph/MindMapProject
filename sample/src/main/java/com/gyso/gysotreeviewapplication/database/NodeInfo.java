package com.gyso.gysotreeviewapplication.database;

public class NodeInfo {
    public int nodeColor;
    public int lineColor;
    public String nodeShape;
    public String lineShape;
    public NodeInfo(){

    }

    public int getNodeColor() {
        return nodeColor;
    }

    public void setNodeColor(int nodeColor) {
        this.nodeColor = nodeColor;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public String getNodeShape() {
        return nodeShape;
    }

    public void setNodeShape(String nodeShape) {
        this.nodeShape = nodeShape;
    }

    public String getLineShape() {
        return lineShape;
    }

    public void setLineShape(String lineShape) {
        this.lineShape = lineShape;
    }
}
