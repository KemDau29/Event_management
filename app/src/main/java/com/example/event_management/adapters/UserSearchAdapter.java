package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.event_management.R;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.event_management.R;
import java.util.List;
import java.util.Map;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    private Context context;
    private List<Map<String, Object>> userList;
    private OnUserSearchActionListener listener;

    public interface OnUserSearchActionListener {
        void onAddFriend(String userId, String userName);
    }

    public UserSearchAdapter(Context context, List<Map<String, Object>> userList, OnUserSearchActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> userData = userList.get(position);
        String uid = (String) userData.get("uid");
        String fullName = (String) userData.get("fullname");
        String userName = (String) userData.get("username");

        holder.tvFullName.setText(fullName);
        holder.tvUserName.setText("@" + userName);

        holder.btnAddFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddFriend(uid, fullName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvUserName;
        Button btnAddFriend;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}
