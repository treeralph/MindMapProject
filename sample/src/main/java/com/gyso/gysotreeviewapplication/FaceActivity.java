package com.gyso.gysotreeviewapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.gyso.gysotreeviewapplication.database.AppDatabase;
import com.gyso.gysotreeviewapplication.database.ChildIndex;
import com.gyso.gysotreeviewapplication.database.Element;
import com.gyso.gysotreeviewapplication.database.User;
import com.gyso.gysotreeviewapplication.databinding.ActivityFaceBinding;

import java.util.List;

public class FaceActivity extends AppCompatActivity {

    int START_FAILURE = -1;
    int START_SUCCESS = 1;

    ActivityFaceBinding binding;
    AppDatabase db;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDBInstance(this);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                if(message.what == START_FAILURE){
                    binding.faceActivityConsoleTextView.setText("There is an error!");
                }else{
                    Intent intent = new Intent(FaceActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        activityInitializer();
    }

    private void activityInitializer(){
        List<User> users = db.userDao().getAllUsers();
        if(users.size() == 0){
            User currentUser = new User();
            db.userDao().insertUsers(currentUser);
        }

        List<Element> elements = db.elementDao().getElementWithParentId(-1);
        if(elements.size() > 0){
            Intent intent = new Intent(FaceActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.faceActivityButton.setOnClickListener(v -> {
            binding.faceActivityConsoleTextView.setText("");

            String folderName = binding.faceActivityEditText.getText().toString().replace(" ", "");

            Element element = new Element();
            element.parentId = -1;
            element.isImg = false;
            element.isLink = false;
            element.nodeInfo = "auto";
            element.content = folderName;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    try{
                        int elementId = (int) db.elementDao().insertElement(element);

                        ChildIndex childIndex = new ChildIndex();
                        childIndex.elementId = elementId;
                        db.childIndexDao().insertChildIndexes(childIndex);

                        message.what = START_SUCCESS;
                        handler.sendMessage(message);
                    }catch(SQLiteException e){
                        message.what = START_FAILURE;
                        handler.sendMessage(message);
                    }
                }
            });
            thread.start();
        });
    }
}