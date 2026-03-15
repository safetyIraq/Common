package com.example.emptyactivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * محول عرض رسائل المحادثة
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;  // رسالة مرسلة (على اليمين)
    private static final int VIEW_TYPE_RECEIVED = 2; // رسالة مستقبلة (على اليسار)

    private List<ChatMessage> messageList;
    private String currentUserId;
    private String receiverName;
    private String receiverImage;

    public ChatAdapter(List<ChatMessage> messageList, String receiverName, String receiverImage) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.receiverName = receiverName;
        this.receiverImage = receiverImage;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, receiverName, receiverImage);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder للرسائل المرسلة
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, messageStatus;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageStatus = itemView.findViewById(R.id.messageStatus);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            
            // تنسيق الوقت
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            messageTime.setText(sdf.format(new Date(message.getTimestamp())));

            // حالة الرسالة (مقروءة أم لا)
            if (message.isRead()) {
                messageStatus.setText("✓✓");
                messageStatus.setTextColor(0xFF4CAF50); // أخضر
            } else {
                messageStatus.setText("✓");
                messageStatus.setTextColor(0xFF999999); // رمادي
            }
        }
    }

    // ViewHolder للرسائل المستقبلة
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, senderName;
        CircleImageView senderImage;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            senderName = itemView.findViewById(R.id.senderName);
            senderImage = itemView.findViewById(R.id.senderImage);
        }

        void bind(ChatMessage message, String name, String imageUrl) {
            messageText.setText(message.getMessage());
            senderName.setText(name);

            // تنسيق الوقت
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            messageTime.setText(sdf.format(new Date(message.getTimestamp())));

            // تحميل الصورة (اختياري)
            // إذا عندك Glide تقدر تحمل الصورة
            // Glide.with(itemView.getContext()).load(imageUrl).into(senderImage);
        }
    }
}
