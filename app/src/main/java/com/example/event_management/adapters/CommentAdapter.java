package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.event_management.R;
import com.example.event_management.models.Comment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends BaseAdapter {
    private Context context;
    private List<Comment> commentList = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    public CommentAdapter(Context context) {
        this.context = context;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return commentList.size(); }

    @Override
    public Object getItem(int position) { return commentList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
            holder = new ViewHolder();
            holder.tvUserName = convertView.findViewById(R.id.tvCommentUserName);
            holder.tvTime = convertView.findViewById(R.id.tvCommentTime);
            holder.tvContent = convertView.findViewById(R.id.tvCommentContent);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Comment comment = commentList.get(position);
        if (comment != null) {
            holder.tvUserName.setText(comment.getUserName());
            holder.tvContent.setText(comment.getContent());
            holder.tvTime.setText(comment.getTimestamp() != null ? sdf.format(comment.getTimestamp()) : "");
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvUserName, tvTime, tvContent;
    }
}
