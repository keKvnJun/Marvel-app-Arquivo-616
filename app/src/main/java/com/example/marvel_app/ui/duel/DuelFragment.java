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
import com.example.marvel_app.domain.duel.DuelCard;
import com.example.marvel_app.domain.duel.DuelRules;
import com.example.marvel_app.domain.duel.DuelStats;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class DuelFragment extends Fragment {
    private final Random random = new Random();
    private final List<DuelCard> cards = Arrays.asList(
            card("spider", "Spider-Man", 82, 78, 95, 72),
            card("iron", "Iron Man", 88, 76, 96, 68),
            card("storm", "Storm", 92, 70, 84, 80),
            card("hulk", "Hulk", 76, 79, 97, 60),
            card("captain", "Captain America", 54, 100, 98, 94),
            card("strange", "Doctor Strange", 100, 71, 82, 66)
    );
    private DuelCard player;
    private DuelCard opponent;
    private int round = 1;
    private int playerScore;
    private int opponentScore;
    private View root;
    private MaterialButton duelButton;
    private Runnable nextDeal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_duel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        root = view;
        duelButton = view.findViewById(R.id.start_duel_button);
        deal();
        duelButton.setOnClickListener(button -> playRound());
        Toast.makeText(requireContext(),
                "Modo demonstrativo: os índices são uma mecânica do Arquivo 616, não níveis oficiais.",
                Toast.LENGTH_LONG).show();
    }

    private void deal() {
        player = cards.get(random.nextInt(cards.size()));
        do {
            opponent = cards.get(random.nextInt(cards.size()));
        } while (opponent.getId().equals(player.getId()));
        bindCard(root.findViewById(R.id.player_duel_card), player, true, false);
        bindCard(root.findViewById(R.id.opponent_duel_card), opponent, false, false);
        updateHeader();
    }

    private void playRound() {
        duelButton.setEnabled(false);
        if (round > 3) {
            round = 1;
            playerScore = 0;
            opponentScore = 0;
            duelButton.setText(R.string.duel_start);
            deal();
            duelButton.setEnabled(true);
            return;
        }
        DuelRules.Category category = selectedCategory();
        DuelRules.Outcome outcome = DuelRules.compare(player, opponent, category);
        if (outcome == DuelRules.Outcome.FIRST_WINS) playerScore++;
        if (outcome == DuelRules.Outcome.SECOND_WINS) opponentScore++;
        bindCard(root.findViewById(R.id.player_duel_card), player, true, true);
        bindCard(root.findViewById(R.id.opponent_duel_card), opponent, false, true);
        String message = outcome == DuelRules.Outcome.TIE ? "Empate nos arquivos."
                : outcome == DuelRules.Outcome.FIRST_WINS ? "Seu dossiê venceu a rodada."
                : "O Arquivo venceu a rodada.";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        round++;
        updateHeader();
        if (round <= 3) {
            nextDeal = () -> {
                if (root != null) {
                    deal();
                    duelButton.setEnabled(true);
                }
            };
            root.postDelayed(nextDeal, 900L);
        } else {
            duelButton.setText("Jogar novamente");
            duelButton.setEnabled(true);
        }
    }

    private DuelRules.Category selectedCategory() {
        int selected = ((com.google.android.material.chip.ChipGroup)
                root.findViewById(R.id.duel_attribute_group)).getCheckedChipId();
        if (selected == R.id.attribute_experience_chip) return DuelRules.Category.EDITORIAL_HISTORY;
        if (selected == R.id.attribute_appearances_chip) return DuelRules.Category.PRESENCE;
        if (selected == R.id.attribute_impact_chip) return DuelRules.Category.MULTIVERSE_INDEX;
        return DuelRules.Category.VERSATILITY;
    }

    private void bindCard(View container, DuelCard card, boolean user, boolean reveal) {
        ((TextView) container.findViewById(R.id.duel_card_label)).setText(
                user ? R.string.duel_your_card : R.string.duel_opponent);
        ((TextView) container.findViewById(R.id.duel_card_name)).setText(
                user || reveal ? card.getName() : "ARQUIVO SIGILOSO");
        TextView value = container.findViewById(R.id.duel_card_value);
        value.setText(String.valueOf(valueFor(card, selectedCategory())));
        value.setVisibility(reveal ? View.VISIBLE : View.INVISIBLE);
        ImageView image = container.findViewById(R.id.duel_card_image);
        image.setImageResource(user || reveal ? R.drawable.comic_collage_halftone : R.drawable.comic_collage_muted);
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

    private void updateHeader() {
        ((TextView) root.findViewById(R.id.duel_round_text)).setText(
                getString(R.string.duel_round, Math.min(round, 3)));
        ((TextView) root.findViewById(R.id.duel_score_text)).setText(
                getString(R.string.duel_score, playerScore, opponentScore));
    }

    private static DuelCard card(String id, String name, int versatility, int history,
                                 int presence, int alliances) {
        return new DuelCard(id, name, "Dossiê", new DuelStats(
                versatility, history, presence, alliances), true,
                "Mecânica do aplicativo baseada em dados editoriais disponíveis.");
    }

    @Override
    public void onDestroyView() {
        if (root != null && nextDeal != null) root.removeCallbacks(nextDeal);
        duelButton = null;
        root = null;
        super.onDestroyView();
    }
}
