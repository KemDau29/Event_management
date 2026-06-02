package com.example.event_management.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminUserFragment extends Fragment {

    private List<Map<String, Object>> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false); // Reuse history layout (just a title and list)

        TextView tvTitle = view.findViewById(android.R.id.text1);
        if (tvTitle != null) tvTitle.setText("Quản lý người dùng");

        ListView listView = view.findViewById(R.id.listHistory);
        adapter = new UserAdapter();
        listView.setAdapter(adapter);

        loadUsers();

        return view;
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            if (value != null) {
                userList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    userList.add(doc.getData());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    class UserAdapter extends BaseAdapter {
        @Override
        public int getCount() { return userList.size(); }
        @Override
        public Object getItem(int i) { return userList.get(i); }
        @Override
        public long getItemId(int i) { return i; }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) view = getLayoutInflater().inflate(R.layout.item_admin_user, viewGroup, false);
            TextView name = view.findViewById(R.id.tvAdminUserName);
            TextView email = view.findViewById(R.id.tvAdminUserEmail);
            Map<String, Object> data = userList.get(i);
            name.setText((String) data.get("fullname"));
            email.setText((String) data.get("email"));
            return view;
        }
    }
}
