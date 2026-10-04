package com.example.marvel_app.ui.spiderverse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.domain.spiderverse.SpiderVerseSeed;

import java.util.List;

public final class SpiderVariantAdapter extends RecyclerView.Adapter<SpiderVariantAdapter.Holder> {
    public interface Listener { void onSelected(SpiderVerseSeed seed); }

    private final List<SpiderVerseSeed> items;
    private final Listener listener;

    public SpiderVariantAdapter(List<SpiderVerseSeed> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spider_variant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SpiderVerseSeed item = items.get(position);
        holder.name.setText(item.getDisplayName());
        holder.identity.setText(item.getRealName());
        holder.universe.setText(item.getUniverse().toUpperCase());
        holder.image.setImageResource(position % 2 == 0
                ? R.drawable.comic_collage_halftone : R.drawable.comic_collage_muted);
        holder.itemView.setOnClickListener(view -> listener.onSelected(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView identity;
        final TextView universe;

        Holder(View view) {
            super(view);
            image = view.findViewById(R.id.variant_image);
            name = view.findViewById(R.id.variant_name);
            identity = view.findViewById(R.id.variant_identity);
            universe = view.findViewById(R.id.variant_universe);
        }
    }
}
