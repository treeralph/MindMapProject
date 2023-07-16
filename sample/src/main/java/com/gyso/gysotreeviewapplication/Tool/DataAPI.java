package com.gyso.gysotreeviewapplication.Tool;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataAPI {

    private static final String TAG = "DataAPI";
    public static final String NAVER_SHORTEN_URL = "https://naver.me/";

    public static void linkParser(String link, Callback callback){
        if(isNaver(link)){
            operateForNaver(link, new Callback() {
                @Override
                public void onCallback(Object object) {
                    String extractLink = (String) object;
                    operate(extractLink, new Callback() {
                        @Override
                        public void onCallback(Object object) {
                            LinkCapsule capsule = (LinkCapsule) object;
                            callback.onCallback(capsule);
                        }
                    });
                }
            });
        }else{
            operate(link, new Callback() {
                @Override
                public void onCallback(Object object) {
                    LinkCapsule capsule = (LinkCapsule) object;
                    callback.onCallback(capsule);
                }
            });
        }
    }

    public static void operateForNaver(String link, Callback callback){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(link).get();
                    Log.e(TAG, doc.toString());
                    Elements metaTags = doc.select("meta");
                    for(Element metaTag: metaTags){
                        if(metaTag.attr("property").equals("al:android:url")){
                            String content = metaTag.attr("content");

                            content = content.replace("%3A", ":");
                            content = content.replace("%2F", "/");
                            content = content.replace("%3D", "=");
                            content = content.replace("%3F", "?");
                            content = content.replace("%26", "&");

                            Log.e(TAG, "content: " + content);

                            String regex = "\\?url=([^&]*)&";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(content);
                            if(matcher.find()){
                                String result = matcher.group(1);
                                callback.onCallback(result);
                            }else{
                                callback.onCallback(null);
                            }
                        }
                    }
                } catch(Exception e){
                    Log.e(TAG, "operateForNaver run: " + e.toString());
                }
            }
        });
        thread.start();
    }

    public static void operate(String link, Callback callback) {
        String url = link; // 대상 링크의 URL
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements metaTags = doc.select("meta[property^=og:]");

                    String title = null;
                    String description = null;
                    String imageUrl = null;

                    for (Element metaTag : metaTags) {
                        String property = metaTag.attr("property");
                        String content = metaTag.attr("content");

                        if (property.equals("og:title")) {
                            title = content;
                        } else if (property.equals("og:description")) {
                            description = content;
                        } else if (property.equals("og:image")) {
                            imageUrl = content;
                        }
                    }

                    System.out.println("제목: " + title);
                    System.out.println("설명: " + description);
                    System.out.println("이미지 URL: " + imageUrl);

                    Log.e(TAG, "title: " + title);
                    Log.e(TAG, "description: " + description);
                    Log.e(TAG, "image url: " + imageUrl);

                    LinkCapsule info = new LinkCapsule();
                    info.link = link;
                    info.title = title;
                    info.description = description;
                    info.imageUrl = imageUrl;

                    callback.onCallback(info);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static boolean isNaver(String link){
        boolean result = link.startsWith(NAVER_SHORTEN_URL);
        Log.e("ASDF", "isNaver: " + result);
        return result;
    }

    public static class LinkCapsule {
        public String title;
        public String description;
        public String imageUrl;
        public String link;
        public LinkCapsule(){

        }

        @Override
        public String toString() {
            return "LinkCapsule{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    '}';
        }
    }
}
