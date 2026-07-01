package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.event_management.R;
import com.example.event_management.models.Organization;
import java.util.ArrayList;
import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.OrgViewHolder> {

    private final Context context;
    private List<Organization> orgList = new ArrayList<>();
    private final OnOrgClickListener listener;

    public interface OnOrgClickListener {
        void onOrgClick(Organization org);
    }

    public OrganizationAdapter(Context context, OnOrgClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrgList(List<Organization> list) {
        this.orgList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_organization, parent, false);
        return new OrgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrgViewHolder holder, int position) {
        Organization org = orgList.get(position);
        if (org != null) {
            holder.tvName.setText(org.getName());
            if (org.getLogoUrl() != null && !org.getLogoUrl().isEmpty()) {
                Glide.with(context).load(org.getLogoUrl()).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imgLogo);
            } else {
                holder.imgLogo.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.itemView.setOnClickListener(v -> listener.onOrgClick(org));
        }
    }

    @Override
    public int getItemCount() {
        return orgList.size();
    }

    static class OrgViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView tvName;
        public OrgViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.imgOrgLogo);
            tvName = itemView.findViewById(R.id.tvOrgName);
        }
    }
}
