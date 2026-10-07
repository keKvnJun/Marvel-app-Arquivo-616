package com.example.marvel_app.ui.explore;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.data.local.FavoriteStore;
import com.example.marvel_app.data.local.HistoryStore;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.ui.common.CharacterAdapter;
import com.example.marvel_app.ui.common.CharacterListViewModel;
import com.example.marvel_app.ui.common.NavigationBundles;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class ExploreFragment extends Fragment implements CharacterAdapter.Listener {
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private CharacterListViewModel viewModel;
    private CharacterAdapter adapter;
    private HistoryStore historyStore;
    private Runnable pendingSearch;
    private boolean powerMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FavoriteStore favorites = new FavoriteStore(requireContext());
        historyStore = new HistoryStore(requireContext());
        adapter = new CharacterAdapter(CharacterAdapter.Mode.ROW, favorites, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        RecyclerView list = view.findViewById(R.id.search_results_list);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && layoutManager.findLastVisibleItemPosition() >= adapter.getItemCount() - 4) {
                    viewModel.loadNext();
                }
            }
        });

        TextView count = view.findViewById(R.id.results_count_text);
        View progress = view.findViewById(R.id.search_progress);
        View empty = view.findViewById(R.id.search_empty_state);
        viewModel = new ViewModelProvider(this).get(CharacterListViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), state -> {
            adapter.submitList(state.items);
            count.setText(getString(R.string.results_count, state.items.size()));
            progress.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            empty.setVisibility(!state.loading && state.items.isEmpty() ? View.VISIBLE : View.GONE);
            if (!state.message.isEmpty()) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show();
            }
        });

        TextInputEditText input = view.findViewById(R.id.search_input);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                String query = s == null ? "" : s.toString();
                pendingSearch = () -> executeSearch(query);
                searchHandler.postDelayed(pendingSearch, 350L);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        String initialQuery = getArguments() == null ? "" : getArguments().getString("initialQuery", "");
        if (initialQuery.isEmpty()) {
            CharacterListViewModel.State currentState = viewModel.state().getValue();
            if (currentState == null || (!currentState.loading && currentState.items.isEmpty())) {
                viewModel.loadFeatured();
            }
        } else {
            input.setText(initialQuery);
            input.setSelection(initialQuery.length());
        }

        TextInputLayout inputLayout = view.findViewById(R.id.search_input_layout);
        ChipGroup filters = view.findViewById(R.id.filter_chip_group);
        filters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            powerMode = checkedIds.contains(R.id.filter_powers_chip);
            inputLayout.setHint(powerMode ? "Poderes separados por vírgula" : getString(R.string.search_hint));
            executeSearch(input.getText() == null ? "" : input.getText().toString());
        });

    }

    private void executeSearch(String text) {
        if (powerMode) {
            viewModel.searchByPowers(Arrays.stream(text.split(","))
                    .map(String::trim).filter(value -> !value.isEmpty())
                    .map(this::translatePower).collect(Collectors.toList()));
        } else {
            viewModel.search(text);
        }
    }

    private String translatePower(String power) {
        String normalized = java.text.Normalizer.normalize(power, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(java.util.Locale.ROOT);
        if (normalized.equals("voar") || normalized.equals("voo")) return "Flight";
        if (normalized.equals("velocidade") || normalized.equals("super velocidade")) return "Super Speed";
        if (normalized.equals("forca") || normalized.equals("super forca")) return "Super Strength";
        if (normalized.equals("telepatia")) return "Telepathy";
        if (normalized.equals("invisibilidade")) return "Invisibility";
        if (normalized.equals("cura") || normalized.equals("regeneracao")) return "Healing Factor";
        return power;
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
        adapter = null;
        historyStore = null;
        super.onDestroyView();
    }

    @Override
    public void onCharacterSelected(CharacterCardData character) {
        historyStore.record(character);
        Navigation.findNavController(requireView()).navigate(
                R.id.action_explore_to_detail, NavigationBundles.forCharacter(character));
    }

    @Override
    public void onFavoriteChanged() {
    }
}
