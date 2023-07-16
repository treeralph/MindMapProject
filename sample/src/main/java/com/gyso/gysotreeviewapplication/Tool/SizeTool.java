package com.gyso.gysotreeviewapplication.Tool;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

public class SizeTool {

    private static final String TAG = "SizeTool";
    private static final float sizeThreshold = 512 * 512;

    public static float resizingQuality(Bitmap target){
        float threshold = sizeThreshold;
        float targetSize = target.getWidth() * target.getHeight();
        Log.e(TAG, "target width: " + String.valueOf(target.getWidth()) + "\ntarget height: " + String.valueOf(target.getHeight()));
        if(threshold > targetSize){
            return 1;
        }else{
            float result = (float) Math.sqrt(targetSize / threshold);
            Log.e(TAG, "quality result: " + String.valueOf(result));
            return result;
        }
    }

    /**
     * todo: current working method;
     * */

    public static Bitmap resizing(@NonNull Bitmap target) {
        float threshold = sizeThreshold;
        float targetSize = target.getWidth() * target.getHeight();
        Log.e(TAG, "target width: " + String.valueOf(target.getWidth()) + "\ntarget height: " + String.valueOf(target.getHeight()));
        float constant = (float) Math.sqrt(targetSize / threshold);
        Bitmap resizingTarget = Bitmap.createScaledBitmap(target,
                (int) (target.getWidth() / constant),
                (int) (target.getHeight() / constant),
                true);
        return resizingTarget;
    }
}
