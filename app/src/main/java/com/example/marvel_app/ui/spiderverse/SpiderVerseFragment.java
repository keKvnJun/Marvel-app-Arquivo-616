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
        animateEntrance(view, variants);
        view.findViewById(R.id.spider_back_button).setOnClickListener(
                button -> Navigation.findNavController(button).navigateUp());
        view.findViewById(R.id.discover_variants_button).setOnClickListener(
                button -> openQuery("Spider"));
    }

    private void animateEntrance(View root, RecyclerView variants) {
        View header = root.findViewById(R.id.spider_header);
        View title = root.findViewById(R.id.spider_title_text);
        View label = root.findViewById(R.id.spider_file_label);
        View intro = root.findViewById(R.id.spider_intro_text);

        header.setAlpha(0f);
        header.setScaleX(1.08f);
        header.setScaleY(1.08f);
        header.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(420L).start();

        title.setTranslationX(-64f);
        label.setTranslationX(-44f);
        title.setAlpha(0f);
        label.setAlpha(0f);
        title.animate().translationX(0f).alpha(1f).setStartDelay(130L).setDuration(310L).start();
        label.animate().translationX(0f).alpha(1f).setStartDelay(210L).setDuration(280L).start();

        intro.setAlpha(0f);
        intro.setTranslationY(24f);
        variants.setAlpha(0f);
        variants.setTranslationY(36f);
        intro.animate().alpha(1f).translationY(0f).setStartDelay(260L).setDuration(280L).start();
        variants.animate().alpha(1f).translationY(0f).setStartDelay(330L).setDuration(340L).start();
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
