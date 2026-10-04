package com.example.marvel_app.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.data.model.ResourceReference;

import java.util.ArrayList;
import java.util.List;

final class ReferenceAdapter extends RecyclerView.Adapter<ReferenceAdapter.Holder> {
    private final List<ResourceReference> items = new ArrayList<>();

    void submit(List<ResourceReference> references) {
        items.clear();
        if (references != null) items.addAll(references);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView text = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        text.setTextAppearance(com.example.marvel_app.R.style.TextAppearance_Marvel_Body);
        text.setCompoundDrawablePadding(12);
        return new Holder(text);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.text.setText("•  " + items.get(position).getName());
    }

    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView text;
        Holder(TextView text) { super(text); this.text = text; }
    }
}
