package com.example.event_management.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.event_management.models.Event;
import com.example.event_management.repositories.EventRepository;
import java.util.List;

public class EventViewModel extends ViewModel {
    private final EventRepository repository = new EventRepository();
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();

    public MutableLiveData<List<Event>> getEvents() {
        repository.getAllEvents(new EventRepository.OnEventsLoadedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                eventsLiveData.setValue(events);
            }

            @Override
            public void onFailure(Exception e) {
                eventsLiveData.setValue(null);
            }
        });
        return eventsLiveData;
    }
}