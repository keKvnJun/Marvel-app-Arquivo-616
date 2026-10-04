package com.example.marvel_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.data.local.FavoriteStore;
import com.example.marvel_app.data.local.HistoryStore;
import com.example.marvel_app.data.local.ThemeStore;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.ui.common.CharacterAdapter;
import com.example.marvel_app.ui.common.CharacterListViewModel;
import com.example.marvel_app.ui.common.NavigationBundles;

import java.util.Collections;

public final class HomeFragment extends Fragment implements CharacterAdapter.Listener {
    private CharacterAdapter featuredAdapter;
    private CharacterAdapter recentAdapter;
    private FavoriteStore favoriteStore;
    private HistoryStore historyStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        favoriteStore = new FavoriteStore(requireContext());
        historyStore = new HistoryStore(requireContext());
        featuredAdapter = new CharacterAdapter(CharacterAdapter.Mode.CARD, favoriteStore, this);
        recentAdapter = new CharacterAdapter(CharacterAdapter.Mode.ROW, favoriteStore, this);

        RecyclerView featured = view.findViewById(R.id.featured_characters_list);
        featured.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        featured.setAdapter(featuredAdapter);
        RecyclerView recent = view.findViewById(R.id.recent_characters_list);
        recent.setLayoutManager(new LinearLayoutManager(requireContext()));
        recent.setAdapter(recentAdapter);
        recentAdapter.submitList(historyStore.getHistory());

        view.findViewById(R.id.spider_verse_feature).setOnClickListener(
                item -> Navigation.findNavController(item).navigate(R.id.action_home_to_spider_verse));
        view.findViewById(R.id.view_all_featured_button).setOnClickListener(
                item -> Navigation.findNavController(item).navigate(R.id.nav_explore));
        view.findViewById(R.id.theme_button).setOnClickListener(item -> toggleTheme());

        CharacterListViewModel viewModel = new ViewModelProvider(this).get(CharacterListViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), state -> {
            featuredAdapter.submitList(state.items);
            if (!state.message.isEmpty()) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show();
            }
        });
        if (viewModel.state().getValue() == null
                || viewModel.state().getValue().items.equals(Collections.emptyList())) {
            viewModel.loadFeatured();
        }
    }

    private void toggleTheme() {
        ThemeStore store = new ThemeStore(requireContext());
        boolean isDark = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        ThemeStore.ThemeMode next = isDark ? ThemeStore.ThemeMode.LIGHT : ThemeStore.ThemeMode.DARK;
        store.setThemeMode(next);
        AppCompatDelegate.setDefaultNightMode(isDark
                ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    public void onCharacterSelected(CharacterCardData character) {
        historyStore.record(character);
        Navigation.findNavController(requireView()).navigate(
                R.id.action_home_to_detail, NavigationBundles.forCharacter(character));
    }

    @Override
    public void onFavoriteChanged() {
    }

    @Override
    public void onDestroyView() {
        featuredAdapter = null;
        recentAdapter = null;
        favoriteStore = null;
        historyStore = null;
        super.onDestroyView();
    }
}
