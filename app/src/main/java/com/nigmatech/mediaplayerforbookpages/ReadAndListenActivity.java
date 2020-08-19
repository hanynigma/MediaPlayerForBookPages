package com.nigmatech.mediaplayerforbookpages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ReadAndListenActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<ListViewData> listViewData = new ArrayList<>();
    ListAdapter adapter;

    String mediaNumClicked;
    String mediaFileNameClicked;

    int positionClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_and_listen);


        listView = findViewById(R.id.listView);


        final String[] mediaFileNameStringArray = getResources().getStringArray(R.array.audioNameString);
        final String[] mediaFileName = getResources().getStringArray(R.array.audioFileName);

        for (int i = 0; i < mediaFileNameStringArray.length; i++) {


            ListViewData listViewData = new ListViewData(mediaFileNameStringArray[i], mediaFileName[i]);

            this.listViewData.add(listViewData);

        }

        adapter = new ListAdapter(this, listViewData);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                positionClicked = position;
                mediaNumClicked = mediaFileName[position];
                mediaFileNameClicked = mediaFileNameStringArray[position];

                final Intent i = new Intent(getBaseContext(), MediaPlayerActivity.class);

                i.putExtra("mediaNumberClicked", mediaNumClicked);
                i.putExtra("mediaFileNameClicked", mediaFileNameClicked);
                i.putExtra("positionClicked", positionClicked);


                startActivity(i);
            }
        });

    }
}
