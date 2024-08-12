package com.example.bbcnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView pubDateTextView;
    private Button linkButton;
    private Button favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        titleTextView = findViewById(R.id.title);
        descriptionTextView = findViewById(R.id.description);
        pubDateTextView = findViewById(R.id.pubDate);
        linkButton = findViewById(R.id.linkButton);
        favoriteButton = findViewById(R.id.button_favorite);

        Intent intent = getIntent();
        final String title = intent.getStringExtra("title");
        final String description = intent.getStringExtra("description");
        final String pubDate = intent.getStringExtra("pubDate");
        final String link = intent.getStringExtra("link");

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        pubDateTextView.setText(pubDate);

        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFavorites(title, description, pubDate, link);
            }
        });
    }

    private void saveToFavorites(String title, String description, String pubDate, String link) {
        SharedPreferences sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String favoriteArticle = title + ";" + description + ";" + pubDate + ";" + link;
        int nextIndex = sharedPreferences.getAll().size();
        editor.putString("favorite_" + nextIndex, favoriteArticle);
        editor.apply();

        Toast.makeText(this, "Article added to favorites!", Toast.LENGTH_SHORT).show();
    }
}