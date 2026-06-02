package com.example.event_management.repositories;

import com.example.event_management.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnEventsLoadedListener {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
    }

    public void getAllEvents(OnEventsLoadedListener listener) {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> eventList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setId(document.getId());
                            eventList.add(event);
                        }
                        listener.onSuccess(eventList);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }
}