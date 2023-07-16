package com.gyso.gysotreeviewapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gyso.gysotreeviewapplication.Tool.Callback;
import com.gyso.gysotreeviewapplication.Tool.FolderRecyclerViewAdapter;
import com.gyso.gysotreeviewapplication.Tool.SizeTool;
import com.gyso.gysotreeviewapplication.Tool.YoutubeAPI;
import com.gyso.gysotreeviewapplication.database.AppDatabase;
import com.gyso.gysotreeviewapplication.database.ChildIndex;
import com.gyso.gysotreeviewapplication.database.Element;
import com.gyso.gysotreeviewapplication.databinding.ActivityAddElementBinding;
import com.gyso.gysotreeviewapplication.databinding.ActivityMainBinding;
import com.gyso.gysotreeviewapplication.dialog.ElementDetailDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AddElementActivity extends AppCompatActivity {

    private String TAG = AddElementActivity.class.getSimpleName();

    public static int GET_IMAGE = 1;
    public static int GET_THUMBNAIL_SUCCESS = 2;
    public static int GET_THUMBNAIL_FAILURE = 3;

    ActivityAddElementBinding binding;

    boolean useYoutubeThumbnail;
    Bitmap imgBitmap;
    String linkUrl;
    String contentName;
    String description;

    ImageView page1ImageView;
    TextView page1console;

    ViewPagerAdapter adapter;
    Handler handler;
    AppDatabase db;

    String sharedLinkTemper;

    boolean isUpdate = false;
    Element updatingElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddElementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDBInstance(this);
        onUpdate();

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                if(message.what == GET_THUMBNAIL_SUCCESS){
                    page1ImageView.setImageBitmap(imgBitmap);
                }else if(message.what == GET_THUMBNAIL_FAILURE){
                    page1console.setText("Load Youtube Thumbnail: Failure");
                }
                return false;
            }
        });

        activityViewInitializer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSharedText();
    }

    private void activityViewInitializer() {

        adapter = new ViewPagerAdapter();
        binding.addElementActivityViewPager.setUserInputEnabled(false);
        binding.addElementActivityViewPager.setAdapter(adapter);
        binding.addElementActivityIndicator.attachTo(binding.addElementActivityViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleSharedText();
    }

    private void onUpdate(){
        Intent intent = getIntent();
        int updatingElementId = intent.getIntExtra(ElementDetailDialog.ADD_ELEMENT_ID_EXTRA, -1);
        if(updatingElementId != -1){
            updatingElement = db.elementDao().getElementWithId(updatingElementId);
            isUpdate = true;
            Log.e(TAG, "onUpdate: " + updatingElement.toString());
        }
    }

    public class ViewPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            switch(viewType){
                case 0:
                    View itemView0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_node_page0, parent, false);
                    return new Page0ViewHolder(itemView0);
                case 1:
                    View itemView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_node_page1, parent, false);
                    return new Page1ViewHolder(itemView1);
                case 2:
                    View itemView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_node_page2, parent, false);
                    return new Page2ViewHolder(itemView2);
                case 3:
                    View itemView3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_node_page3, parent, false);
                    return new Page3ViewHolder(itemView3);
                case 4:
                    View itemView4 = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_node_page4, parent, false);
                    return new Page4ViewHolder(itemView4);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch(position){
                case 0:
                    Page0ViewHolder page0ViewHolder = (Page0ViewHolder) holder;

                    if(isUpdate){
                        page0ViewHolder.linkEditText.setText(updatingElement.linkUrl);
                    }

                    if(sharedLinkTemper != null){
                        page0ViewHolder.linkEditText.setText(sharedLinkTemper);
                    }
                    page0ViewHolder.thumbnailButton.setOnClickListener(v -> {
                        if(page0ViewHolder.checked){
                            page0ViewHolder.thumbnailTextView.setTextColor(getResources().getColor(R.color.textColor));
                            page0ViewHolder.thumbnailCheckImage.setVisibility(View.GONE);
                            page0ViewHolder.thumbnailInnerCardView.setCardBackgroundColor(getResources().getColor(R.color.colorBackground));
                            page0ViewHolder.checked = false;
                        }else{
                            page0ViewHolder.thumbnailTextView.setTextColor(getResources().getColor(R.color.colorBackground));
                            page0ViewHolder.thumbnailCheckImage.setVisibility(View.VISIBLE);
                            page0ViewHolder.thumbnailInnerCardView.setCardBackgroundColor(getResources().getColor(R.color.textColor));
                            page0ViewHolder.checked = true;
                        }
                    });
                    page0ViewHolder.button.setOnClickListener(v -> {
                        useYoutubeThumbnail = page0ViewHolder.checked;
                        linkUrl = page0ViewHolder.linkEditText.getText().toString().replace(" ", "");
                        binding.addElementActivityViewPager.setCurrentItem(1);
                    });
                    break;
                case 1:
                    Page1ViewHolder page1ViewHolder = (Page1ViewHolder) holder;
                    if(isUpdate){
                        try {
                            File file = new File(updatingElement.imgUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                            page1ViewHolder.imageView.setImageBitmap(bitmap);
                        } catch(FileNotFoundException e){
                            Log.e(TAG, "onBindViewHolder: " + e.toString());
                        }
                    }
                    page1ImageView = page1ViewHolder.imageView;
                    page1console = page1ViewHolder.consoleTextView;
                    if(useYoutubeThumbnail){
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    YoutubeAPI.getThumbnail(YoutubeAPI.extractYoutubeIdFromURL(linkUrl), new Callback() {
                                        @Override
                                        public void onCallback(Object object) {
                                            imgBitmap = (Bitmap) object;
                                            Message message = new Message();
                                            message.what = GET_THUMBNAIL_SUCCESS;
                                            handler.sendMessage(message);
                                        }
                                    });
                                }catch(Exception e){
                                    Log.e(TAG, "page1 get Thumbnail error: " + e.toString());
                                    Message message = new Message();
                                    message.what = GET_THUMBNAIL_FAILURE;
                                    handler.sendMessage(message);
                                }
                            }
                        });
                        thread.start();
                    }

                    page1ViewHolder.textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_IMAGE);
                        }
                    });

                    page1ViewHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            binding.addElementActivityViewPager.setCurrentItem(2);
                        }
                    });

                    break;
                case 2:
                    Page2ViewHolder page2ViewHolder = (Page2ViewHolder) holder;
                    if(isUpdate){
                        page2ViewHolder.editText.setText(updatingElement.content);
                    }
                    page2ViewHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            contentName = page2ViewHolder.editText.getText().toString();
                            binding.addElementActivityViewPager.setCurrentItem(3);
                        }
                    });
                    break;
                case 3:
                    Page3ViewHolder page3ViewHolder = (Page3ViewHolder) holder;
                    if(isUpdate){
                        page3ViewHolder.editText.setText(updatingElement.description);
                    }
                    page3ViewHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            description = page3ViewHolder.editText.getText().toString();
                            binding.addElementActivityViewPager.setCurrentItem(4);
                        }
                    });
                    break;
                case 4:
                    Page4ViewHolder page4ViewHolder = (Page4ViewHolder) holder;

                    List<Element> roots = db.elementDao().getElementWithParentId(-1);
                    FolderRecyclerViewAdapter adapter = new FolderRecyclerViewAdapter(getApplicationContext(), roots);

                    page4ViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    page4ViewHolder.recyclerView.setAdapter(adapter);

                    if(isUpdate){
                        try {

                            /**
                             *
                             *
                             *
                             * */

                            adapter.setClickedElem(updatingElement.id);
                        } catch(Exception e){
                            Log.e(TAG, "onBindViewHolder: ");
                        }
                    }

                    page4ViewHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try{
                                Element clicked = adapter.getClickedElem();

                                ChildIndex childIndex = db.childIndexDao().getChildIndexWithElementId(clicked.id);
                                int index = childIndex.index;

                                Element newElem = new Element();
                                newElem.parentId = clicked.id;
                                newElem.description = description;
                                newElem.content = contentName;
                                newElem.linkUrl = linkUrl;
                                newElem.lineNum = childIndex.index;

                                childIndex.index = index + 1;
                                db.childIndexDao().updateChildIndexes(childIndex);

                                if(imgBitmap != null) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                                        String path = getExternalCacheDir().getAbsolutePath();
                                        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS")) + ".png";

                                        Log.e("File path", "File path: " + path + "/" + fileName);
                                        try {
                                            FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
                                            imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
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

                                if(linkUrl == null || linkUrl.isEmpty()){
                                    newElem.isLink = false;
                                }else{
                                    newElem.isLink = true;
                                }

                                /**
                                 * @status: Done
                                 *
                                 * 유튜브의 공유기능으로 어플리케이션을 작동시키면 예상컨데 이 엑티비티가 메인엑티비티 실행 없이 켜질 것이다.
                                 * 따라서, 공유기능으로 어플리케이션을 실행시켰을 떄는 메인엑티비티로의 인텐트가 필요하다.
                                 * 인텐트 없이 엑티비티를 터미널시키면 어플리케이션이 종료 될 것이기 때문이다.
                                 *
                                 * 하지만 메인엑티비티 실행 후 이 엑티비티로 진행한 경우 메인엑티비티의 onActivityResult로 데이터를
                                 * 전달하는 편이 좋다.
                                 *
                                 * 위의 이유로 분기처리가 필요하며 기준은 백스텍에 메인엑티비티가 존재하는지 그렇지 않은지로 하는 것이 좋을 듯하다.
                                 * */



                                Log.e("AddElementActivity", "newElem: " + newElem.toString());
                                long rowId = db.elementDao().insertElement(newElem);

                                ChildIndex newChildIndex = new ChildIndex();
                                newChildIndex.elementId = (int) rowId;
                                db.childIndexDao().insertChildIndexes(newChildIndex);

                                ActivityManager activityManager = (ActivityManager) getApplication().getSystemService( Activity.ACTIVITY_SERVICE );
                                ActivityManager.RunningTaskInfo task = activityManager.getRunningTasks( 10 ).get(0);
                                if(task.numActivities == 1){
                                    // MainActivity doesn't exist.
                                    Intent intent = new Intent(AddElementActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }else{
                                    // MainActivity exists.
                                    Intent intent = new Intent();
                                    intent.putExtra("newNodeId", rowId);
                                    setResult(RESULT_OK, intent);
                                }

                                finish();

                            } catch(Exception e){
                                page4ViewHolder.textView.setText("have to choose folder");
                            }
                        }
                    });

                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class Page0ViewHolder extends RecyclerView.ViewHolder {
            boolean checked;
            CardView thumbnailButton;
            CardView thumbnailInnerCardView;
            ImageView thumbnailCheckImage;
            TextView thumbnailTextView;
            EditText linkEditText;
            TextView button;
            public Page0ViewHolder(@NonNull View itemView) {
                super(itemView);
                checked = false;
                thumbnailButton = itemView.findViewById(R.id.add_node_page_0_thumbnail_button);
                thumbnailInnerCardView = itemView.findViewById(R.id.add_node_page_0_thumbnail_inner_card_view);
                thumbnailCheckImage = itemView.findViewById(R.id.add_node_page_0_thumbnail_check_icon);
                thumbnailTextView = itemView.findViewById(R.id.add_node_page_0_thumbnail_text_view);
                linkEditText = itemView.findViewById(R.id.add_node_page_0_edit_text);
                button = itemView.findViewById(R.id.add_node_page_0_button);

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(linkEditText.getHint());
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#E57373")), 11, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                linkEditText.setHint(spannableStringBuilder);
            }
        }

        public class Page1ViewHolder extends RecyclerView.ViewHolder {
            TextView consoleTextView;
            TextView textView;
            ImageView imageView;
            TextView button;
            public Page1ViewHolder(@NonNull View itemView) {
                super(itemView);
                consoleTextView = itemView.findViewById(R.id.add_node_page_1_console_text_view);
                textView = itemView.findViewById(R.id.add_node_page_1_text_view);
                imageView = itemView.findViewById(R.id.add_node_page_1_image_view);
                button = itemView.findViewById(R.id.add_node_page_1_button);

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(textView.getText());
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#E57373")), 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setHint(spannableStringBuilder);
            }
        }

        public class Page2ViewHolder extends RecyclerView.ViewHolder {
            EditText editText;
            TextView button;
            public Page2ViewHolder(@NonNull View itemView) {
                super(itemView);
                editText = itemView.findViewById(R.id.add_node_page_2_edit_text);
                button = itemView.findViewById(R.id.add_node_page_2_button);

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editText.getHint());
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#E57373")), 6, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                editText.setHint(spannableStringBuilder);
            }
        }

        public class Page3ViewHolder extends RecyclerView.ViewHolder {
            EditText editText;
            TextView button;
            public Page3ViewHolder(@NonNull View itemView) {
                super(itemView);
                editText = itemView.findViewById(R.id.add_node_page_3_edit_text);
                button = itemView.findViewById(R.id.add_node_page_3_button);

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editText.getHint());
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#E57373")), 6, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                editText.setHint(spannableStringBuilder);
            }
        }

        public class Page4ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            RecyclerView recyclerView;
            TextView button;
            public Page4ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.add_node_page_4_text_view);
                recyclerView = itemView.findViewById(R.id.add_node_page_4_recycler_view);
                button = itemView.findViewById(R.id.add_node_page_4_button);

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(textView.getText());
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#055287")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setHint(spannableStringBuilder);
            }
        }
    }

    private void handleSharedText() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.e("handleSharedText", "action: " + action + ", type: " + type);
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.e("handleSharedText", "sharedText: " + sharedText);
            if (sharedText != null) {
                sharedLinkTemper = sharedText;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == GET_IMAGE) {
                Uri mediaUri = data.getData();
                try {
                    /*
                    InputStream inputStream = getBaseContext().getContentResolver().openInputStream(mediaUri);
                    imgBitmap = BitmapFactory.decodeStream(inputStream);
                     */

                    InputStream inputStream = getBaseContext().getContentResolver().openInputStream(mediaUri);
                    Bitmap targetBitmap = BitmapFactory.decodeStream(inputStream);
                    imgBitmap = SizeTool.resizing(targetBitmap);
                    page1ImageView.setImageBitmap(imgBitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}