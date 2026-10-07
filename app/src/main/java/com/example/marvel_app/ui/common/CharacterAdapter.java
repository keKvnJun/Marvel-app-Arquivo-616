package com.example.marvel_app.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.marvel_app.R;
import com.example.marvel_app.data.local.FavoriteStore;
import com.example.marvel_app.data.model.CharacterCardData;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.Set;

public final class CharacterAdapter extends ListAdapter<CharacterCardData, CharacterAdapter.CharacterHolder> {
    public enum Mode { CARD, ROW }

    public interface Listener {
        void onCharacterSelected(CharacterCardData character);
        void onFavoriteChanged();
    }

    private static final DiffUtil.ItemCallback<CharacterCardData> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull CharacterCardData oldItem, @NonNull CharacterCardData newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CharacterCardData oldItem, @NonNull CharacterCardData newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getRealName().equals(newItem.getRealName())
                    && oldItem.getImageUrl().equals(newItem.getImageUrl())
                    && oldItem.getIssueAppearances() == newItem.getIssueAppearances();
        }
    };

    private final Mode mode;
    private final FavoriteStore favorites;
    private final Listener listener;
    private final Set<Long> favoriteIds = new HashSet<>();

    public CharacterAdapter(Mode mode, FavoriteStore favorites, Listener listener) {
        super(DIFF);
        this.mode = mode;
        this.favorites = favorites;
        this.listener = listener;
        for (CharacterCardData favorite : favorites.getFavorites()) {
            favoriteIds.add(favorite.getId());
        }
    }

    @NonNull
    @Override
    public CharacterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = mode == Mode.CARD ? R.layout.item_character_card : R.layout.item_character_row;
        return new CharacterHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), mode);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterHolder holder, int position) {
        CharacterCardData item = getItem(position);
        holder.name.setText(item.getName());
        holder.identity.setText(item.getRealName().isEmpty() ? item.getPublisherName() : item.getRealName());
        holder.itemView.setOnClickListener(view -> listener.onCharacterSelected(item));
        Glide.with(holder.image)
                .load(item.getImageUrl().isEmpty() ? R.drawable.comic_collage_muted : item.getImageUrl())
                .centerCrop()
                .thumbnail(0.15f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .dontAnimate()
                .placeholder(R.drawable.comic_collage_muted)
                .error(R.drawable.comic_collage_muted)
                .into(holder.image);
        holder.image.setContentDescription(item.getName());
        if (holder.appearances != null) {
            holder.appearances.setText(String.valueOf(item.getIssueAppearances()));
        }
        if (holder.powers != null) {
            holder.powers.setText("•••");
        }
        updateFavorite(holder.favorite, item);
        holder.favorite.setOnClickListener(view -> {
            boolean selected = favorites.toggle(item);
            if (selected) favoriteIds.add(item.getId()); else favoriteIds.remove(item.getId());
            view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(view.getContext(), R.anim.favorite_pop));
            updateFavorite(holder.favorite, item);
            listener.onFavoriteChanged();
        });
    }

    private void updateFavorite(MaterialButton button, CharacterCardData item) {
        boolean selected = favoriteIds.contains(item.getId());
        button.setIconResource(selected ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
        button.setContentDescription(button.getContext().getString(
                selected ? R.string.favorite_remove : R.string.favorite_add
        ));
    }

    static final class CharacterHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView identity;
        final TextView powers;
        final TextView appearances;
        final MaterialButton favorite;

        CharacterHolder(View itemView, Mode mode) {
            super(itemView);
            if (mode == Mode.CARD) {
                image = itemView.findViewById(R.id.character_image);
                name = itemView.findViewById(R.id.character_name);
                identity = itemView.findViewById(R.id.character_identity);
                powers = itemView.findViewById(R.id.power_count);
                appearances = itemView.findViewById(R.id.appearance_count);
                favorite = itemView.findViewById(R.id.favorite_button);
            } else {
                image = itemView.findViewById(R.id.row_character_image);
                name = itemView.findViewById(R.id.row_character_name);
                identity = itemView.findViewById(R.id.row_character_identity);
                powers = null;
                appearances = null;
                favorite = itemView.findViewById(R.id.row_favorite_button);
            }
        }
    }
}
