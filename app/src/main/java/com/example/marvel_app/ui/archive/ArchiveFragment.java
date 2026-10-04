package com.example.marvel_app.ui.archive;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.marvel_app.data.local.ThemeStore;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.ui.common.CharacterAdapter;
import com.example.marvel_app.ui.common.NavigationBundles;
import com.example.marvel_app.domain.duel.StanLeeCardFactory;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public final class ArchiveFragment extends Fragment implements CharacterAdapter.Listener {
    private FavoriteStore favorites;
    private HistoryStore history;
    private CharacterAdapter adapter;
    private View emptyState;
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
        adapter = new CharacterAdapter(CharacterAdapter.Mode.ROW, favorites, this);
        RecyclerView list = view.findViewById(R.id.archive_characters_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        emptyState = view.findViewById(R.id.archive_empty_state);

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
        List<CharacterCardData> items = selectedTab == 0 ? favorites.getFavorites() : history.getHistory();
        adapter.submitList(items);
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
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
        adapter = null;
        emptyState = null;
        super.onDestroyView();
    }
}
