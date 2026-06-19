package com.example.event_management;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.event_management.models.Event;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail_container);

        Event event = (Event) getIntent().getSerializableExtra("event");
        if (event != null) {
            event_detail fragment = event_detail.newInstance(event);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        } else {
            finish();
        }
    }
}
