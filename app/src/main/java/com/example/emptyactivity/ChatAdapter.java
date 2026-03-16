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

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messageList;
    private String currentUserId;
    private String receiverName;

    public ChatAdapter(List<ChatMessage> messageList, String receiverName) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.receiverName = receiverName;
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
            ((ReceivedMessageViewHolder) holder).bind(message, receiverName);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            messageTime.setText(sdf.format(new Date(message.getTimestamp())));
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, senderName;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            senderName = itemView.findViewById(R.id.senderName);
        }

        void bind(ChatMessage message, String name) {
            messageText.setText(message.getMessage());
            senderName.setText(name);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            messageTime.setText(sdf.format(new Date(message.getTimestamp())));
        }
    }
}
