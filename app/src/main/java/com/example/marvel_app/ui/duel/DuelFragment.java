package com.example.marvel_app.ui.duel;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.example.marvel_app.R;
import com.example.marvel_app.data.local.SharedPreferencesCardStorage;
import com.example.marvel_app.data.repository.CharacterRepository;
import com.example.marvel_app.domain.cards.CardCatalog;
import com.example.marvel_app.domain.cards.CardCollectionState;
import com.example.marvel_app.domain.cards.CardCollectionStore;
import com.example.marvel_app.domain.cards.CollectibleCard;
import com.example.marvel_app.domain.cards.PackOpeningService;
import com.example.marvel_app.domain.duel.DuelCard;
import com.example.marvel_app.domain.duel.DuelRules;
import com.example.marvel_app.domain.duel.DuelStats;
import com.example.marvel_app.ui.widget.PackOpeningView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;

public final class DuelFragment extends Fragment {
    private static final int PACK_SIZE = 3;

    private final Random random = new Random();
    private CardCollectionStore collectionStore;
    private PackOpeningService packService;
    private List<CollectibleCard> hydratedCatalog = CardCatalog.all();
    private Call<?> cardArtCall;
    private View root;
    private MaterialButton duelButton;
    private MaterialButton openPackButton;
    private ChipGroup attributeGroup;
    private TextView resultBanner;
    private PackOpeningView packOpeningView;
    private View battleTable;
    private View missionResultPanel;
    private ImageView missionResultImage;
    private TextView missionResultTitle;
    private TextView missionResultScore;
    private MaterialButton missionContinueButton;
    private View[] handSlots;
    private ImageView[] opponentSlots;
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
        openPackButton = view.findViewById(R.id.open_pack_button);
        attributeGroup = view.findViewById(R.id.duel_attribute_group);
        resultBanner = view.findViewById(R.id.duel_result_banner);
        packOpeningView = view.findViewById(R.id.pack_opening_overlay);
        battleTable = view.findViewById(R.id.battle_table);
        missionResultPanel = view.findViewById(R.id.mission_result_panel);
        missionResultImage = view.findViewById(R.id.mission_result_image);
        missionResultTitle = view.findViewById(R.id.mission_result_title);
        missionResultScore = view.findViewById(R.id.mission_result_score);
        missionContinueButton = view.findViewById(R.id.mission_continue_button);
        handSlots = new View[]{
                view.findViewById(R.id.hand_card_one),
                view.findViewById(R.id.hand_card_two),
                view.findViewById(R.id.hand_card_three)
        };
        opponentSlots = new ImageView[]{
                view.findViewById(R.id.opponent_slot_one),
                view.findViewById(R.id.opponent_slot_two),
                view.findViewById(R.id.opponent_slot_three)
        };

        openPackButton.setEnabled(false);
        openPackButton.setOnClickListener(button -> openPack());
        packOpeningView.setOnDismissListener(() -> setPackModal(false));
        duelButton.setOnClickListener(button -> playRound());
        for (int index = 0; index < handSlots.length; index++) {
            int cardIndex = index;
            handSlots[index].setOnClickListener(card -> selectPlayerCard(cardIndex));
        }
        attributeGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            refreshTable(roundRevealed);
            updateArenaTheme();
        });
        missionContinueButton.setOnClickListener(button -> closeMissionResult());
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (packOpeningView != null
                                && packOpeningView.getVisibility() == View.VISIBLE) {
                            packOpeningView.announceForAccessibility(
                                    getString(R.string.pack_opening_finish_first));
                            return;
                        }
                        if (missionResultPanel != null
                                && missionResultPanel.getVisibility() == View.VISIBLE) {
                            closeMissionResult();
                            return;
                        }
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
        );

        refreshCollectionSummary();
        prepareRound();
        updateArenaTheme();
        loadCardArtwork();
    }

    private void loadCardArtwork() {
        cardArtCall = CharacterRepository.getInstance(requireContext())
                .loadCharactersByIds(CardCatalog.comicVineIds(), result -> {
                    if (root == null) return;
                    openPackButton.setEnabled(true);
                    if (result.isSuccess()) {
                        hydratedCatalog = CardCatalog.hydrate(CardCatalog.all(), result.getData());
                        packService = new PackOpeningService(hydratedCatalog, random);
                        player = hydratedVersion(player);
                        opponent = hydratedVersion(opponent);
                        refreshHand(deckCards());
                        refreshTable(roundRevealed);
                    }
                });
    }

    private void openPack() {
        setPackModal(true);
        CardCollectionState current = collectionStore.load();
        List<CollectibleCard> pack = packService.openPack(current.getOwnedIds(), PACK_SIZE);
        int newCards = collectionStore.addPack(pack);
        collectionStore.fillDeckFromCollection();

        ((TextView) root.findViewById(R.id.pack_status_text)).setText(
                getResources().getQuantityString(R.plurals.pack_new_cards, newCards, newCards));
        refreshCollectionSummary();
        resetMatch();

        packOpeningView.setCards(createPackRevealCards(pack));
        packOpeningView.play(null);
    }

    private List<View> createPackRevealCards(List<CollectibleCard> pack) {
        List<View> cards = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CollectibleCard collectible : pack) {
            View card = inflater.inflate(R.layout.item_pack_reveal_card, packOpeningView, false);
            DuelCard duelCard = collectible.getDuelCard();
            DuelStats stats = duelCard.getStats();
            ((TextView) card.findViewById(R.id.pack_reveal_rarity)).setText(
                    duelCard.getRarity().toUpperCase(Locale.ROOT));
            ((TextView) card.findViewById(R.id.pack_reveal_name)).setText(duelCard.getName());
            ((TextView) card.findViewById(R.id.pack_reveal_stats)).setText(getString(
                    R.string.collection_card_stats,
                    stats.getVersatility(),
                    stats.getEditorialHistory(),
                    stats.getPresence(),
                    stats.getAlliances()
            ));
            loadArtwork(card.findViewById(R.id.pack_reveal_image), collectible, true);
            card.setContentDescription(getString(
                    R.string.pack_card_result,
                    duelCard.getRarity(),
                    duelCard.getName()
            ));
            cards.add(card);
        }
        return cards;
    }

    private void selectPlayerCard(int index) {
        if (roundRevealed) return;
        List<CollectibleCard> deck = deckCards();
        if (index < 0 || index >= deck.size()) return;
        selectedDeckIndex = index;
        player = deck.get(index);
        refreshHand(deck);
        refreshTable(false);
        showResult(getString(R.string.battle_card_selected, player.getDuelCard().getName()),
                ResultStyle.NEUTRAL, false);
    }

    private void playRound() {
        if (round > 3) {
            resetMatch();
            return;
        }
        if (player == null || opponent == null) {
            showResult(getString(R.string.deck_needed), ResultStyle.NEUTRAL, true);
            return;
        }

        roundRevealed = true;
        setControlsEnabled(false);
        DuelRules.Outcome outcome = DuelRules.compare(
                player.getDuelCard(), opponent.getDuelCard(), selectedCategory());
        if (outcome == DuelRules.Outcome.FIRST_WINS) playerScore++;
        else if (outcome == DuelRules.Outcome.SECOND_WINS) opponentScore++;
        refreshTable(true);

        String roundMessage = outcomeMessage(outcome);
        round++;
        updateHeader();
        if (round <= 3) {
            showResult(roundMessage, styleFor(outcome), true);
            duelButton.setText(R.string.duel_next_round);
            duelButton.setEnabled(true);
            duelButton.setOnClickListener(button -> {
                prepareRound();
                duelButton.setOnClickListener(next -> playRound());
            });
        } else {
            showResult(roundMessage + "\n" + matchResultMessage(), styleForMatch(), true);
            duelButton.setText(R.string.duel_play_again);
            duelButton.setEnabled(true);
            showMissionResult();
        }
    }

    private void resetMatch() {
        if (missionResultPanel != null) missionResultPanel.setVisibility(View.GONE);
        round = 1;
        playerScore = 0;
        opponentScore = 0;
        selectedDeckIndex = 0;
        duelButton.setOnClickListener(button -> playRound());
        prepareRound();
    }

    private void prepareRound() {
        roundRevealed = false;
        setControlsEnabled(true);
        List<CollectibleCard> deck = deckCards();
        player = deck.isEmpty() ? null : deck.get(selectedDeckIndex % deck.size());
        chooseOpponent();
        refreshHand(deck);
        refreshTable(false);
        updateHeader();
        duelButton.setEnabled(player != null);
        duelButton.setText(R.string.duel_start);
        showResult(
                getString(player == null ? R.string.battle_open_pack_hint : R.string.battle_ready),
                ResultStyle.NEUTRAL,
                false
        );
    }

    private void chooseOpponent() {
        if (player == null) {
            opponent = null;
            return;
        }
        Set<String> deckIds = new HashSet<>();
        for (CollectibleCard card : deckCards()) deckIds.add(card.getId());
        List<CollectibleCard> candidates = new ArrayList<>();
        for (CollectibleCard card : hydratedCatalog) {
            if (!deckIds.contains(card.getId())) candidates.add(card);
        }
        if (candidates.isEmpty()) {
            for (CollectibleCard card : hydratedCatalog) {
                if (!card.getId().equals(player.getId())) candidates.add(card);
            }
        }
        opponent = candidates.isEmpty()
                ? player
                : candidates.get(random.nextInt(candidates.size()));
    }

    private void refreshHand(List<CollectibleCard> deck) {
        for (int index = 0; index < handSlots.length; index++) {
            View slot = handSlots[index];
            MaterialCardView card = (MaterialCardView) slot;
            TextView name = slot.findViewById(R.id.hand_card_name);
            TextView rarity = slot.findViewById(R.id.hand_card_rarity);
            ImageView image = slot.findViewById(R.id.hand_card_image);
            boolean occupied = index < deck.size();
            if (occupied) {
                CollectibleCard collectible = deck.get(index);
                name.setText(collectible.getDuelCard().getName());
                rarity.setText(collectible.getDuelCard().getRarity().toUpperCase(Locale.ROOT));
                loadArtwork(image, collectible, true);
                slot.setContentDescription(getString(
                        R.string.battle_hand_card_description,
                        collectible.getDuelCard().getName(),
                        index == selectedDeckIndex ? getString(R.string.battle_selected) : ""
                ));
            } else {
                name.setText(R.string.battle_empty_slot);
                rarity.setText(R.string.battle_open_pack_short);
                loadArtwork(image, null, false);
                slot.setContentDescription(getString(R.string.battle_empty_slot));
            }
            boolean selected = occupied && index == selectedDeckIndex;
            card.setStrokeColor(ContextCompat.getColor(requireContext(),
                    selected ? R.color.marvel_red : R.color.outline_color));
            int thinStroke = getResources().getDimensionPixelSize(R.dimen.stroke_thin);
            card.setStrokeWidth(selected ? thinStroke * 3 : thinStroke);
            slot.setAlpha(occupied ? 1f : 0.48f);
            slot.setEnabled(occupied && !roundRevealed);
        }
    }

    private void refreshTable(boolean revealOpponent) {
        if (root == null) return;
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
            loadArtwork(image, null, false);
            return;
        }

        DuelCard card = collectible.getDuelCard();
        name.setText(user || reveal ? card.getName() : getString(R.string.duel_classified));
        rarity.setText(user || reveal ? card.getRarity() : "");
        attribute.setText(user || reveal
                ? getString(categoryLabel(selectedCategory()))
                : "");
        value.setText(String.valueOf(valueFor(card, selectedCategory())));
        value.setVisibility(user || reveal ? View.VISIBLE : View.INVISIBLE);
        loadArtwork(image, collectible, user || reveal);
    }

    private List<CollectibleCard> deckCards() {
        List<CollectibleCard> deck = collectionStore.deckCards();
        List<CollectibleCard> hydrated = new ArrayList<>(deck.size());
        for (CollectibleCard card : deck) {
            hydrated.add(hydratedVersion(card));
        }
        return hydrated;
    }

    private CollectibleCard hydratedVersion(@Nullable CollectibleCard card) {
        if (card == null) return null;
        for (CollectibleCard candidate : hydratedCatalog) {
            if (candidate.getId().equals(card.getId())) return candidate;
        }
        return card;
    }

    private void loadArtwork(ImageView image, @Nullable CollectibleCard card, boolean visible) {
        String imageUrl = card == null ? "" : card.getCharacter().getImageUrl();
        Object source = visible && imageUrl != null && !imageUrl.isEmpty()
                ? imageUrl
                : R.drawable.comic_collage_muted;
        Glide.with(image)
                .load(source)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .thumbnail(0.2f)
                .dontAnimate()
                .centerCrop()
                .placeholder(R.drawable.comic_collage_muted)
                .error(R.drawable.comic_collage_muted)
                .into(image);
        image.setContentDescription(visible && card != null
                ? card.getDuelCard().getName()
                : getString(R.string.battle_rival_hidden_card));
    }

    private void updateArenaTheme() {
        if (battleTable == null) return;
        int base = ContextCompat.getColor(requireContext(), R.color.surface_secondary);
        int accent;
        switch (selectedCategory()) {
            case EDITORIAL_HISTORY:
                accent = ContextCompat.getColor(requireContext(), R.color.jarvis_blue);
                break;
            case PRESENCE:
                accent = ContextCompat.getColor(requireContext(), R.color.success_green);
                break;
            case MULTIVERSE_INDEX:
                accent = ContextCompat.getColor(requireContext(), R.color.comic_yellow);
                break;
            default:
                accent = ContextCompat.getColor(requireContext(), R.color.marvel_red);
        }
        battleTable.setBackgroundTintList(ColorStateList.valueOf(
                ColorUtils.blendARGB(base, accent, 0.14f)));
    }

    private void showMissionResult() {
        CollectibleCard highlight = playerScore >= opponentScore ? player : opponent;
        missionResultTitle.setText(matchResultMessage());
        missionResultScore.setText(getString(
                R.string.mission_result_score, playerScore, opponentScore));
        loadArtwork(missionResultImage, highlight, true);
        setPackModal(true);
        missionResultPanel.setVisibility(View.VISIBLE);
        missionResultPanel.setAlpha(0f);
        missionResultPanel.setScaleX(0.88f);
        missionResultPanel.setScaleY(0.88f);
        missionResultPanel.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(240L)
                .start();
        missionContinueButton.requestFocus();
        missionResultPanel.announceForAccessibility(matchResultMessage());
    }

    private void closeMissionResult() {
        if (missionResultPanel == null) return;
        missionResultPanel.setVisibility(View.GONE);
        setPackModal(false);
        resetMatch();
        if (duelButton != null) duelButton.requestFocus();
    }

    private void setPackModal(boolean visible) {
        if (root != null) {
            View content = root.findViewById(R.id.duel_content);
            content.setImportantForAccessibility(visible
                    ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                    : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }
        if (getActivity() != null) {
            View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
            }
        }
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
        if (playerScore > opponentScore) return getString(R.string.duel_match_win);
        if (opponentScore > playerScore) return getString(R.string.duel_match_loss);
        return getString(R.string.duel_match_tie);
    }

    private ResultStyle styleFor(DuelRules.Outcome outcome) {
        if (outcome == DuelRules.Outcome.FIRST_WINS) return ResultStyle.WIN;
        if (outcome == DuelRules.Outcome.SECOND_WINS) return ResultStyle.LOSS;
        return ResultStyle.TIE;
    }

    private ResultStyle styleForMatch() {
        if (playerScore > opponentScore) return ResultStyle.WIN;
        if (opponentScore > playerScore) return ResultStyle.LOSS;
        return ResultStyle.TIE;
    }

    private void showResult(String message, ResultStyle style, boolean animate) {
        resultBanner.setText(message);
        int color;
        int textColor;
        if (style == ResultStyle.WIN) {
            color = R.color.success_green;
            textColor = R.color.white;
        } else if (style == ResultStyle.LOSS) {
            color = R.color.marvel_red;
            textColor = R.color.white;
        } else if (style == ResultStyle.TIE) {
            color = R.color.comic_yellow;
            textColor = R.color.black;
        } else {
            color = R.color.surface_elevated;
            textColor = R.color.text_primary;
        }
        resultBanner.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), color)));
        resultBanner.setTextColor(ContextCompat.getColor(requireContext(), textColor));
        if (animate) {
            resultBanner.setAlpha(0f);
            resultBanner.setTranslationY(18f);
            resultBanner.animate().alpha(1f).translationY(0f).setDuration(220L).start();
        } else {
            resultBanner.animate().cancel();
            resultBanner.setAlpha(1f);
            resultBanner.setTranslationY(0f);
        }
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
        int completedRounds = Math.min(round - 1, opponentSlots.length);
        for (int index = 0; index < opponentSlots.length; index++) {
            opponentSlots[index].setAlpha(index < completedRounds ? 0.34f : 1f);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        if (attributeGroup != null) {
            for (int index = 0; index < attributeGroup.getChildCount(); index++) {
                attributeGroup.getChildAt(index).setEnabled(enabled);
            }
        }
        if (handSlots != null && collectionStore != null) {
            List<CollectibleCard> deck = deckCards();
            for (int index = 0; index < handSlots.length; index++) {
                handSlots[index].setEnabled(enabled && index < deck.size());
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (cardArtCall != null) {
            cardArtCall.cancel();
            cardArtCall = null;
        }
        if (resultBanner != null) resultBanner.animate().cancel();
        if (missionResultPanel != null) missionResultPanel.animate().cancel();
        if (packOpeningView != null) packOpeningView.dismiss();
        root = null;
        duelButton = null;
        openPackButton = null;
        attributeGroup = null;
        resultBanner = null;
        packOpeningView = null;
        battleTable = null;
        missionResultPanel = null;
        missionResultImage = null;
        missionResultTitle = null;
        missionResultScore = null;
        missionContinueButton = null;
        handSlots = null;
        opponentSlots = null;
        collectionStore = null;
        packService = null;
        super.onDestroyView();
    }

    private enum ResultStyle {
        NEUTRAL,
        WIN,
        LOSS,
        TIE
    }
}
