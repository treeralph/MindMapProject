package com.gyso.gysotreeviewapplication.base2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.gyso.gysotreeviewapplication.MainActivity;
import com.gyso.gysotreeviewapplication.R;
import com.gyso.gysotreeviewapplication.base.Animal;
import com.gyso.gysotreeviewapplication.database.Element;
import com.gyso.gysotreeviewapplication.databinding.NodeBaseLayoutBinding;
import com.gyso.gysotreeviewapplication.databinding.NodeBaseLayoutClickedBinding;
import com.gyso.gysotreeviewapplication.databinding.NodeBaseLayoutForNewBranchBinding;
import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.DashLine;
import com.gyso.treeview.line.EmptyLine;
import com.gyso.treeview.model.NodeModel;

import java.io.File;
import java.io.FileInputStream;

/**
 * @Author: 怪兽N
 * @Time: 2021/4/23  16:48
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * Tree View Adapter for node data to view
 */
public class ElementTreeViewAdapter extends TreeViewAdapter<Element> {
    private DashLine dashLine =  new DashLine(Color.parseColor("#F06292"),6);
    private EmptyLine emptyLine = new EmptyLine();
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;
    private int clicked = -10;
    private NodeModel<Element> clickedNodeModel;

    public void setOnItemListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setOnItemLongListener(OnItemLongClickListener longListener){
        this.longListener = longListener;
    }

    @Override
    public TreeViewHolder<Element> onCreateViewHolder(@NonNull ViewGroup viewGroup, NodeModel<Element> node) {
        if(node.value.id == MainActivity.PSEUDO_NODE_FOR_NEW_BRANCH){
            NodeBaseLayoutForNewBranchBinding nodeForNewBranchBinding = NodeBaseLayoutForNewBranchBinding.inflate(LayoutInflater.from(viewGroup.getContext()),viewGroup,false);
            return new TreeViewHolder<>(nodeForNewBranchBinding.getRoot(), node);
        } else if(node.value.id == clicked){
            NodeBaseLayoutClickedBinding nodeClickedBinding = NodeBaseLayoutClickedBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
            return new TreeViewHolder<>(nodeClickedBinding.getRoot(), node);
        } else {
            NodeBaseLayoutBinding nodeBinding = NodeBaseLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
            return new TreeViewHolder<>(nodeBinding.getRoot(), node);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder<Element> holder) {
        //todo get view and node from holder, and then show by you
        try {
            if (holder.getNode().value.id == MainActivity.PSEUDO_NODE_FOR_NEW_BRANCH) {
                return;
            }



            View itemView = holder.getView();
            NodeModel<Element> node = holder.getNode();
            FrameLayout outerViewGroup = itemView.findViewById(R.id.outerLayout);
            CardView totalCardView = itemView.findViewById(R.id.nodeCardView);
            TextView contentView = itemView.findViewById(R.id.name);
            ImageView headView = itemView.findViewById(R.id.portrait);

            boolean isContent = true;
            boolean isImage = true;

            final Element element = node.value;

            String elementContent = element.content.trim();
            if(!(elementContent == null || elementContent.equals(""))){
                contentView.setText(elementContent);
            }else{
                contentView.setVisibility(View.GONE);
                isContent = false;
            }

            if (element.parentId == -1) {
                outerViewGroup.setVisibility(View.INVISIBLE);
            }


            if (element.isImg) {
                try {
                    File file = new File(element.imgUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                    headView.setImageBitmap(bitmap);

                    int imageWidth = bitmap.getWidth();
                    contentView.setMaxWidth(imageWidth);
                    contentView.setMaxLines(2);
                } catch (Exception e) {
                    headView.setVisibility(View.GONE);
                    String current = contentView.getText().toString();
                    contentView.setTextSize(35);
                    contentView.setText(" " + current + " ");
                    isImage = false;
                }
            }else{
                headView.setVisibility(View.GONE);
                String current = contentView.getText().toString();
                contentView.setTextSize(35);
                contentView.setText(" " + current + " ");

            }

            if(!isContent && !isImage){
                contentView.setText(" BLANK NODE ");
            }

            totalCardView.setOnClickListener(v -> {
                if(listener != null) {
                    listener.onItemClick(v, node);
                }
            });
            return;
        } catch(Exception e){

        }
    }

    @Override
    public BaseLine onDrawLine(DrawInfo drawInfo) {
        TreeViewHolder<?> toHolder = drawInfo.getToHolder();
        NodeModel<?> node = toHolder.getNode();
        NodeModel<?> parent = node.getParentNode();
        Object value = parent.getValue();
        if(value instanceof Element){
            Element parentNode = (Element) value;
            if(parentNode.parentId == -1){
                return emptyLine;
            }
        }
        return null;
    }

    public void setClicked(NodeModel<Element> targetNode){
        int target = targetNode.value.id;
        if(clicked == target){
            clicked = -10;
            clickedNodeModel = null;
        }else{
            clicked = target;
            clickedNodeModel = targetNode;
        }
        notifyDataSetChange();
    }

    public NodeModel<Element> getClickedNodeModel(){
        return clickedNodeModel;
    }

    public interface OnItemClickListener{
        void onItemClick(View item, NodeModel<Element> node);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View item, NodeModel<Element> node);
    }
}
