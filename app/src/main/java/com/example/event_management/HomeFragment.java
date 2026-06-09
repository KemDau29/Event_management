package com.example.event_management;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.event_management.adapters.FeaturedEventAdapter;
import com.example.event_management.adapters.UpcomingEventAdapter;
import com.example.event_management.models.Event;
import com.example.event_management.viewmodels.EventViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewUpcomingEvents;
    private UpcomingEventAdapter upcomingAdapter;
    private FeaturedEventAdapter featuredAdapter;
    private EventViewModel viewModel;
    private ViewPager2 viewPagerFeatured;
    private TextView tvHomeUserName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> displayList = new ArrayList<>();
    private String currentSearchText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tvHomeUserName = view.findViewById(R.id.tvHomeUserName);

        loadUserData();

        EditText edtSearch = view.findViewById(R.id.edtSearch);
        View btnFilter = view.findViewById(R.id.btnFilter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase().trim();
                filterAndSort();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilter.setOnClickListener(v -> showFilterMenu(v));

        View btnNotification = view.findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> openNotifications());
        }

        recyclerViewUpcomingEvents = view.findViewById(R.id.listUpcomingEvents);

        recyclerViewUpcomingEvents.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerViewUpcomingEvents.setNestedScrollingEnabled(false);


        upcomingAdapter = new UpcomingEventAdapter(requireContext(), this::openDetail);
        recyclerViewUpcomingEvents.setAdapter(upcomingAdapter);


        viewPagerFeatured = view.findViewById(R.id.viewPagerFeatured);
        featuredAdapter = new FeaturedEventAdapter(requireContext(), this::openDetail);
        viewPagerFeatured.setAdapter(featuredAdapter);
        viewPagerFeatured.setOffscreenPageLimit(3);

        TextView tvViewAllUpcoming = view.findViewById(R.id.tvViewAllUpcoming);
        if (tvViewAllUpcoming != null) {
            tvViewAllUpcoming.setOnClickListener(v -> {
                EventFragment eventFragment = new EventFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, eventFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                allEvents.clear();
                allEvents.addAll(events);
                filterAndSort();

                List<Event> sortedEvents = new ArrayList<>(events);
                Collections.sort(sortedEvents, (e1, e2) -> Integer.compare(e2.getAttendants(), e1.getAttendants()));

                int maxFeaturedCount = Math.min(sortedEvents.size(), 5);
                List<Event> featuredEvents = sortedEvents.subList(0, maxFeaturedCount);

                featuredAdapter.setFeaturedList(featuredEvents);
            }
        });

        return view;
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Giá tăng dần");
        popup.getMenu().add("Giá giảm dần");
        popup.getMenu().add("Mới nhất");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Giá tăng dần")) {
                sortList("price_asc");
            } else if (title.equals("Giá giảm dần")) {
                sortList("price_desc");
            } else if (title.equals("Mới nhất")) {
                sortList("newest");
            }
            return true;
        });
        popup.show();
    }

    private void sortList(String type) {
        switch (type) {
            case "price_asc":
                Collections.sort(displayList, (e1, e2) -> Integer.compare(e1.getPrice(), e2.getPrice()));
                break;
            case "price_desc":
                Collections.sort(displayList, (e1, e2) -> Integer.compare(e2.getPrice(), e1.getPrice()));
                break;
            case "newest":
                Collections.sort(displayList, (e1, e2) -> {
                    if (e1.getDate() == null || e2.getDate() == null) return 0;
                    return e2.getDate().compareTo(e1.getDate());
                });
                break;
        }
        upcomingAdapter.setEventList(displayList);
    }

    private void filterAndSort() {
        displayList.clear();
        Date now = new Date();
        for (Event event : allEvents) {
            // Lọc theo ngày: chỉ lấy sự kiện ở tương lai
            boolean isUpcoming = event.getDate() != null && event.getDate().after(now);
            
            // Lọc theo từ khóa tìm kiếmf
            boolean matchesSearch = currentSearchText.isEmpty() || 
                                     event.getTitle().toLowerCase().contains(currentSearchText);

            if (isUpcoming && matchesSearch) {
                displayList.add(event);
            }
        }
        upcomingAdapter.setEventList(displayList);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("fullname");
                            if (name != null && !name.isEmpty()) {
                                tvHomeUserName.setText(name);
                            } else {
                                tvHomeUserName.setText("Người dùng");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvHomeUserName.setText("Xin chào!");
                    });
        } else {
            tvHomeUserName.setText("Khách");
        }
    }

    private void openDetail(Event event) {
        event_detail detailFragment = event_detail.newInstance(event);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void openNotifications() {
        NotificationsFragment notificationsFragment = new NotificationsFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, notificationsFragment)
                .addToBackStack(null)
                .commit();
    }
}