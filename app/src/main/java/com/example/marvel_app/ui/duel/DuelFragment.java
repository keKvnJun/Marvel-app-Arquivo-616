package com.example.marvel_app.ui.duel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.marvel_app.R;
import com.example.marvel_app.data.local.SharedPreferencesCardStorage;
import com.example.marvel_app.domain.cards.CardCatalog;
import com.example.marvel_app.domain.cards.CardCollectionState;
import com.example.marvel_app.domain.cards.CardCollectionStore;
import com.example.marvel_app.domain.cards.CollectibleCard;
import com.example.marvel_app.domain.cards.PackOpeningService;
import com.example.marvel_app.domain.duel.DuelCard;
import com.example.marvel_app.domain.duel.DuelRules;
import com.example.marvel_app.domain.duel.DuelStats;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Random;

public final class DuelFragment extends Fragment {
    private static final int PACK_SIZE = 3;

    private final Random random = new Random();
    private CardCollectionStore collectionStore;
    private PackOpeningService packService;
    private View root;
    private MaterialButton duelButton;
    private MaterialButton cycleButton;
    private ChipGroup attributeGroup;
    private CollectibleCard player;
    private CollectibleCard opponent;
    private int selectedDeckIndex;
    private int round = 1;
    private int playerScore;
    private int opponentScore;
    private boolean roundRevealed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_duel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        root = view;
        collectionStore = new CardCollectionStore(new SharedPreferencesCardStorage(requireContext()));
        packService = new PackOpeningService(CardCatalog.all(), random);
        duelButton = view.findViewById(R.id.start_duel_button);
        cycleButton = view.findViewById(R.id.cycle_card_button);
        attributeGroup = view.findViewById(R.id.duel_attribute_group);

        view.findViewById(R.id.open_pack_button).setOnClickListener(button -> openPack());
        duelButton.setOnClickListener(button -> playRound());
        cycleButton.setOnClickListener(button -> cyclePlayerCard());
        attributeGroup.setOnCheckedStateChangeListener(
                (group, checkedIds) -> refreshTable(roundRevealed));

        refreshCollectionSummary();
        prepareRound();
    }

    private void openPack() {
        CardCollectionState current = collectionStore.load();
        List<CollectibleCard> pack = packService.openPack(current.getOwnedIds(), PACK_SIZE);
        int newCards = collectionStore.addPack(pack);
        collectionStore.fillDeckFromCollection();

        View packPanel = root.findViewById(R.id.pack_results);
        packPanel.setVisibility(View.VISIBLE);
        int[] resultIds = {R.id.pack_result_one, R.id.pack_result_two, R.id.pack_result_three};
        for (int index = 0; index < resultIds.length; index++) {
            TextView cardView = root.findViewById(resultIds[index]);
            if (index < pack.size()) {
                CollectibleCard card = pack.get(index);
                cardView.setText(getString(R.string.pack_card_result,
                        card.getDuelCard().getRarity(), card.getCharacter().getName()));
                cardView.setVisibility(View.VISIBLE);
                cardView.setAlpha(0f);
                cardView.setScaleX(0.82f);
                cardView.setScaleY(0.82f);
                cardView.animate()
                        .alpha(1f).scaleX(1f).scaleY(1f)
                        .setStartDelay(index * 140L)
                        .setDuration(320L)
                        .start();
            } else {
                cardView.setVisibility(View.GONE);
            }
        }
        ((TextView) root.findViewById(R.id.pack_status_text)).setText(
                getResources().getQuantityString(R.plurals.pack_new_cards, newCards, newCards));
        refreshCollectionSummary();
        resetMatch();
    }

    private void cyclePlayerCard() {
        List<CollectibleCard> deck = collectionStore.deckCards();
        if (deck.isEmpty() || roundRevealed) {
            return;
        }
        selectedDeckIndex = (selectedDeckIndex + 1) % deck.size();
        player = deck.get(selectedDeckIndex);
        chooseOpponent();
        refreshTable(false);
    }

    private void playRound() {
        if (round > 3) {
            resetMatch();
            return;
        }
        if (player == null || opponent == null) {
            Toast.makeText(requireContext(), R.string.deck_needed, Toast.LENGTH_LONG).show();
            return;
        }

        roundRevealed = true;
        setAttributesEnabled(false);
        DuelRules.Outcome outcome = DuelRules.compare(
                player.getDuelCard(), opponent.getDuelCard(), selectedCategory());
        if (outcome == DuelRules.Outcome.FIRST_WINS) {
            playerScore++;
        } else if (outcome == DuelRules.Outcome.SECOND_WINS) {
            opponentScore++;
        }
        refreshTable(true);
        String roundMessage = outcomeMessage(outcome);

        round++;
        updateHeader();
        if (round <= 3) {
            Toast.makeText(requireContext(), roundMessage, Toast.LENGTH_SHORT).show();
            duelButton.setText(R.string.duel_next_round);
            duelButton.setOnClickListener(button -> {
                prepareRound();
                duelButton.setOnClickListener(next -> playRound());
            });
        } else {
            duelButton.setText(R.string.duel_play_again);
            Toast.makeText(
                    requireContext(),
                    roundMessage + "\n" + matchResultMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void resetMatch() {
        round = 1;
        playerScore = 0;
        opponentScore = 0;
        selectedDeckIndex = 0;
        duelButton.setText(R.string.duel_start);
        duelButton.setOnClickListener(button -> playRound());
        prepareRound();
    }

    private void prepareRound() {
        roundRevealed = false;
        setAttributesEnabled(true);
        List<CollectibleCard> deck = collectionStore.deckCards();
        player = deck.isEmpty() ? null : deck.get(selectedDeckIndex % deck.size());
        chooseOpponent();
        refreshTable(false);
        updateHeader();
        cycleButton.setEnabled(deck.size() > 1);
        duelButton.setEnabled(player != null);
        duelButton.setText(R.string.duel_start);
    }

    private void chooseOpponent() {
        if (player == null) {
            opponent = null;
            return;
        }
        List<CollectibleCard> catalog = CardCatalog.all();
        do {
            opponent = catalog.get(random.nextInt(catalog.size()));
        } while (opponent.getId().equals(player.getId()) && catalog.size() > 1);
    }

    private void refreshTable(boolean revealOpponent) {
        if (root == null) {
            return;
        }
        bindCard(root.findViewById(R.id.player_duel_card), player, true, player != null);
        bindCard(root.findViewById(R.id.opponent_duel_card), opponent, false, revealOpponent);
    }

    private void bindCard(View container, CollectibleCard collectible, boolean user, boolean reveal) {
        TextView label = container.findViewById(R.id.duel_card_label);
        TextView name = container.findViewById(R.id.duel_card_name);
        TextView rarity = container.findViewById(R.id.duel_card_rarity);
        TextView attribute = container.findViewById(R.id.duel_card_attribute_label);
        TextView value = container.findViewById(R.id.duel_card_value);
        ImageView image = container.findViewById(R.id.duel_card_image);
        label.setText(user ? R.string.duel_your_card : R.string.duel_opponent);

        if (collectible == null) {
            name.setText(user ? R.string.deck_empty_short : R.string.duel_classified);
            rarity.setText("");
            attribute.setText("");
            value.setVisibility(View.INVISIBLE);
            image.setImageResource(R.drawable.comic_collage_muted);
            return;
        }

        DuelCard card = collectible.getDuelCard();
        name.setText(user || reveal ? card.getName() : getString(R.string.duel_classified));
        rarity.setText(user || reveal ? card.getRarity() : "");
        attribute.setText(user || reveal ? categoryLabel(selectedCategory()) : "");
        value.setText(String.valueOf(valueFor(card, selectedCategory())));
        value.setVisibility(user || reveal ? View.VISIBLE : View.INVISIBLE);
        image.setImageResource(user || reveal
                ? R.drawable.comic_collage_halftone
                : R.drawable.comic_collage_muted);
    }

    private DuelRules.Category selectedCategory() {
        int selected = attributeGroup.getCheckedChipId();
        if (selected == R.id.attribute_experience_chip) return DuelRules.Category.EDITORIAL_HISTORY;
        if (selected == R.id.attribute_appearances_chip) return DuelRules.Category.PRESENCE;
        if (selected == R.id.attribute_impact_chip) return DuelRules.Category.MULTIVERSE_INDEX;
        return DuelRules.Category.VERSATILITY;
    }

    private int categoryLabel(DuelRules.Category category) {
        switch (category) {
            case EDITORIAL_HISTORY: return R.string.duel_experience;
            case PRESENCE: return R.string.duel_appearances;
            case ALLIANCES: return R.string.duel_alliances;
            case MULTIVERSE_INDEX: return R.string.duel_impact;
            default: return R.string.duel_powers;
        }
    }

    private int valueFor(DuelCard card, DuelRules.Category category) {
        DuelStats stats = card.getStats();
        switch (category) {
            case EDITORIAL_HISTORY: return stats.getEditorialHistory();
            case PRESENCE: return stats.getPresence();
            case ALLIANCES: return stats.getAlliances();
            case MULTIVERSE_INDEX: return stats.getMultiverseIndex();
            default: return stats.getVersatility();
        }
    }

    private String outcomeMessage(DuelRules.Outcome outcome) {
        if (outcome == DuelRules.Outcome.TIE) return getString(R.string.duel_tie);
        if (outcome == DuelRules.Outcome.FIRST_WINS) return getString(R.string.duel_win);
        if (outcome == DuelRules.Outcome.SECOND_WINS) return getString(R.string.duel_loss);
        return getString(R.string.duel_invalid);
    }

    private String matchResultMessage() {
        if (playerScore > opponentScore) {
            return getString(R.string.duel_match_win);
        }
        if (opponentScore > playerScore) {
            return getString(R.string.duel_match_loss);
        }
        return getString(R.string.duel_match_tie);
    }

    private void refreshCollectionSummary() {
        CardCollectionState state = collectionStore.load();
        ((TextView) root.findViewById(R.id.collection_summary_text)).setText(getString(
                R.string.collection_summary,
                state.getOwnedIds().size(), state.getDeckIds().size(),
                CardCollectionStore.MAX_DECK_SIZE));
    }

    private void updateHeader() {
        ((TextView) root.findViewById(R.id.duel_round_text)).setText(
                getString(R.string.duel_round, Math.min(round, 3)));
        ((TextView) root.findViewById(R.id.duel_score_text)).setText(
                getString(R.string.duel_score, playerScore, opponentScore));
    }

    private void setAttributesEnabled(boolean enabled) {
        if (attributeGroup == null) {
            return;
        }
        for (int index = 0; index < attributeGroup.getChildCount(); index++) {
            attributeGroup.getChildAt(index).setEnabled(enabled);
        }
    }

    @Override
    public void onDestroyView() {
        root = null;
        duelButton = null;
        cycleButton = null;
        attributeGroup = null;
        collectionStore = null;
        packService = null;
        super.onDestroyView();
    }
}
