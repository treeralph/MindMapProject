package com.gyso.gysotreeviewapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyso.gysotreeviewapplication.Tool.Callback;
import com.gyso.gysotreeviewapplication.base.AnimalTreeViewAdapter;
import com.gyso.gysotreeviewapplication.database.AppDatabase;
import com.gyso.gysotreeviewapplication.database.Element;
import com.gyso.gysotreeviewapplication.databinding.ActivityFolderBinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class FolderActivity extends AppCompatActivity {

    private static final String TAG = "FolderActivity";

    ActivityFolderBinding binding;
    ActivityRecyclerViewAdapter adapter;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFolderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = AppDatabase.getDBInstance(this);

        activityViewInitializer();
    }

    private void activityViewInitializer(){

        binding.folderActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivityRecyclerViewAdapter(db, object -> {
            int rootId = (int) object;

            Intent intent = new Intent();
            intent.putExtra("rootId", rootId);
            setResult(RESULT_OK, intent);

            finish();
        });

        binding.folderActivityRecyclerView.setAdapter(adapter);
        binding.folderActivityWriteButton.setOnClickListener(v -> {
            binding.folderActivityButtonViewGroup.setVisibility(View.GONE);
            binding.folderActivityWriteViewGroup.setVisibility(View.VISIBLE);
        });
        binding.folderActivityRemoveButton.setOnClickListener(v -> {
            binding.folderActivityButtonViewGroup.setVisibility(View.GONE);
            binding.folderActivityRemoveViewGroup.setVisibility(View.VISIBLE);
            adapter.changeMode();
        });
        binding.folderActivityWriteWriteDoneButton.setOnClickListener(v -> {
            String newFolderName = binding.folderActivityWriteEditText.getText().toString().trim();
            Element temp = db.elementDao().getElementWithContent(newFolderName);
            if(!(newFolderName == null) && !newFolderName.equals("") && temp==null){
                Element newRoot = new Element();
                newRoot.parentId = -1;
                newRoot.isImg = false;
                newRoot.isLink = false;
                newRoot.nodeInfo = "auto";
                newRoot.content = newFolderName;

                adapter.addElement(newRoot);

                binding.folderActivityWriteViewGroup.setVisibility(View.GONE);
                binding.folderActivityButtonViewGroup.setVisibility(View.VISIBLE);
            }else{
                Log.e(TAG, "activityViewInitializer: FolderName: null");
            }
        });
        binding.folderActivityWriteRemoveDoneButton.setOnClickListener(v -> {
            try{
                adapter.removeClickedElem();
                adapter.changeMode();
                binding.folderActivityRemoveViewGroup.setVisibility(View.GONE);
                binding.folderActivityButtonViewGroup.setVisibility(View.VISIBLE);
            } catch(Exception e){
                Log.e(TAG, "activityViewInitializer: " + e.toString());
            }
        });
    }





    public static class ActivityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static int NORMAL_MODE = 0;
        public static int DELETE_MODE = 1;

        List<Element> elements;
        int clicked = -1;
        int mode = NORMAL_MODE;

        Callback normalCallback;

        AppDatabase db;

        public ActivityRecyclerViewAdapter(AppDatabase db, Callback normalCallback) {
            this.normalCallback = normalCallback;
            this.db = db;
            this.elements = db.elementDao().getElementWithParentId(-1);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch(viewType){
                case 0:
                    View itemView0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_folder, parent, false);
                    return new FolderViewHolder(itemView0);
                case 1:
                    View itemView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_folder_clicked, parent, false);
                    return new FolderClickedViewHolder(itemView1);
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
                    if(mode == NORMAL_MODE){
                        viewHolder.linearLayout.setOnClickListener(v -> {
                            normalCallback.onCallback(currentElem.id);
                        });
                    }else{
                        viewHolder.linearLayout.setOnClickListener(v -> {
                            clicked = holder.getAdapterPosition();
                            notifyDataSetChanged();
                        });
                    }
                    break;
                case 1:
                    FolderClickedViewHolder clickedViewHolder = (FolderClickedViewHolder) holder;
                    clickedViewHolder.textView.setText(currentElem.content);
                    clickedViewHolder.linearLayout.setOnClickListener(v -> {
                        clicked = -1;
                        notifyDataSetChanged();
                    });
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(position == clicked){
                return 1;
            }else{
                return 0;
            }
        }

        public void addElement(Element element) {
            db.elementDao().insertElement(element);
            elements = db.elementDao().getElementWithParentId(-1);
            notifyDataSetChanged();
        }

        public void changeMode(){

            if(mode == NORMAL_MODE){
                mode = DELETE_MODE;
            }else{
                mode = NORMAL_MODE;
            }
            /**
             * todo: I'm not sure that it's working right way.
             * */
            notifyDataSetChanged();
        }

        public void removeClickedElem() throws Exception {
            Element clickedElement = elements.get(clicked);
            removeRoot(clickedElement);
            elements = db.elementDao().getElementWithParentId(-1);
        }

        public void removeRoot(Element root) {
            Queue<Element> queue = new LinkedList<>();
            queue.add(root);
            while(!queue.isEmpty()){
                Element current = queue.poll();
                List<Element> children = db.elementDao().getElementWithParentId(current.id);
                db.elementDao().deleteElement(current);
                for(Element child: children){
                    queue.add(child);
                }
            }
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
}