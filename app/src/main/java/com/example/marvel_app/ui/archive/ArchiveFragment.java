package com.example.marvel_app.ui.archive;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.data.local.FavoriteStore;
import com.example.marvel_app.data.local.HistoryStore;
import com.example.marvel_app.data.local.SharedPreferencesCardStorage;
import com.example.marvel_app.data.local.ThemeStore;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.domain.cards.CardCollectionState;
import com.example.marvel_app.domain.cards.CardCollectionStore;
import com.example.marvel_app.domain.cards.CollectibleCard;
import com.example.marvel_app.ui.common.CharacterAdapter;
import com.example.marvel_app.ui.common.NavigationBundles;
import com.example.marvel_app.domain.duel.StanLeeCardFactory;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public final class ArchiveFragment extends Fragment implements CharacterAdapter.Listener {
    private FavoriteStore favorites;
    private HistoryStore history;
    private CharacterAdapter characterAdapter;
    private CollectionCardAdapter collectionAdapter;
    private CardCollectionStore cardCollection;
    private RecyclerView list;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;
    private TextView collectionSummary;
    private int selectedTab;
    private int shieldTaps;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_archive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        favorites = new FavoriteStore(requireContext());
        history = new HistoryStore(requireContext());
        cardCollection = new CardCollectionStore(new SharedPreferencesCardStorage(requireContext()));
        characterAdapter = new CharacterAdapter(CharacterAdapter.Mode.ROW, favorites, this);
        collectionAdapter = new CollectionCardAdapter(this::toggleDeck);
        list = view.findViewById(R.id.archive_characters_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(characterAdapter);
        emptyState = view.findViewById(R.id.archive_empty_state);
        emptyTitle = view.findViewById(R.id.archive_empty_title);
        emptyBody = view.findViewById(R.id.archive_empty_body);
        collectionSummary = view.findViewById(R.id.archive_collection_summary);

        TabLayout tabs = view.findViewById(R.id.archive_tabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { selectedTab = tab.getPosition(); refresh(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        MaterialSwitch themeSwitch = view.findViewById(R.id.theme_switch);
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        themeSwitch.setChecked(dark);
        themeSwitch.setOnCheckedChangeListener((button, checked) -> {
            new ThemeStore(requireContext()).setThemeMode(
                    checked ? ThemeStore.ThemeMode.DARK : ThemeStore.ThemeMode.LIGHT);
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        View legend = view.findViewById(R.id.legend_card_container);
        boolean legendUnlocked = requireContext().getSharedPreferences("archive_secrets", 0)
                .getBoolean("stan_lee_unlocked", false);
        legend.setVisibility(legendUnlocked ? View.VISIBLE : View.GONE);
        StanLeeCardFactory.create();
        view.findViewById(R.id.archive_shield_logo).setOnClickListener(item -> {
            shieldTaps++;
            if (shieldTaps >= 3) {
                legend.setVisibility(View.VISIBLE);
                requireContext().getSharedPreferences("archive_secrets", 0).edit()
                        .putBoolean("stan_lee_unlocked", true).apply();
                legend.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                        requireContext(), R.anim.favorite_pop));
            }
        });
        refresh();
    }

    private void refresh() {
        if (selectedTab == 2) {
            CardCollectionState state = cardCollection.load();
            List<CollectibleCard> cards = cardCollection.ownedCards();
            list.setAdapter(collectionAdapter);
            collectionAdapter.submit(cards, state.getDeckIds());
            collectionSummary.setVisibility(View.VISIBLE);
            collectionSummary.setText(getString(
                    R.string.collection_summary,
                    state.getOwnedIds().size(), state.getDeckIds().size(),
                    CardCollectionStore.MAX_DECK_SIZE));
            emptyTitle.setText(R.string.collection_empty_title);
            emptyBody.setText(R.string.collection_empty_body);
            emptyState.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        List<CharacterCardData> items = selectedTab == 0
                ? favorites.getFavorites() : history.getHistory();
        list.setAdapter(characterAdapter);
        characterAdapter.submitList(items);
        collectionSummary.setVisibility(View.GONE);
        emptyTitle.setText(selectedTab == 0
                ? R.string.archive_empty_title : R.string.history_empty_title);
        emptyBody.setText(selectedTab == 0
                ? R.string.archive_empty_body : R.string.history_empty_body);
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (list != null) {
            refresh();
        }
    }

    private void toggleDeck(CollectibleCard card) {
        CardCollectionStore.DeckChange result = cardCollection.toggleDeck(card.getId());
        int message;
        switch (result) {
            case ADDED:
                message = R.string.deck_added;
                break;
            case REMOVED:
                message = R.string.deck_removed;
                break;
            case DECK_FULL:
                message = R.string.deck_full;
                break;
            default:
                message = R.string.deck_change_error;
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        refresh();
    }

    @Override
    public void onCharacterSelected(CharacterCardData character) {
        Navigation.findNavController(requireView()).navigate(
                R.id.action_archive_to_detail, NavigationBundles.forCharacter(character));
    }

    @Override
    public void onFavoriteChanged() {
        refresh();
    }

    @Override
    public void onDestroyView() {
        favorites = null;
        history = null;
        characterAdapter = null;
        collectionAdapter = null;
        cardCollection = null;
        list = null;
        emptyState = null;
        emptyTitle = null;
        emptyBody = null;
        collectionSummary = null;
        super.onDestroyView();
    }
}
