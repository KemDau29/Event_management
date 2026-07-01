package com.example.event_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.event_management.adapters.EventAdapter;
import com.example.event_management.models.Event;
import com.example.event_management.models.Organization;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrganizationDetailFragment extends Fragment {

    private Organization organization;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EventAdapter eventAdapter;
    private List<Event> orgEvents = new ArrayList<>();
    private Button btnFollow;
    private TextView tvFollowerCount;
    private TextView tvNoEvents;
    private boolean isFollowing = false;

    public static OrganizationDetailFragment newInstance(Organization org) {
        OrganizationDetailFragment fragment = new OrganizationDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("org", org);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            organization = (Organization) getArguments().getSerializable("org");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organization_detail, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        TextView tvName = view.findViewById(R.id.tvOrgDetailName);
        TextView tvDesc = view.findViewById(R.id.tvOrgDetailDesc);
        tvFollowerCount = view.findViewById(R.id.tvOrgFollowerCount);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        ImageView imgLogo = view.findViewById(R.id.imgOrgDetailLogo);
        btnFollow = view.findViewById(R.id.btnFollowOrg);
        ListView listView = view.findViewById(R.id.listOrgEvents);

        view.findViewById(R.id.btnBackOrg).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        if (organization != null) {
            tvName.setText(organization.getName());
            tvDesc.setText(organization.getDescription());
            updateFollowerCountUI();
            
            if (organization.getLogoUrl() != null && !organization.getLogoUrl().isEmpty()) {
                Glide.with(this).load(organization.getLogoUrl()).placeholder(android.R.drawable.ic_menu_gallery).into(imgLogo);
            }

            checkFollowStatus();

            btnFollow.setOnClickListener(v -> toggleFollow());

            eventAdapter = new EventAdapter(requireContext());
            listView.setAdapter(eventAdapter);
            
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                Event event = orgEvents.get(position);
                event_detail detailFragment = event_detail.newInstance(event);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            });

            loadOrgEvents();
        }

        return view;
    }

    private void updateFollowerCountUI() {
        int count = organization.getFollowers() != null ? organization.getFollowers().size() : 0;
        tvFollowerCount.setText(count + " người theo dõi");
    }

    private void checkFollowStatus() {
        if (mAuth.getCurrentUser() == null || organization.getFollowers() == null) return;
        isFollowing = organization.getFollowers().contains(mAuth.getUid());
        updateFollowButton();
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("Đang theo dõi");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
        } else {
            btnFollow.setText("Theo dõi");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#185FA5")));
        }
    }

    private void toggleFollow() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để theo dõi", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getUid();
        if (isFollowing) {
            db.collection("organizations").document(organization.getId())
                    .update("followers", FieldValue.arrayRemove(uid))
                    .addOnSuccessListener(aVoid -> {
                        isFollowing = false;
                        organization.getFollowers().remove(uid);
                        updateFollowButton();
                        updateFollowerCountUI();
                        Toast.makeText(getContext(), "Đã bỏ theo dõi", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("organizations").document(organization.getId())
                    .update("followers", FieldValue.arrayUnion(uid))
                    .addOnSuccessListener(aVoid -> {
                        isFollowing = true;
                        if (organization.getFollowers() == null) organization.setFollowers(new ArrayList<>());
                        organization.getFollowers().add(uid);
                        updateFollowButton();
                        updateFollowerCountUI();
                        Toast.makeText(getContext(), "Đã theo dõi " + organization.getName(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadOrgEvents() {
        if (organization == null || organization.getId() == null) {
            android.util.Log.e("OrgDetail", "Organization or ID is null");
            tvNoEvents.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("events")
                .whereEqualTo("organizerId", organization.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orgEvents.clear();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            event.setId(doc.getId());
                            orgEvents.add(event);
                        }
                    }
                    
                    if (orgEvents.isEmpty()) {
                        tvNoEvents.setVisibility(View.VISIBLE);
                    } else {
                        tvNoEvents.setVisibility(View.GONE);
                    }
                    
                    eventAdapter.setEventList(orgEvents);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("OrgDetail", "Error loading events: " + e.getMessage());
                    tvNoEvents.setVisibility(View.VISIBLE);
                    tvNoEvents.setText("Lỗi khi tải sự kiện");
                });
    }
}
