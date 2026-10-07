package com.example.marvel_app.ui.archive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.domain.cards.CollectibleCard;
import com.example.marvel_app.domain.duel.DuelStats;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class CollectionCardAdapter extends RecyclerView.Adapter<CollectionCardAdapter.Holder> {
    interface Listener {
        void onDeckToggle(CollectibleCard card);
    }

    private final Listener listener;
    private List<CollectibleCard> items = Collections.emptyList();
    private Set<String> deckIds = Collections.emptySet();

    CollectionCardAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<CollectibleCard> cards, List<String> selectedDeckIds) {
        items = new ArrayList<>(cards);
        deckIds = new HashSet<>(selectedDeckIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CollectibleCard item = items.get(position);
        DuelStats stats = item.getDuelCard().getStats();
        holder.name.setText(item.getCharacter().getName());
        holder.identity.setText(item.getCharacter().getRealName());
        holder.rarity.setText(item.getDuelCard().getRarity().toUpperCase(Locale.ROOT));
        holder.stats.setText(holder.itemView.getContext().getString(
                R.string.collection_card_stats,
                stats.getVersatility(), stats.getEditorialHistory(),
                stats.getPresence(), stats.getAlliances()));
        boolean inDeck = deckIds.contains(item.getId());
        holder.deckButton.setText(inDeck ? R.string.deck_remove : R.string.deck_add);
        holder.deckButton.setIconResource(inDeck ? R.drawable.ic_shield : R.drawable.ic_cards);
        holder.deckButton.setOnClickListener(view -> listener.onDeckToggle(item));
        holder.image.setImageResource(Math.abs(item.getId().hashCode()) % 2 == 0
                ? R.drawable.comic_collage_halftone : R.drawable.comic_collage_muted);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView identity;
        final TextView rarity;
        final TextView stats;
        final MaterialButton deckButton;

        Holder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.collection_card_image);
            name = itemView.findViewById(R.id.collection_card_name);
            identity = itemView.findViewById(R.id.collection_card_identity);
            rarity = itemView.findViewById(R.id.collection_card_rarity);
            stats = itemView.findViewById(R.id.collection_card_stats);
            deckButton = itemView.findViewById(R.id.collection_deck_button);
        }
    }
}
