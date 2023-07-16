package com.gyso.gysotreeviewapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gyso.gysotreeviewapplication.Tool.YoutubeAPI;
import com.gyso.gysotreeviewapplication.base.Animal;
import com.gyso.gysotreeviewapplication.base.AnimalTreeViewAdapter;
import com.gyso.gysotreeviewapplication.base2.ElementTreeViewAdapter;
import com.gyso.gysotreeviewapplication.database.AppDatabase;
import com.gyso.gysotreeviewapplication.database.ChildIndex;
import com.gyso.gysotreeviewapplication.database.Element;
import com.gyso.gysotreeviewapplication.database.User;
import com.gyso.gysotreeviewapplication.databinding.ActivityMainBinding;
import com.gyso.gysotreeviewapplication.dialog.ElementDetailDialog;
import com.gyso.treeview.TreeViewEditor;
import com.gyso.treeview.for_sample.ReleasedCallback;
import com.gyso.treeview.layout.BoxHorizonLeftAndRightLayoutManager;
import com.gyso.treeview.layout.BoxRightTreeLayoutManager;
import com.gyso.treeview.layout.BoxVerticalUpAndDownLayoutManager;
import com.gyso.treeview.layout.CompactRingTreeLayoutManager;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.AngledLine;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.SmoothLine;
import com.gyso.treeview.line.StraightLine;
import com.gyso.treeview.listener.TreeViewControlListener;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    public static final int PSEUDO_NODE_FOR_NEW_BRANCH = -2;
    public static final int CHOOSE_FOLDER_REQUEST_CODE = 0;
    public static final int ADD_ELEMENT_REQUEST_CODE = 1;

    public static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private final Stack<NodeModel<Element>> removeCache = new Stack();
    private NodeModel<Element> targetNode;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private Handler handler = new Handler();
    private NodeModel<Element> parentToRemoveChildren = null;

    ElementTreeViewAdapter adapter;
    TreeViewEditor editor;

    private AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDBInstance(this);
        //demo init
        initWidgets();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int newElementId = intent.getIntExtra(ShareActivity.SHARE_NEW_NODE_ID_CODE, -100);
        if(newElementId != -100){
            Element newElement = db.elementDao().getElementWithId(newElementId);
            setDataElement(newElement.parentId);
        }
    }

    /**
     * @Author: jun
     * todo: how to manage target Node!!!!!!!! ??????????
     * */

    /**
     * To use a tree view, you should do 6 steps as follows:
     *      1 customs adapter
     *
     *      2 configure layout manager. Space unit is dp.
     *      You can custom you line by extends {@link BaseLine}
     *
     *      3 view setting
     *
     *      4 nodes data setting
     *
     *      5 if you want to edit the map, then get and use and tree view editor
     *
     *      6 you own others jobs
     */
    private void initWidgets() {
        //1 customs adapter
        adapter = new ElementTreeViewAdapter();

        //2 configure layout manager; unit dp
        TreeLayoutManager treeLayoutManager = getTreeLayoutManager();

        //3 view setting
        binding.baseTreeView.setAdapter(adapter);
        binding.baseTreeView.setTreeLayoutManager(treeLayoutManager);

        //4 nodes data setting
        User currentUser = db.userDao().getAllUsers().get(0);
        try{
            setDataElement(currentUser.lastMindMapRootId);
        } catch(Exception e){
            Element root = db.elementDao().getElementWithParentId(-1).get(0);
            setDataElement(root.id);
        }

        //5 get an editor. Note: an adapter must set before get an editor.
        editor = binding.baseTreeView.getEditor();
        editor.setReleasedCallback(object -> {
            Log.e(TAG, "ReleasedCallback: Object: " + object.toString());
            Map<String, Object> relation = (Map<String, Object>) object;
            NodeModel<Element> released = (NodeModel<Element>) relation.get("released");
            NodeModel<Element> targeted = (NodeModel<Element>) relation.get("targeted");
            /**
             * todo: onDragMoveNodesHit에서 지금까지 처리하던 것을 여기서 처리하려고 한다.
             * */
            if(targeted.value.id == PSEUDO_NODE_FOR_NEW_BRANCH) {

            } else{

            }
        });

        //6 you own others jobs
        doYourOwnJobs(editor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CHOOSE_FOLDER_REQUEST_CODE){
                int currentRootId = data.getIntExtra("rootId", -1);
                if(currentRootId != -1){
                    setDataElement(currentRootId);
                }
            }else if(requestCode == ADD_ELEMENT_REQUEST_CODE){
                int newNodeId = (int) data.getLongExtra("newNodeId", 0);
                if(newNodeId != 0){
                    Element newNode = db.elementDao().getElementWithId(newNodeId);
                    NodeModel<Element> newNodeModel = new NodeModel<>(newNode);
                    editor.addChildNodes(adapter.getTreeModel().getRootNode(), newNodeModel);
                }else{
                    setDataElement(adapter.getTreeModel().getRootNode().value.id);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        User currentUser = db.userDao().getAllUsers().get(0);
        currentUser.lastMindMapRootId = adapter.getTreeModel().getRootNode().value.id;
        db.userDao().updateUsers(currentUser);
    }


    void doYourOwnJobs(TreeViewEditor editor){
        //choose folder button

        binding.baseTreeView.setOnClickListener(v -> {
            targetNode = null;
            Log.e(TAG, "TargetNode: NULL");
        });

        /**
         * @Author: jun
         * */
        binding.chooseFolderBt.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FolderActivity.class);
            startActivityForResult(intent, CHOOSE_FOLDER_REQUEST_CODE);
        });

        //drag to move node
        binding.dragEditModeRd.setOnCheckedChangeListener((v, isChecked)->{
            editor.requestMoveNodeByDragging(isChecked);
            /*
            if(isChecked){ // edit mode
                NodeModel<Element> rootNodeModel = adapter.getTreeModel().getRootNode();
                Element pseudoNode = new Element();
                pseudoNode.id = PSEUDO_NODE_FOR_NEW_BRANCH;
                pseudoNode.parentId = rootNodeModel.value.id;
                pseudoNode.isLink = false;
                pseudoNode.isImg = false;
                NodeModel<Element> pseudoNodeModel = new NodeModel<>(pseudoNode);
                editor.addChildNodes(rootNodeModel, pseudoNodeModel);
            }else{ // normal mode
                NodeModel<Element> rootNodeModel = adapter.getTreeModel().getRootNode();
                for(NodeModel<Element> child: rootNodeModel.childNodes) {
                    if(child.value.id == PSEUDO_NODE_FOR_NEW_BRANCH) {
                        editor.removeNode(child);
                        setDataElement(rootNodeModel.value.id);
                    }
                }
                editor.focusMidLocation();
            }
             */
        });

        //focus, means that tree view fill center in your window viewport
        binding.viewCenterBt.setOnClickListener(v->editor.focusMidLocation());

        //add some nodes
        binding.addNodesBt.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, AddElementActivity.class);
            startActivityForResult(intent, ADD_ELEMENT_REQUEST_CODE);
        });

        adapter.setOnItemListener((item, node)-> {
            ElementDetailDialog dialog = new ElementDetailDialog(this, node, object -> {
                // delete callback
                NodeModel<Element> elementNode = (NodeModel<Element>) object;
                Element element = elementNode.getValue();
                for(NodeModel<Element> child: elementNode.getChildNodes()){
                    Element childElement = child.value;
                    childElement.parentId = element.parentId;
                    db.elementDao().updateElements(childElement);
                }
                db.elementDao().deleteElement(element);
                setDataElement(adapter.getTreeModel().getRootNode().value.id);
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });



        adapter.setOnItemLongListener(((item, node) -> {
            try {
                YoutubeAPI.openYoutube(getApplicationContext(), node.value.linkUrl);
            } catch(Exception e){
                Log.e(TAG, "open Link error occur: " + e.toString());
            }
        }));

        //treeView control listener
        final Object token = new Object();
        Runnable dismissRun = ()->{
            binding.scalePercent.setVisibility(View.GONE);
        };

        binding.baseTreeView.setTreeViewControlListener(new TreeViewControlListener() {
            @Override
            public void onScaling(int state, int percent) {
                Log.e(TAG, "onScaling: "+state+"  "+percent);
                binding.scalePercent.setVisibility(View.VISIBLE);
                if(state == TreeViewControlListener.MAX_SCALE){
                    binding.scalePercent.setText("MAX");
                }else if(state == TreeViewControlListener.MIN_SCALE){
                    binding.scalePercent.setText("MIN");
                }else{
                    binding.scalePercent.setText(percent+"%");
                }
                handler.removeCallbacksAndMessages(token);
                handler.postAtTime(dismissRun,token,SystemClock.uptimeMillis()+2000);
            }

            @Override
            public void onDragMoveNodesHit(NodeModel<?> draggingNode, NodeModel<?> hittingNode, View draggingView, View hittingView) {

                /**
                 * Hit이 발생할때마다 호출되는 메서드이므로 의도한 바와 다르다.
                 * 따라서, db 업데이트를 이 메서드에서 수행하는 것을 옳지 않다.
                 * */

                /*
                try {
                    Element draggingNodeValue = (Element) draggingNode.value;
                    Element hittingNodeValue = (Element) hittingNode.value;
                    if(hittingNodeValue.id == PSEUDO_NODE_FOR_NEW_BRANCH){
                        NodeModel<Element> root = adapter.getTreeModel().getRootNode();

                        ChildIndex childIndex = db.childIndexDao().getChildIndexWithElementId(root.value.id);
                        int index = childIndex.index;

                        draggingNodeValue.parentId = root.value.id;
                        draggingNodeValue.lineNum = index;
                        db.elementDao().updateElements(draggingNodeValue);

                        childIndex.index = index + 1;
                        db.childIndexDao().updateChildIndexes(childIndex);
                        Log.e(TAG, "onDragMoveNodesHit: " + "ASDFASDFASDF");
                    }else{
                        ChildIndex childIndex = db.childIndexDao().getChildIndexWithElementId(hittingNodeValue.id);
                        int index = childIndex.index;

                        draggingNodeValue.parentId = hittingNodeValue.id;
                        draggingNodeValue.lineNum = index;

                        childIndex.index = index + 1;
                        db.childIndexDao().updateChildIndexes(childIndex);
                        db.elementDao().updateElements(draggingNodeValue);
                    }
                }catch(Exception e){

                }

                 */
            }
        });
    }

    /**
     * Box[XXX]TreeLayoutManagers are recommend for your project for they are running stably. Others treeLayoutManagers are developing.
     * @return layout manager
     */
    private TreeLayoutManager getTreeLayoutManager() {
        int space_50dp = 30;
        int space_20dp = 20;
        BaseLine line = getLine();
        return new BoxRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);


        //TODO !!!!! the layoutManagers below are just for test don't use in your projects. Just for test now
        //return new TableRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);

        //return new CompactRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);

        //return new CompactRingTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new ForceDirectedTreeLayoutManager(this,line);
    }

    private BaseLine getLine() {
        //return new SmoothLine();
        //return new StraightLine(Color.parseColor("#055287"),2);
        //return new DashLine(Color.parseColor("#F1286C"),3);
        return new AngledLine();
    }

    /**
     * @Author: jun
     * @Time: 0621-15:47
     * */
    private void setDataElement(int rootId){

        Element rootNode = db.elementDao().getElementWithId(rootId);

        NodeModel<Element> root = new NodeModel<>(rootNode);
        TreeModel<Element> treeModel = new TreeModel<>(root);

        Queue<NodeModel<Element>> queue = new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()){
            NodeModel<Element> current = queue.poll();
            List<Element> children = db.elementDao().getElementWithParentId(current.value.id);
            Collections.sort(children);
            for(Element child: children){
                NodeModel<Element> childModel = new NodeModel<>(child);
                childModel.setParentNode(current);
                treeModel.addNode(current, childModel);
                queue.add(childModel);
            }
        }
        adapter.setTreeModel(treeModel);
        targetNode = root;
    }
}