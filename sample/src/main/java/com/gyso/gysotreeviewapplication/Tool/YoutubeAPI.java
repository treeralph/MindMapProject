package com.gyso.gysotreeviewapplication.Tool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class YoutubeAPI {

    private static String TAG = YoutubeAPI.class.getName();
    public static String THUMBNAIL_API = "https://img.youtube.com/vi/";
    public static String GET_INFO_API = "https://noembed.com/embed?url=";
    public static String CHROME_PACKAGE_NAME = "com.android.chrome";

    /**
     * @INFO_JSON_FORMAT:
     * {
     *      "url":"https://www.youtube.com/watch?v=mpnaax3DvfU",
     *      "version":"1.0",
     *      "thumbnail_height":360,
     *      "title":"진기명기",
     *      "author_name":"런닝맨 - 스브스 공식 채널",
     *      "author_url":"https://www.youtube.com/@SBSRunningMan",
     *      "thumbnail_url":"https://i.ytimg.com/vi/mpnaax3DvfU/hqdefault.jpg",
     *      "html":"<iframe width=\"200\" height=\"113\" src=\"https://www.youtube.com/embed/mpnaax3DvfU?feature=oembed\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen title=\"진기명기\"></iframe>",
     *      "height":113,
     *      "provider_url":"https://www.youtube.com/",
     *      "type":"video",
     *      "provider_name":"YouTube",
     *      "thumbnail_width":480,
     *      "width":200
     * }
     * */

    public static void getInfo(String youtubeURL, Callback callback){
        String getInfoQuery = GET_INFO_API + youtubeURL;
        try{
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();

            URL url = new URL(getInfoQuery);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn != null) {
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "response code: " + String.valueOf(responseCode));
                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.w(TAG, line + "\n");
                    stringBuilder.append(line + "\n");
                }
                String result = stringBuilder.toString();
                JSONObject result2Json = new JSONObject(result);
                try {
                    YoutubeInfo youtubeInfo = new YoutubeInfo(result2Json);
                    callback.onCallback(youtubeInfo.title);
                } catch(Exception e){
                    callback.onCallback(null);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public static void getThumbnail(String youtubeId, Callback callback){

        String getThumbnailQuery = THUMBNAIL_API + youtubeId + "/0.jpg";
        String getThumbnailQueryHQ = THUMBNAIL_API + youtubeId + "/mqdefault.jpg";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getThumbnailQueryHQ)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                InputStream inputStream = responseBody.byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                callback.onCallback(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String extractYoutubeIdFromURL(String string) throws Exception{
        String regex = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/\\w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#&?]*).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        if(matcher.find()){
            return matcher.group(7);
        }else{
            throw new Exception("Not Found Exception");
        }
    }

    public static void openYoutube(Context context, String youtubeURI) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(youtubeURI));
        intent.setPackage("com.google.android.youtube");
        context.startActivity(intent);
    }

    public static class YoutubeInfo{
        String url;
        String title;
        String authorName;
        String authorUrl;
        String thumbnailUrl;
        public YoutubeInfo(){

        }
        public YoutubeInfo(JSONObject jsonObject) throws Exception{
            this.url = jsonObject.getString("url");
            this.title = jsonObject.getString("title");
            this.authorName = jsonObject.getString("author_name");
            this.authorUrl = jsonObject.getString("author_url");
            this.thumbnailUrl = jsonObject.getString("thumbnail_url");
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getAuthorUrl() {
            return authorUrl;
        }

        public void setAuthorUrl(String authorUrl) {
            this.authorUrl = authorUrl;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}
