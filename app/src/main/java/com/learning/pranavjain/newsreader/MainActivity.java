package com.learning.pranavjain.newsreader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask downloadTask = new DownloadTask();
        try {

            String result = downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            //Log.i("Result",result);

            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++){

                String articleId = jsonArray.getString(i);

                //Log.i("Article","Reached HERE");

                DownloadTask getArticle = new DownloadTask();

                String articleInfo  = getArticle.execute("https://hacker-news.firebaseio.com/v0/item/"+articleId+".json?print=pretty").get();
                JSONObject jsonObject = new JSONObject(articleInfo);

                String articleTitle = jsonObject.getString("title");
                String articleURL = jsonObject.getString("url");

                //Log.i("Article",articleTitle);
                //Log.i("Article",articleURL);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection;

            try {

                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int data = inputStreamReader.read();
                while(data != -1){

                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return result;
        }

    }

}
