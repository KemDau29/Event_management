package com.example.event_management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.event_management.R;
import com.example.event_management.models.TicketType;
import java.util.List;

public class TicketTypeAdapter extends RecyclerView.Adapter<TicketTypeAdapter.ViewHolder> {
    private List<TicketType> ticketTypes;

    public TicketTypeAdapter(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketType ticketType = ticketTypes.get(position);
        holder.tvName.setText(ticketType.getName());
        holder.tvPrice.setText(String.format("%,dđ", ticketType.getPrice()));
        holder.tvDescription.setText(ticketType.getDescription());
    }

    @Override
    public int getItemCount() {
        return ticketTypes != null ? ticketTypes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTicketName);
            tvPrice = itemView.findViewById(R.id.tvTicketPrice);
            tvDescription = itemView.findViewById(R.id.tvTicketDescription);
        }
    }
}
