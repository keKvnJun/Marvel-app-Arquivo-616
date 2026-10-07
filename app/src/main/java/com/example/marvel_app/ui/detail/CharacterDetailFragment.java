package com.example.marvel_app.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.marvel_app.R;
import com.example.marvel_app.data.local.FavoriteStore;
import com.example.marvel_app.data.local.HistoryStore;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.data.model.ResourceReference;
import com.example.marvel_app.ui.common.NavigationBundles;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public final class CharacterDetailFragment extends Fragment {
    private CharacterCardData card;
    private FavoriteStore favorites;
    private MaterialButton favoriteButton;
    private RelationshipAdapter alliesAdapter;
    private RelationshipAdapter enemiesAdapter;
    private RelationshipAdapter teamsAdapter;
    private ReferenceAdapter appearancesAdapter;
    private String displayNameOverride = "";
    private String realNameOverride = "";
    private boolean relationshipNavigationLocked;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments() == null ? Bundle.EMPTY : getArguments();
        displayNameOverride = args.getString("displayNameOverride", "");
        realNameOverride = args.getString("realNameOverride", "");
        card = new CharacterCardData(
                args.getLong("characterId"), args.getString("objectId", ""),
                args.getString("name", ""), args.getString("realName", ""),
                args.getString("imageUrl", ""), "Marvel Comics", args.getInt("appearances")
        );
        favorites = new FavoriteStore(requireContext());
        alliesAdapter = new RelationshipAdapter(
                R.string.relationship_ally_badge,
                R.color.success_green,
                true,
                this::openRelatedDossier
        );
        enemiesAdapter = new RelationshipAdapter(
                R.string.relationship_enemy_badge,
                R.color.marvel_red,
                true,
                this::openRelatedDossier
        );
        teamsAdapter = new RelationshipAdapter(
                R.string.relationship_team_badge,
                R.color.jarvis_blue,
                false,
                this::openRelatedDossier
        );
        appearancesAdapter = new ReferenceAdapter();
        RecyclerView allies = view.findViewById(R.id.detail_allies_list);
        allies.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        allies.setAdapter(alliesAdapter);
        RecyclerView enemies = view.findViewById(R.id.detail_enemies_list);
        enemies.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        enemies.setAdapter(enemiesAdapter);
        RecyclerView teams = view.findViewById(R.id.detail_teams_list);
        teams.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        teams.setAdapter(teamsAdapter);
        RecyclerView appearances = view.findViewById(R.id.detail_appearances_list);
        appearances.setLayoutManager(new LinearLayoutManager(requireContext()));
        appearances.setAdapter(appearancesAdapter);
        favoriteButton = view.findViewById(R.id.detail_favorite_button);
        bindSummary(view, card);
        updateFavoriteIcon();
        favoriteButton.setOnClickListener(button -> {
            favorites.toggle(card);
            button.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                    requireContext(), R.anim.favorite_pop));
            updateFavoriteIcon();
        });
        view.findViewById(R.id.detail_back_button).setOnClickListener(
                button -> Navigation.findNavController(button).navigateUp());
        new HistoryStore(requireContext()).record(card);

        CharacterDetailViewModel viewModel = new ViewModelProvider(this)
                .get(CharacterDetailViewModel.class);
        viewModel.result().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                bindCharacter(view, result.getData());
            } else {
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        viewModel.load(card.getApiObjectId());
    }

    private void bindSummary(View view, CharacterCardData summary) {
        ((TextView) view.findViewById(R.id.detail_character_name)).setText(summary.getName());
        ((TextView) view.findViewById(R.id.detail_identity)).setText(summary.getRealName());
        setStat(view.findViewById(R.id.detail_powers_stat), "?", getString(R.string.stat_powers));
        setStat(view.findViewById(R.id.detail_appearances_stat),
                String.valueOf(summary.getIssueAppearances()), getString(R.string.stat_appearances));
        setStat(view.findViewById(R.id.detail_teams_stat), "?", getString(R.string.stat_teams));
        loadImage(view.findViewById(R.id.detail_character_image), summary.getImageUrl());
    }

    private void bindCharacter(View view, CharacterDto character) {
        CharacterCardData remoteCard = CharacterCardData.from(character);
        card = new CharacterCardData(
                remoteCard.getId(),
                remoteCard.getApiObjectId(),
                displayNameOverride.isEmpty() ? remoteCard.getName() : displayNameOverride,
                realNameOverride.isEmpty() ? remoteCard.getRealName() : realNameOverride,
                remoteCard.getImageUrl(),
                remoteCard.getPublisherName(),
                remoteCard.getIssueAppearances()
        );
        ((TextView) view.findViewById(R.id.detail_character_name)).setText(card.getName());
        ((TextView) view.findViewById(R.id.detail_identity)).setText(
                card.getRealName().isEmpty() ? "Identidade não catalogada" : card.getRealName());
        TextView description = view.findViewById(R.id.detail_description);
        String raw = !character.getDeck().isEmpty() ? character.getDeck() : character.getDescription();
        if (raw.length() > 5000) raw = raw.substring(0, 5000);
        description.setText(raw.isEmpty() ? getString(R.string.detail_no_description)
                : HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_COMPACT));
        setStat(view.findViewById(R.id.detail_powers_stat),
                String.valueOf(character.getPowers().size()), getString(R.string.stat_powers));
        setStat(view.findViewById(R.id.detail_appearances_stat),
                String.valueOf(character.getCountOfIssueAppearances()), getString(R.string.stat_appearances));
        setStat(view.findViewById(R.id.detail_teams_stat),
                String.valueOf(character.getTeams().size()), getString(R.string.stat_teams));
        loadImage(view.findViewById(R.id.detail_character_image), character.getDetailImageUrl());

        ChipGroup powers = view.findViewById(R.id.detail_powers_group);
        powers.removeAllViews();
        for (ResourceReference power : character.getPowers()) {
            Chip chip = new Chip(requireContext());
            chip.setText(power.getName());
            chip.setCheckable(false);
            powers.addView(chip);
        }
        bindRelationshipLane(view, R.id.detail_allies_label, R.id.detail_allies_list,
                alliesAdapter, character.getFriends());
        bindRelationshipLane(view, R.id.detail_enemies_label, R.id.detail_enemies_list,
                enemiesAdapter, character.getEnemies());
        bindRelationshipLane(view, R.id.detail_teams_label, R.id.detail_teams_list,
                teamsAdapter, character.getTeams());
        appearancesAdapter.submit(character.getFirstAppearedInIssue() == null
                ? java.util.Collections.emptyList()
                : java.util.Collections.singletonList(character.getFirstAppearedInIssue()));
        ((ImageView) view.findViewById(R.id.detail_character_image)).setContentDescription(
                getString(R.string.comic_image_description, card.getName()));
        updateFavoriteIcon();
        new HistoryStore(requireContext()).record(card);
    }

    private void setStat(View container, String value, String label) {
        ((TextView) container.findViewById(R.id.stat_value)).setText(value);
        ((TextView) container.findViewById(R.id.stat_label)).setText(label);
    }

    private void bindRelationshipLane(
            View root,
            int labelId,
            int listId,
            RelationshipAdapter adapter,
            java.util.List<ResourceReference> references
    ) {
        boolean hasItems = references != null && !references.isEmpty();
        root.findViewById(labelId).setVisibility(hasItems ? View.VISIBLE : View.GONE);
        root.findViewById(listId).setVisibility(hasItems ? View.VISIBLE : View.GONE);
        adapter.submit(references);
    }

    private void openRelatedDossier(ResourceReference reference) {
        if (relationshipNavigationLocked || getView() == null) return;
        String objectId = objectIdFrom(reference);
        if (objectId.isEmpty()) return;
        NavController navController = Navigation.findNavController(requireView());
        if (navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.characterDetailFragment) {
            return;
        }
        relationshipNavigationLocked = true;
        CharacterCardData related = new CharacterCardData(
                reference.getId(),
                objectId,
                reference.getName(),
                "",
                "",
                "Marvel Comics",
                0
        );
        navController.navigate(
                R.id.action_detail_to_related_detail,
                NavigationBundles.forCharacter(related)
        );
    }

    private String objectIdFrom(ResourceReference reference) {
        String url = reference.getApiDetailUrl();
        if (!url.isEmpty()) {
            String value = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            int separator = value.lastIndexOf('/');
            if (separator >= 0 && separator < value.length() - 1) {
                return value.substring(separator + 1);
            }
        }
        return reference.getId() > 0 ? "4005-" + reference.getId() : "";
    }

    @Override
    public void onResume() {
        super.onResume();
        relationshipNavigationLocked = false;
    }

    private void loadImage(ImageView image, String url) {
        Glide.with(image).load(url == null || url.isEmpty() ? R.drawable.comic_collage_muted : url)
                .centerCrop().placeholder(R.drawable.comic_collage_muted)
                .error(R.drawable.comic_collage_muted).into(image);
    }

    private void updateFavoriteIcon() {
        boolean selected = favorites.isFavorite(card.getId());
        favoriteButton.setIconResource(selected
                ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
        favoriteButton.setContentDescription(getString(
                selected ? R.string.favorite_remove : R.string.favorite_add));
    }

    @Override
    public void onDestroyView() {
        favoriteButton = null;
        alliesAdapter = null;
        enemiesAdapter = null;
        teamsAdapter = null;
        appearancesAdapter = null;
        displayNameOverride = "";
        realNameOverride = "";
        super.onDestroyView();
    }
}
