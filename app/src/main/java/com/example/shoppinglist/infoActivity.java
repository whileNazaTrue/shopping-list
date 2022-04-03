package com.example.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class infoActivity extends AppCompatActivity {
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Bundle bundle = getIntent().getExtras();
        ArrayList<String> categoryList = bundle.getStringArrayList("ARRAYLIST_SENDER");

        listView = findViewById(R.id.categoryList);

        ArrayAdapter<String> categories = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,categoryList);
        listView.setAdapter(categories);
    }
}