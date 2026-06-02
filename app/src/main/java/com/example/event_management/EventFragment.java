package com.example.event_management;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.event_management.adapters.CategoryAdapter;
import com.example.event_management.adapters.EventAdapter;
import com.example.event_management.models.Category;
import com.example.event_management.models.Event;
import com.example.event_management.viewmodels.EventViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventFragment extends Fragment {

    private EventAdapter adapter;
    private EventViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private FirebaseFirestore db;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> displayList = new ArrayList<>();
    private String currentSearchText = "";
    private Category currentCategory = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event, container, false);
        db = FirebaseFirestore.getInstance();

        // Ánh xạ thanh search và nút filter
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

        btnFilter.setOnClickListener(this::showFilterMenu);

        // 1. Ánh xạ và thiết lập ListView Events
        adapter = new EventAdapter(requireContext());
        ListView listEvents = view.findViewById(R.id.listEvents);
        listEvents.setAdapter(adapter);

        // 2. Ánh xạ và thiết lập RecyclerView Categories
        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList, category -> {
            filterEventsByCategory(category);
        });
        rvCategories.setAdapter(categoryAdapter);

        // 3. Tải danh sách Categories từ Firestore
        loadCategories();

        // 4. Lấy dữ liệu Events từ ViewModel
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                allEvents.clear();
                allEvents.addAll(events);
                filterAndSort();
            }
        });

        // 5. Sự kiện click item Event
        listEvents.setOnItemClickListener((parent, view1, position, id) -> {
            Event event = (Event) adapter.getItem(position);
            if (event != null) {
                event_detail detailFragment = event_detail.newInstance(event);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
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
            switch (title) {
                case "Giá tăng dần":
                    sortList("price_asc");
                    break;
                case "Giá giảm dần":
                    sortList("price_desc");
                    break;
                case "Mới nhất":
                    sortList("newest");
                    break;
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
        adapter.setEventList(displayList);
    }

    private void filterAndSort() {
        displayList.clear();
        for (Event event : allEvents) {
            boolean matchesSearch = event.getTitle().toLowerCase().contains(currentSearchText);
            boolean matchesCategory = (currentCategory == null || currentCategory.getId().equals("all")) 
                                     || (event.getCate() != null && event.getCate().getId().equals(currentCategory.getId()));
            
            if (matchesSearch && matchesCategory) {
                displayList.add(event);
            }
        }
        adapter.setEventList(displayList);
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            
            // Thêm option "Tất cả" đầu tiên
            Category allCategory = new Category();
            allCategory.setId("all");
            allCategory.setName("Tất cả");
            categoryList.add(allCategory);

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category category = doc.toObject(Category.class);
                category.setId(doc.getId());
                categoryList.add(category);
            }
            categoryAdapter.notifyDataSetChanged();
        });
    }

    private void filterEventsByCategory(Category category) {
        currentCategory = category;
        filterAndSort();
    }
}
