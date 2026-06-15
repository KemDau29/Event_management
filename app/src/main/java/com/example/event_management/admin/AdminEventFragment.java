package com.example.event_management.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.R;
import com.example.event_management.adapters.AdminEventAdapter;
import com.example.event_management.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.R;
import com.example.event_management.adapters.AdminEventAdapter;
import com.example.event_management.models.Category;
import com.example.event_management.models.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;

public class AdminEventFragment extends Fragment {

    private AdminEventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private Date selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_events, container, false);

        db = FirebaseFirestore.getInstance();
        ListView listView = view.findViewById(R.id.listAdminEvents);

        adapter = new AdminEventAdapter(requireContext(), new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onEdit(Event event) {
                showAddEditDialog(event);
            }

            @Override
            public void onDelete(Event event) {
                deleteEvent(event);
            }

            @Override
            public void onDetail(Event event) {
                AdminEventDetailFragment fragment = new AdminEventDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("eventId", event.getId());
                bundle.putString("eventTitle", event.getTitle());
                fragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        listView.setAdapter(adapter);
        loadEvents();
        loadCategories();

        view.findViewById(R.id.btnAddEvent).setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                cat.setId(doc.getId());
                categoryList.add(cat);
            }
        });
    }

    private void showAddEditDialog(Event event) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_event, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText edtTitle = dialogView.findViewById(R.id.edtAdminEventTitle);
        EditText edtDesc = dialogView.findViewById(R.id.edtAdminEventDesc);
        EditText edtLocation = dialogView.findViewById(R.id.edtAdminEventLocation);
        EditText edtLat = dialogView.findViewById(R.id.edtAdminEventLat);
        EditText edtLng = dialogView.findViewById(R.id.edtAdminEventLng);
        EditText edtPrice = dialogView.findViewById(R.id.edtAdminEventPrice);
        EditText edtRemaining = dialogView.findViewById(R.id.edtAdminEventRemaining);
        EditText edtImageUrl = dialogView.findViewById(R.id.edtAdminEventImageUrl);
        TextView tvDate = dialogView.findViewById(R.id.tvAdminEventDate);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerAdminEventCategory);

        // Setup Category Spinner
        List<String> catNames = new ArrayList<>();
        for (Category c : categoryList) catNames.add(c.getName());
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, catNames);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(catAdapter);

        if (event != null) {
            tvTitle.setText("Chỉnh sửa sự kiện");
            edtTitle.setText(event.getTitle());
            edtDesc.setText(event.getDescription());
            edtLocation.setText(event.getLocation());
            edtLat.setText(String.valueOf(event.getLatitude()));
            edtLng.setText(String.valueOf(event.getLongitude()));
            edtPrice.setText(String.valueOf(event.getPrice()));
            edtRemaining.setText(String.valueOf(event.getRemainingTickets()));
            edtImageUrl.setText(event.getImageUrl());
            selectedDate = event.getDate();
            if (selectedDate != null) tvDate.setText(sdf.format(selectedDate));
            
            // Set selection for category
            if (event.getCate() != null) {
                for (int i = 0; i < categoryList.size(); i++) {
                    if (categoryList.get(i).getId().equals(event.getCate().getId())) {
                        spinnerCat.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            tvTitle.setText("Thêm mới sự kiện");
            selectedDate = null;
        }

        tvDate.setOnClickListener(v -> showDateTimePicker(tvDate));

        dialogView.findViewById(R.id.btnAdminDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnAdminDialogSave).setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();
            String loc = edtLocation.getText().toString().trim();
            String latStr = edtLat.getText().toString().trim();
            String lngStr = edtLng.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String remainStr = edtRemaining.getText().toString().trim();
            String img = edtImageUrl.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || loc.isEmpty() || priceStr.isEmpty() || remainStr.isEmpty() || selectedDate == null || spinnerCat.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Event newEvent = (event != null) ? event : new Event();
            newEvent.setTitle(title);
            newEvent.setDescription(desc);
            newEvent.setLocation(loc);

            // Tự động tìm tọa độ nếu để trống hoặc bằng 0
            if (latStr.isEmpty() || lngStr.isEmpty() || (Double.parseDouble(latStr) == 0 && Double.parseDouble(lngStr) == 0)) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(loc, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        newEvent.setLatitude(addresses.get(0).getLatitude());
                        newEvent.setLongitude(addresses.get(0).getLongitude());
                    }
                } catch (IOException e) {
                    android.util.Log.e("GEOCODER", "Lỗi tìm tọa độ: " + e.getMessage());
                }
            } else {
                newEvent.setLatitude(Double.parseDouble(latStr));
                newEvent.setLongitude(Double.parseDouble(lngStr));
            }

            newEvent.setPrice(Integer.parseInt(priceStr));
            newEvent.setRemainingTickets(Integer.parseInt(remainStr));
            newEvent.setImageUrl(img);
            newEvent.setDate(selectedDate);
            
            int catIndex = spinnerCat.getSelectedItemPosition();
            DocumentReference catRef = db.collection("categories").document(categoryList.get(catIndex).getId());
            newEvent.setCate(catRef);

            if (event == null) {
                db.collection("events").add(newEvent).addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Đã thêm sự kiện", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            } else {
                db.collection("events").document(event.getId()).set(newEvent).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã cập nhật sự kiện", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }
        });

        dialog.show();
    }

    private void showDateTimePicker(TextView tvDate) {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) calendar.setTime(selectedDate);

        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                selectedDate = calendar.getTime();
                tvDate.setText(sdf.format(selectedDate));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadEvents() {
        db.collection("events").addSnapshotListener((value, error) -> {
            if (value != null) {
                eventList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    event.setId(doc.getId());
                    eventList.add(event);
                }
                adapter.setEventList(eventList);
            }
        });
    }

    private void deleteEvent(Event event) {
        db.collection("events").document(event.getId()).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xóa sự kiện", Toast.LENGTH_SHORT).show());
    }
}
