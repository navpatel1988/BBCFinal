package com.example.bbcnews;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ListView listView;
    private ProgressBar progressBar;
    private Button loadNewsButton;
    private Button viewFavoritesButton;
    private List<NewsArticle> newsArticles;
    private ArrayAdapter<String> adapter;
    private List<String> titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        loadNewsButton = findViewById(R.id.button);
        viewFavoritesButton = findViewById(R.id.button_view_favorites);
        newsArticles = new ArrayList<>();
        titles = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);

        loadNewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadNewsTask().execute();
            }
        });

        viewFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("title", newsArticles.get(position).getTitle());
                intent.putExtra("description", newsArticles.get(position).getDescription());
                intent.putExtra("pubDate", newsArticles.get(position).getPubDate());
                intent.putExtra("link", newsArticles.get(position).getLink());
                startActivity(intent);
            }
        });
    }

    private class LoadNewsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            loadNewsButton.setVisibility(View.GONE); // Hide the button while loading
            viewFavoritesButton.setVisibility(View.GONE); // Hide the button while loading
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER) {

                    String newUrl = connection.getHeaderField("Location");
                    Log.d(TAG, "Redirect to URL: " + newUrl);

                    connection = (HttpURLConnection) new URL(newUrl).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    responseCode = connection.getResponseCode();
                    Log.d(TAG, "Response Code after redirect: " + responseCode);
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Failed to fetch news: HTTP response code " + responseCode);
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                Log.d(TAG, "Response: " + response.toString());

                return response.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error fetching news", e);
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing reader", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            if (result != null) {
                parseXML(result);
            } else {
                Toast.makeText(MainActivity.this, "Failed to load news", Toast.LENGTH_SHORT).show();
                loadNewsButton.setVisibility(View.VISIBLE); // Show button again if load fails
                viewFavoritesButton.setVisibility(View.VISIBLE); // Show button again if load fails
            }
        }
    }

    private void parseXML(String xml) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new BufferedReader(new StringReader(xml)));
            int eventType = parser.getEventType();
            NewsArticle currentArticle = null;
            String text = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("item")) {
                            currentArticle = new NewsArticle("", "", "", "");
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (currentArticle != null) {
                            if (tagName.equalsIgnoreCase("title")) {
                                currentArticle.setTitle(text);
                            } else if (tagName.equalsIgnoreCase("description")) {
                                currentArticle.setDescription(text);
                            } else if (tagName.equalsIgnoreCase("pubDate")) {
                                currentArticle.setPubDate(text);
                            } else if (tagName.equalsIgnoreCase("link")) {
                                currentArticle.setLink(text);
                            } else if (tagName.equalsIgnoreCase("item")) {
                                newsArticles.add(currentArticle);
                                titles.add(currentArticle.getTitle());
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing XML", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Show buttons again when returning to MainActivity
        loadNewsButton.setVisibility(View.VISIBLE);
        viewFavoritesButton.setVisibility(View.VISIBLE);
    }
}