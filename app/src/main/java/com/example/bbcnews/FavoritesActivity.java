package com.example.bbcnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {

    private ListView favoritesListView;
    private List<String> favoritesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesListView = findViewById(R.id.favorites_list);
        favoritesList = new ArrayList<>();

        loadFavorites();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favoritesList);
        favoritesListView.setAdapter(adapter);

        favoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] articleData = favoritesList.get(position).split(";");
                Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
                intent.putExtra("title", articleData[0]);
                intent.putExtra("description", articleData[1]);
                intent.putExtra("pubDate", articleData[2]);
                intent.putExtra("link", articleData[3]);
                startActivity(intent);
            }
        });
    }

    private void loadFavorites() {
        SharedPreferences sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            favoritesList.add(entry.getValue().toString());
        }
    }
}
