package com.gyso.gysotreeviewapplication.Tool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gyso.gysotreeviewapplication.R;
import com.gyso.gysotreeviewapplication.database.AppDatabase;
import com.gyso.gysotreeviewapplication.database.Element;

import java.util.List;

public class FolderRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Element> elements;
    int clicked = -1;

    AppDatabase db;
    Context context;

    public FolderRecyclerViewAdapter(Context context, List<Element> elements) {
        this.elements = elements;
        this.context = context;
        this.db = AppDatabase.getDBInstance(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType){
            case 0:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_folder, parent, false);
                return new FolderViewHolder(itemView);
            case 1:
                View itemViewClicked = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_folder_clicked, parent, false);
                return new FolderClickedViewHolder(itemViewClicked);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Element currentElem = elements.get(holder.getAdapterPosition());

        switch(getItemViewType(holder.getAdapterPosition())){
            case 0:
                FolderViewHolder viewHolder = (FolderViewHolder) holder;
                viewHolder.textView.setText(currentElem.content);
                viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clicked = holder.getAdapterPosition();
                        notifyDataSetChanged();
                    }
                });
                break;
            case 1:
                FolderClickedViewHolder viewHolderClicked = (FolderClickedViewHolder) holder;
                viewHolderClicked.textView.setText(currentElem.content);
                viewHolderClicked.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clicked = -1;
                        notifyDataSetChanged();
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == clicked){
            return 1;
        }else{
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    public void setClickedElem(int id) throws Exception {

        Element target = db.elementDao().getElementWithId(id);
        while(target.parentId != -1) {
            target = db.elementDao().getElementWithId(target.parentId);
        }
        int targetRootId = target.id;
        for(int i=0; i<elements.size(); i++){
            Element root = elements.get(i);
            if(root.id == targetRootId){
                clicked = i;
                notifyDataSetChanged();
            }
        }
    }
    public Element getClickedElem() throws Exception {
        return elements.get(clicked);
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder{
        LinearLayout linearLayout;
        TextView textView;
        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.itemFolderLinearLayout);
            textView = itemView.findViewById(R.id.itemFolderTextView);
        }
    }

    public class FolderClickedViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView textView;
        public FolderClickedViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.itemFolderClickedLinearLayout);
            textView = itemView.findViewById(R.id.itemFolderClickedTextView);
        }
    }
}
