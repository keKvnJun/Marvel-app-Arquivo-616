package com.example.marvel_app.ui.spiderverse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.data.repository.CharacterRepository;
import com.example.marvel_app.domain.spiderverse.SpiderVerseCatalog;
import com.example.marvel_app.domain.spiderverse.SpiderVerseSeed;
import com.example.marvel_app.ui.common.NavigationBundles;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public final class SpiderVerseFragment extends Fragment {
    private SpiderVariantAdapter adapter;
    private Call<?> variantsCall;
    private boolean navigatingToDetail;

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
        adapter = new SpiderVariantAdapter(SpiderVerseCatalog.seeds(), this::openDetail);
        variants.setAdapter(adapter);
        loadVariantCards();
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

    private void loadVariantCards() {
        List<Long> ids = new ArrayList<>();
        for (SpiderVerseSeed seed : SpiderVerseCatalog.seeds()) {
            ids.add(seed.getComicVineId());
        }
        variantsCall = CharacterRepository.getInstance(requireContext())
                .loadCharactersByIds(ids, result -> {
                    if (result.isSuccess() && adapter != null) {
                        adapter.submitCharacters(result.getData());
                    }
                });
    }

    private void openDetail(SpiderVerseSeed seed, CharacterCardData character) {
        if (getView() == null || navigatingToDetail) {
            return;
        }
        NavController navController = Navigation.findNavController(requireView());
        if (navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.spiderVerseFragment) {
            return;
        }
        navigatingToDetail = true;
        Bundle args = NavigationBundles.forCharacter(character);
        args.putString("displayNameOverride", seed.getDisplayName());
        args.putString("realNameOverride", seed.getRealName());
        navController.navigate(R.id.action_spider_verse_to_detail, args);
    }

    @Override
    public void onResume() {
        super.onResume();
        navigatingToDetail = false;
    }

    private void openQuery(String query) {
        Bundle args = new Bundle();
        args.putString("initialQuery", query);
        Navigation.findNavController(requireView()).navigate(
                R.id.action_spider_verse_to_explore, args);
    }

    @Override
    public void onDestroyView() {
        if (variantsCall != null) {
            variantsCall.cancel();
            variantsCall = null;
        }
        adapter = null;
        super.onDestroyView();
    }
}
