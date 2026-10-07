package com.example.marvel_app.ui.spiderverse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.marvel_app.R;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.domain.spiderverse.SpiderVerseSeed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpiderVariantAdapter extends RecyclerView.Adapter<SpiderVariantAdapter.Holder> {
    public interface Listener {
        void onSelected(SpiderVerseSeed seed, CharacterCardData character);
    }

    private final List<SpiderVerseSeed> items;
    private final Listener listener;
    private final Map<Long, CharacterCardData> charactersById = new HashMap<>();

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
        CharacterCardData character = charactersById.get(item.getComicVineId());
        String imageUrl = character == null ? "" : character.getImageUrl();
        Glide.with(holder.image)
                .load(imageUrl.isEmpty() ? R.drawable.comic_collage_halftone : imageUrl)
                .centerCrop()
                .thumbnail(0.15f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .dontAnimate()
                .placeholder(R.drawable.comic_collage_halftone)
                .error(R.drawable.comic_collage_muted)
                .into(holder.image);
        holder.image.setContentDescription(item.getDisplayName());
        CharacterCardData navigationCard = new CharacterCardData(
                item.getComicVineId(),
                item.getApiObjectId(),
                item.getDisplayName(),
                item.getRealName(),
                character == null ? "" : character.getImageUrl(),
                "Marvel",
                character == null ? 0 : character.getIssueAppearances()
        );
        holder.itemView.setOnClickListener(view -> listener.onSelected(item, navigationCard));
    }

    public void submitCharacters(List<CharacterDto> characters) {
        charactersById.clear();
        if (characters != null) {
            for (CharacterDto character : characters) {
                if (character != null) {
                    charactersById.put(character.getId(), CharacterCardData.from(character));
                }
            }
        }
        notifyDataSetChanged();
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
