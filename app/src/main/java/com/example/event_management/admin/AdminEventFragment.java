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
import com.example.event_management.models.Organization;
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

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminEventFragment extends Fragment {

    private AdminEventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private List<Organization> organizationList = new ArrayList<>();
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private Date selectedDate;
    private Date selectedOpenDate;
    private Date selectedCloseDate;
    private Date selectedEarlyBirdOpen;
    private Date selectedEarlyBirdDeadline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_events, container, false);

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = view.findViewById(R.id.listAdminEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

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

        recyclerView.setAdapter(adapter);
        
        // Setup Swipe to Reveal
        setupSwipeToReveal(recyclerView);

        loadEvents();
        loadCategories();
        loadOrganizations();

        view.findViewById(R.id.btnAddEvent).setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    private void setupSwipeToReveal(RecyclerView recyclerView) {
        // Cho phép vuốt cả TRÁI (để mở) và PHẢI (để đóng)
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Sau khi vuốt xong (trái hoặc phải), chúng ta làm mới item để nó "khớp" vào trạng thái mới
                adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View foregroundView = ((AdminEventAdapter.AdminEventViewHolder) viewHolder).cardForeground;
                    
                    // Giới hạn khoảng cách vuốt sang trái (tối đa hiện đủ 2 nút)
                    float maxSwipeWidth = -getResources().getDisplayMetrics().density * 160;
                    
                    // Logic: dX < 0 là đang vuốt trái, dX > 0 là đang vuốt phải
                    float translationX;
                    if (dX < 0) {
                        translationX = Math.max(dX, maxSwipeWidth);
                    } else {
                        // Chặn không cho vuốt quá màn hình sang phải
                        translationX = Math.min(dX, 0); 
                    }
                    
                    getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, translationX, dY, actionState, isCurrentlyActive);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };
        
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
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

    private void loadOrganizations() {
        db.collection("organizations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            organizationList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Organization org = doc.toObject(Organization.class);
                org.setId(doc.getId());
                organizationList.add(org);
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
        View layoutRemaining = dialogView.findViewById(R.id.layoutAdminEventRemaining);
        com.google.android.material.switchmaterial.SwitchMaterial switchIsLimited = dialogView.findViewById(R.id.switchIsLimited);
        EditText edtImageUrl = dialogView.findViewById(R.id.edtAdminEventImageUrl);
        TextView tvDate = dialogView.findViewById(R.id.tvAdminEventDate);
        TextView tvOpen = dialogView.findViewById(R.id.tvAdminTicketOpen);
        TextView tvClose = dialogView.findViewById(R.id.tvAdminTicketClose);
        TextView tvEarlyBirdOpen = dialogView.findViewById(R.id.tvAdminEarlyBirdOpen);
        TextView tvEarlyBirdDeadline = dialogView.findViewById(R.id.tvAdminEarlyBirdDeadline);
        EditText edtEarlyBirdPrice = dialogView.findViewById(R.id.edtAdminEarlyBirdPrice);
        EditText edtEarlyBirdLimit = dialogView.findViewById(R.id.edtAdminEarlyBirdLimit);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerAdminEventCategory);
        Spinner spinnerOrg = dialogView.findViewById(R.id.spinnerAdminEventOrg);

        // Logic toggle limited tickets
        switchIsLimited.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutRemaining.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Setup Category Spinner
        List<String> catNames = new ArrayList<>();
        for (Category c : categoryList) catNames.add(c.getName());
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, catNames);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(catAdapter);

        // Setup Organization Spinner
        List<String> orgNames = new ArrayList<>();
        orgNames.add("Không có đơn vị"); // Option for no organization
        for (Organization o : organizationList) orgNames.add(o.getName());
        ArrayAdapter<String> orgAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, orgNames);
        orgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrg.setAdapter(orgAdapter);

        if (event != null) {
            tvTitle.setText("Chỉnh sửa sự kiện");
            edtTitle.setText(event.getTitle());
            edtDesc.setText(event.getDescription());
            edtLocation.setText(event.getLocation());
            edtLat.setText(String.valueOf(event.getLatitude()));
            edtLng.setText(String.valueOf(event.getLongitude()));
            edtPrice.setText(String.valueOf(event.getPrice()));
            
            switchIsLimited.setChecked(event.isLimited());
            layoutRemaining.setVisibility(event.isLimited() ? View.VISIBLE : View.GONE);
            edtRemaining.setText(String.valueOf(event.getRemainingTickets()));

            edtImageUrl.setText(event.getImageUrl());
            selectedDate = event.getDate();
            if (selectedDate != null) tvDate.setText(sdf.format(selectedDate));
            
            selectedOpenDate = event.getTicketOpenDate();
            if (selectedOpenDate != null) tvOpen.setText(sdf.format(selectedOpenDate));
            
            selectedCloseDate = event.getTicketCloseDate();
            if (selectedCloseDate != null) tvClose.setText(sdf.format(selectedCloseDate));

            selectedEarlyBirdOpen = event.getEarlyBirdOpenDate();
            if (selectedEarlyBirdOpen != null) tvEarlyBirdOpen.setText(sdf.format(selectedEarlyBirdOpen));

            selectedEarlyBirdDeadline = event.getEarlyBirdDeadline();
            if (selectedEarlyBirdDeadline != null) tvEarlyBirdDeadline.setText(sdf.format(selectedEarlyBirdDeadline));
            edtEarlyBirdPrice.setText(String.valueOf(event.getEarlyBirdPrice()));
            edtEarlyBirdLimit.setText(String.valueOf(event.getEarlyBirdLimit()));

            // Set selection for category
            if (event.getCate() != null) {
                for (int i = 0; i < categoryList.size(); i++) {
                    if (categoryList.get(i).getId().equals(event.getCate().getId())) {
                        spinnerCat.setSelection(i);
                        break;
                    }
                }
            }

            // Set selection for organization
            if (event.getOrganizerId() != null) {
                for (int i = 0; i < organizationList.size(); i++) {
                    if (organizationList.get(i).getId().equals(event.getOrganizerId())) {
                        spinnerOrg.setSelection(i + 1); // +1 because of "Không có đơn vị"
                        break;
                    }
                }
            } else {
                spinnerOrg.setSelection(0);
            }
        } else {
            tvTitle.setText("Thêm mới sự kiện");
            selectedDate = null;
            selectedOpenDate = null;
            selectedCloseDate = null;
        }

        tvDate.setOnClickListener(v -> showDateTimePicker(tvDate, 0));
        tvOpen.setOnClickListener(v -> showDateTimePicker(tvOpen, 1));
        tvClose.setOnClickListener(v -> showDateTimePicker(tvClose, 2));
        tvEarlyBirdOpen.setOnClickListener(v -> showDateTimePicker(tvEarlyBirdOpen, 5));
        tvEarlyBirdDeadline.setOnClickListener(v -> showDateTimePicker(tvEarlyBirdDeadline, 4));

        dialogView.findViewById(R.id.btnAdminDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnAdminDialogSave).setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();
            String loc = edtLocation.getText().toString().trim();
            String latStr = edtLat.getText().toString().trim();
            String lngStr = edtLng.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String remainStr = edtRemaining.getText().toString().trim();
            String ebPriceStr = edtEarlyBirdPrice.getText().toString().trim();
            String ebLimitStr = edtEarlyBirdLimit.getText().toString().trim();
            boolean isLimited = switchIsLimited.isChecked();
            String img = edtImageUrl.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || loc.isEmpty() || priceStr.isEmpty() || (isLimited && remainStr.isEmpty()) || selectedDate == null || spinnerCat.getSelectedItem() == null) {
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
            newEvent.setLimited(isLimited);
            if (isLimited) {
                newEvent.setRemainingTickets(Integer.parseInt(remainStr));
            } else {
                newEvent.setRemainingTickets(0); // Hoặc giá trị mặc định cho vô hạn
            }
            newEvent.setImageUrl(img);
            newEvent.setDate(selectedDate);
            newEvent.setTicketOpenDate(selectedOpenDate);
            newEvent.setTicketCloseDate(selectedCloseDate);
            
            // Save Early Bird Info
            if (!ebPriceStr.isEmpty()) newEvent.setEarlyBirdPrice(Integer.parseInt(ebPriceStr));
            if (!ebLimitStr.isEmpty()) newEvent.setEarlyBirdLimit(Integer.parseInt(ebLimitStr));
            newEvent.setEarlyBirdOpenDate(selectedEarlyBirdOpen);
            newEvent.setEarlyBirdDeadline(selectedEarlyBirdDeadline);
            
            int catIndex = spinnerCat.getSelectedItemPosition();
            DocumentReference catRef = db.collection("categories").document(categoryList.get(catIndex).getId());
            newEvent.setCate(catRef);

            int orgIndex = spinnerOrg.getSelectedItemPosition();
            if (orgIndex > 0) {
                newEvent.setOrganizerId(organizationList.get(orgIndex - 1).getId());
            } else {
                newEvent.setOrganizerId(null);
            }

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

    private void showDateTimePicker(TextView tv, int type) {
        Calendar calendar = Calendar.getInstance();
        Date targetDate = null;
        switch (type) {
            case 0: targetDate = selectedDate; break;
            case 1: targetDate = selectedOpenDate; break;
            case 2: targetDate = selectedCloseDate; break;
            case 4: targetDate = selectedEarlyBirdDeadline; break;
            case 5: targetDate = selectedEarlyBirdOpen; break;
        }
        if (targetDate != null) calendar.setTime(targetDate);

        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                Date result = calendar.getTime();
                switch (type) {
                    case 0: selectedDate = result; break;
                    case 1: selectedOpenDate = result; break;
                    case 2: selectedCloseDate = result; break;
                    case 4: selectedEarlyBirdDeadline = result; break;
                    case 5: selectedEarlyBirdOpen = result; break;
                }
                tv.setText(sdf.format(result));
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
