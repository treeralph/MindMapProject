package com.gyso.gysotreeviewapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gyso.gysotreeviewapplication.Tool.Callback;
import com.gyso.gysotreeviewapplication.Tool.DataAPI;
import com.gyso.gysotreeviewapplication.Tool.SizeTool;
import com.gyso.gysotreeviewapplication.database.AppDatabase;
import com.gyso.gysotreeviewapplication.database.ChildIndex;
import com.gyso.gysotreeviewapplication.database.ChildIndexDao;
import com.gyso.gysotreeviewapplication.database.Element;
import com.gyso.gysotreeviewapplication.databinding.ActivityAddElementBinding;
import com.gyso.gysotreeviewapplication.databinding.ActivityShareBinding;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";
    public static final String SHARE_NEW_NODE_ID_CODE = "SHARE_NEW_NODE_CODE";

    ActivityShareBinding binding;
    Handler handler;
    AppDatabase db;

    DataAPI.LinkCapsule capsule;
    Bitmap bitmapTemper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDBInstance(this);

        handler = new Handler(message -> {
            if(message.what == 1){
                makeElement(capsule, bitmapTemper, new Callback() {
                    @Override
                    public void onCallback(Object object) {
                        long id = (long) object;

                        ChildIndex childIndex = new ChildIndex();
                        childIndex.elementId = (int) id;
                        db.childIndexDao().insertChildIndexes(childIndex);

                        Log.e(TAG, "makeElement: " + String.valueOf(id) + " element is made");
                        Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                        intent.putExtra(SHARE_NEW_NODE_ID_CODE, (int) id);
                        startActivity(intent);
                        finish();
                    }
                });
            }
            return false;
        });

        try {
            handleSharedText(new Callback() {
                @Override
                public void onCallback(Object object) {
                    capsule = (DataAPI.LinkCapsule) object;
                    Log.e(TAG, "onCallback: " + capsule);
                    getImage(capsule.imageUrl, bitmapObject -> {
                        Bitmap bitmap = (Bitmap) bitmapObject;
                        bitmapTemper = bitmap;
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    });
                }
            });
        } catch(Exception e){
            Log.e(TAG, "onCreate: error: " + e.toString());
        }
    }

    private void makeElement(DataAPI.LinkCapsule capsule, Bitmap bitmap, Callback callback) {

        /**
         * todo: how to set new element directory?
         *      1 -> make a directory for auto-make element ( V )
         *      2 -> user last directory
         *      3 -> ?
         * */

        Element autoRoot = db.elementDao().getAutoRootNode("auto");
        if(autoRoot == null){
            /**
             * node_info == "auto" 를 만족하는 루트가 없는 경우, 임의의 루트를 가져와서 새로운 노드를 추가한다.
             * */
            List<Element> roots = db.elementDao().getElementWithParentId(-1);
            Element anyRoot = roots.get(0);
            autoRoot = anyRoot;
        }
        ChildIndex childIndex = db.childIndexDao().getChildIndexWithElementId(autoRoot.id);
        int index = childIndex.index;

        Element newElem = new Element();
        newElem.parentId = autoRoot.id;
        newElem.description = capsule.description;
        newElem.content = capsule.title;
        newElem.linkUrl = capsule.link;
        newElem.isLink = true;
        newElem.lineNum = index;

        childIndex.index = index + 1;
        db.childIndexDao().updateChildIndexes(childIndex);

        if(bitmap != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                String path = getExternalCacheDir().getAbsolutePath();
                String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS")) + ".png";

                Log.e("File path", "File path: " + path + "/" + fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();

                    newElem.imgUri = path + "/" + fileName;
                    newElem.isImg = true;
                } catch (Exception e) {
                    newElem.isImg = false;
                }
            }
        }else{
            newElem.isImg = false;
        }
        long newElemId = db.elementDao().insertElement(newElem);
        callback.onCallback(newElemId);
    }

    private void handleSharedText(Callback callback) throws Exception{
        Intent intent = getIntent();
        Log.e("handleSharedText", "intent: " + intent.getExtras().toString());

        Bundle tempBundle = intent.getExtras();
        for(String key: tempBundle.keySet()){
            Log.e("handleSharedText", key + ": " + tempBundle.get(key).toString());
        }





        String action = intent.getAction();
        String type = intent.getType();
        Log.e("handleSharedText", "action: " + action + ", type: " + type);
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.e("handleSharedText", "sharedText: " + sharedText);
            if (sharedText != null) {
                DataAPI.linkParser(sharedText, new Callback() {
                    @Override
                    public void onCallback(Object object) {
                        DataAPI.LinkCapsule capsule = (DataAPI.LinkCapsule) object;
                        callback.onCallback(capsule);
                    }
                });
                return ;
            }else{
                throw new Exception("Shared Text is null");
            }
        }else{
            throw new Exception("Intent Action or MIME error");
        }
    }

    public static void getImage(String imageUrl, Callback callback){
        Thread thread = new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(imageUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    InputStream inputStream = responseBody.byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Bitmap resizedBitmap = SizeTool.resizing(bitmap);
                    callback.onCallback(resizedBitmap);
                }
            } catch (IOException e) {
                Log.e(TAG, "getImage: " + e.toString());
                e.printStackTrace();
            }
        });
        thread.start();
    }
}