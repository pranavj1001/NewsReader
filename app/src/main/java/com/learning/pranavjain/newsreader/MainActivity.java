package com.learning.pranavjain.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    Map<Integer, String> articleUrls = new HashMap<Integer, String>();
    Map<Integer, String> articleTitles = new HashMap<Integer, String>();
    ArrayList<Integer> articleIds = new ArrayList<Integer>();

    SQLiteDatabase articlesDB;

    ArrayList<String> listViewTitles = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    ArrayList<String> listViewUrls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listViewTitles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Log.i("articleURL",listViewUrls.get(position));
                Intent i = new Intent(getApplicationContext(), Main2Activity.class);
                i.putExtra("articleUrl",listViewUrls.get(position));
                startActivity(i);

            }
        });

        articlesDB = this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, url VARCHAR, title VARCHAR, content VARCHAR)");

        updateListView();

        DownloadTask downloadTask = new DownloadTask();
        try {

            String result = downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            //Log.i("Result",result);

            articlesDB.execSQL("DELETE FROM articles");

            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < 20; i++){

                String articleTitle = "";
                String articleURL = "";

                String articleId = jsonArray.getString(i);
                articleIds.add(Integer.valueOf(articleId));

                //Log.i("Article","Reached HERE");

                DownloadTask getArticle = new DownloadTask();

                String articleInfo  = getArticle.execute("https://hacker-news.firebaseio.com/v0/item/"+articleId+".json?print=pretty").get();
                JSONObject jsonObject = new JSONObject(articleInfo);

                //Log.i("jsonObject",jsonObject.toString());

                if(jsonObject.has("title")){
                    articleTitle = jsonObject.getString("title");
                    articleTitles.put(Integer.valueOf(articleId),articleTitle);
                }
                if(jsonObject.has("url")){
                    articleURL = jsonObject.getString("url");
                    articleUrls.put(Integer.valueOf(articleId),articleURL);
                }

                String sql = "INSERT INTO articles (articleId, url, title) VALUES (?, ?, ?)";
                SQLiteStatement sqLiteStatement = articlesDB.compileStatement(sql);
                sqLiteStatement.bindString(1,articleId);
                sqLiteStatement.bindString(2,articleURL);
                sqLiteStatement.bindString(3,articleTitle);
                sqLiteStatement.execute();

                //Log.i("Article",articleTitle);
                //Log.i("Article",articleURL);

            }

            //Log.i("Article","Reached HERE");
            //Log.i("Article Id",articleIds.toString());
            //Log.i("Article Title",articleTitles.toString());
            //Log.i("Article Urls",articleUrls.toString());

           updateListView();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateListView() {

        try {

            Cursor cursor = articlesDB.rawQuery("SELECT * FROM articles ORDER BY articleId DESC", null);
            int articleIndex = cursor.getColumnIndex("articleId");
            int urlIndex = cursor.getColumnIndex("url");
            int titleIndex = cursor.getColumnIndex("title");

            cursor.moveToFirst();
            int j = 0;

            listViewTitles.clear();
            listViewUrls.clear();

            while (cursor != null) {
                j++;

                listViewTitles.add(cursor.getString(titleIndex));
                listViewUrls.add(cursor.getString(urlIndex));

                //Log.i("DB - articleId ", String.valueOf(cursor.getInt(articleIndex)));
                //Log.i("DB - url ", cursor.getString(urlIndex));
                //Log.i("DB - title ", cursor.getString(titleIndex));
                if (j > 19) break;
                else cursor.moveToNext();
            }

            arrayAdapter.notifyDataSetChanged();

        }catch (Exception e){
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
