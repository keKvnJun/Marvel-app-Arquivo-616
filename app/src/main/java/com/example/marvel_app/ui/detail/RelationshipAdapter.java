package com.example.marvel_app.ui.detail;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.data.model.ResourceReference;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

final class RelationshipAdapter extends RecyclerView.Adapter<RelationshipAdapter.Holder> {
    interface Listener {
        void onRelationshipSelected(ResourceReference reference);
    }

    private final List<ResourceReference> items = new ArrayList<>();
    private final int labelRes;
    private final int accentRes;
    private final boolean opensDossier;
    private final Listener listener;

    RelationshipAdapter(
            @StringRes int labelRes,
            @ColorRes int accentRes,
            boolean opensDossier,
            Listener listener
    ) {
        this.labelRes = labelRes;
        this.accentRes = accentRes;
        this.opensDossier = opensDossier;
        this.listener = listener;
    }

    void submit(List<ResourceReference> references) {
        items.clear();
        if (references != null) items.addAll(references);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_relationship_node, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ResourceReference reference = items.get(position);
        int accent = ContextCompat.getColor(holder.itemView.getContext(), accentRes);
        holder.card.setStrokeColor(accent);
        holder.badge.setText(labelRes);
        holder.badge.setTextColor(accent);
        holder.signal.setBackgroundTintList(ColorStateList.valueOf(accent));
        holder.name.setText(reference.getName());
        boolean hasDossier = opensDossier
                && (!reference.getApiDetailUrl().isEmpty() || reference.getId() > 0);
        holder.itemView.setClickable(hasDossier);
        holder.itemView.setFocusable(hasDossier);
        holder.itemView.setAlpha(hasDossier ? 1f : 0.82f);
        int descriptionRes = hasDossier
                ? R.string.relationship_open_description
                : opensDossier
                        ? R.string.relationship_unavailable_description
                        : R.string.relationship_team_description;
        holder.itemView.setContentDescription(holder.itemView.getContext().getString(
                descriptionRes, reference.getName()));
        holder.itemView.setOnClickListener(hasDossier
                ? view -> listener.onRelationshipSelected(reference)
                : null);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final View signal;
        final TextView badge;
        final TextView name;

        Holder(View view) {
            super(view);
            card = (MaterialCardView) view;
            signal = view.findViewById(R.id.relationship_signal);
            badge = view.findViewById(R.id.relationship_badge);
            name = view.findViewById(R.id.relationship_name);
        }
    }
}
