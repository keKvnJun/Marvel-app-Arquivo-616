package com.example.marvel_app.ui.spiderverse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.domain.spiderverse.SpiderVerseCatalog;
import com.example.marvel_app.domain.spiderverse.SpiderVerseSeed;

public final class SpiderVerseFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spider_verse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View portal = view.findViewById(R.id.spider_portal);
        portal.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                requireContext(), R.anim.portal_enter));
        RecyclerView variants = view.findViewById(R.id.spider_variants_list);
        variants.setLayoutManager(new LinearLayoutManager(requireContext()));
        variants.setAdapter(new SpiderVariantAdapter(SpiderVerseCatalog.seeds(), this::openSearch));
        view.findViewById(R.id.spider_back_button).setOnClickListener(
                button -> Navigation.findNavController(button).navigateUp());
        view.findViewById(R.id.discover_variants_button).setOnClickListener(
                button -> openQuery("Spider"));
    }

    private void openSearch(SpiderVerseSeed seed) {
        openQuery(seed.getSearchTerms().get(0));
    }

    private void openQuery(String query) {
        Bundle args = new Bundle();
        args.putString("initialQuery", query);
        Navigation.findNavController(requireView()).navigate(
                R.id.action_spider_verse_to_explore, args);
    }
}
