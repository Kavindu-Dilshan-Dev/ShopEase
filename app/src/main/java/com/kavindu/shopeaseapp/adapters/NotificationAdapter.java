package com.kavindu.shopeaseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.models.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends
        RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private final Context context;
    private final List<NotificationModel> list;

    public NotificationAdapter(Context context,
                               List<NotificationModel> list) {
        this.context = context;
        this.list    = list;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder,
                                 int position) {
        NotificationModel notif = list.get(position);

        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());
        holder.tvTime.setText(formatTime(notif.getCreatedAt()));


        switch (notif.getType() != null ? notif.getType() : "general") {
            case "order":
                holder.ivIcon.setImageResource(
                        android.R.drawable.ic_menu_agenda);
                break;
            case "promo":
                holder.ivIcon.setImageResource(
                        android.R.drawable.ic_menu_today);
                break;
            default:
                holder.ivIcon.setImageResource(
                        android.R.drawable.ic_dialog_info);
                break;
        }


        holder.itemView.setAlpha(notif.isRead() ? 0.6f : 1.0f);


        holder.itemView.setOnClickListener(v -> {
            if (!notif.isRead()) {
                notif.setRead(true);
                holder.itemView.setAlpha(0.6f);
                FirebaseFirestore.getInstance()
                        .collection("notifications")
                        .document(notif.getId())
                        .update("isRead", true);
            }
        });

        // Swipe to delete
        holder.itemView.setOnLongClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .document(notif.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        int pos = list.indexOf(notif);
                        if (pos != -1) {
                            list.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    });
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    private String formatTime(long timestamp) {
        long diff  = System.currentTimeMillis() - timestamp;
        long mins  = diff / 60000;
        if (mins < 1)  return "Just now";
        if (mins < 60) return mins + "m ago";
        long hours = mins / 60;
        if (hours < 24) return hours + "h ago";
        return new SimpleDateFormat("dd MMM yyyy",
                Locale.getDefault()).format(new Date(timestamp));
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime;

        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon    = itemView.findViewById(R.id.ivIcon);
            tvTitle   = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime    = itemView.findViewById(R.id.tvTime);
        }
    }
}