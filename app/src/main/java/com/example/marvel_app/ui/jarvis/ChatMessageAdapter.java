package com.example.marvel_app.ui.jarvis;

import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.example.marvel_app.R;

import java.util.List;

public final class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.Holder> {
    private final List<ChatMessage> messages;

    public ChatMessageAdapter(List<ChatMessage> messages) { this.messages = messages; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.sender.setText(message.sender);
        holder.text.setText(message.text);
        LinearLayout root = (LinearLayout) holder.itemView;
        root.setGravity(message.user ? Gravity.END : Gravity.START);
        holder.text.setBackgroundResource(message.user ? R.drawable.bg_chat_user : R.drawable.bg_chat_jarvis);
        holder.text.setTextColor(ContextCompat.getColor(holder.text.getContext(), R.color.white));
        holder.sender.setTextColor(ContextCompat.getColor(holder.sender.getContext(),
                message.user ? R.color.marvel_red : R.color.jarvis_blue));
    }

    @Override public int getItemCount() { return messages.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView sender;
        final TextView text;
        Holder(View view) {
            super(view);
            sender = view.findViewById(R.id.message_sender);
            text = view.findViewById(R.id.message_text);
        }
    }
}
