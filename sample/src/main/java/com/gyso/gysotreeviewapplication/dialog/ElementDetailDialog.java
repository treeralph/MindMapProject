package com.gyso.gysotreeviewapplication.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.gyso.gysotreeviewapplication.AddElementActivity;
import com.gyso.gysotreeviewapplication.Tool.Callback;
import com.gyso.gysotreeviewapplication.database.AppDatabase;
import com.gyso.gysotreeviewapplication.database.Element;
import com.gyso.gysotreeviewapplication.databinding.ActivityMainBinding;
import com.gyso.gysotreeviewapplication.databinding.DialogNodeDetailBinding;
import com.gyso.treeview.model.NodeModel;

import java.io.File;
import java.io.FileInputStream;

public class ElementDetailDialog extends Dialog {

    private static final String TAG = "ElementDetailDialog";

    public static final String ADD_ELEMENT_ID_EXTRA = "ADD_ELEMENT_ID_EXTRA";

    DialogNodeDetailBinding binding;
    Context context;

    AppDatabase db;
    /**
     * dialog의 메인 이미지는 디바이스에 저장된 이미지가 아닌 이미지로 가져오려고 했더니...
     * 생각해보니 internal storage에서 가져온 이미지랑 api를 통해 가져온 이미지랑 또 구분해야 하는 일이 생긴다.
     * 따라서, 해상도가 높은 이미지와 해상도가 낮은 이미지를 각각 저장하는 편이 좋을 것 같다.
     * */

    public ElementDetailDialog(@NonNull Context context, NodeModel<Element> elementNode, Callback deleteCallback) {
        super(context);
        binding = DialogNodeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.context = context;
        db = AppDatabase.getDBInstance(context);

        Element element = elementNode.getValue();

        try {
            File file = new File(element.imgUri);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            binding.dialogNodeDetailImageView.setImageBitmap(bitmap);
        } catch(Exception e){
            Log.e(TAG, "ElementDetailDialog: " + e.toString());
            binding.dialogNodeDetailImageView.setVisibility(View.GONE);
        }


        boolean isContent = true;
        boolean isDescription = true;
        if(!(element.content==null || element.content.equals(""))) {
            binding.dialogNodeDetailTitleTextView.setText(element.content);
        } else{
            binding.dialogNodeDetailTitleTextView.setVisibility(View.GONE);
            isContent = false;
        }
        if(!(element.description==null || element.description.equals(""))) {
            binding.dialogNodeDetailDescriptionTextView.setText(element.description);
        } else{
            binding.dialogNodeDetailDescriptionTextView.setVisibility(View.GONE);
            isDescription = false;
        }

        if(!isContent && isDescription){
            binding.dialogNodeDetailTitleTextView.setText("BLANK");
        }

        binding.dialogNodeDetailMoveButton.setOnClickListener(v -> {
            String link = element.linkUrl;
            if(isYoutubeUrl(link)) { openYoutube(link); }
            else{ openLink(link); }
        });
        binding.dialogNodeDetailImageView.setOnClickListener(v -> {

        });
        binding.dialogNodeDetailSettingButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddElementActivity.class);
            intent.putExtra(ADD_ELEMENT_ID_EXTRA, element.id);
            context.startActivity(intent);
            this.dismiss();
        });
        binding.dialogNodeDetailRemoveButton.setOnClickListener(v -> {
            deleteCallback.onCallback(elementNode);
            this.dismiss();
        });


    }

    public void openYoutube(String youtubeURI) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(youtubeURI));
        intent.setPackage("com.google.android.youtube");
        context.startActivity(intent);
    }

    public void openLink(String link){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(browserIntent);
    }

    public static boolean isYoutubeUrl(String link) {
        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
        return (!link.isEmpty() && link.matches(pattern)) ? true : false;
    }

}
