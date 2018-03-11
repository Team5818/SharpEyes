package org.rivierarobotics.sharpeyes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * The primary activity.
 */
public class MatchEditActivity extends AppCompatActivity {

    private RecyclerView matchesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headered_list);

        matchesView = findViewById(R.id.listView);
        matchesView.setHasFixedSize(true);
        matchesView.setLayoutManager(new LinearLayoutManager(this));

    }

}
