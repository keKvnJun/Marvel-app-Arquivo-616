package com.example.marvel_app.ui.jarvis;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marvel_app.R;
import com.example.marvel_app.data.api.GeminiVisionClient;
import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.data.repository.CharacterRepository;
import com.example.marvel_app.domain.jarvis.JarvisResponses;
import com.example.marvel_app.domain.jarvis.MarvelTopicClassifier;

import java.util.ArrayList;
import java.util.List;

public final class JarvisFragment extends Fragment {
    private final List<ChatMessage> messages = new ArrayList<>();
    private final MarvelTopicClassifier classifier = new MarvelTopicClassifier();
    private ChatMessageAdapter adapter;
    private RecyclerView list;
    private EditText input;
    private okhttp3.Call visionCall;
    private retrofit2.Call<?> dataCall;

    private final ActivityResultLauncher<Void> camera = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(), this::analyzeImage);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jarvis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        list = view.findViewById(R.id.chat_messages_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatMessageAdapter(messages);
        list.setAdapter(adapter);
        input = view.findViewById(R.id.jarvis_input);
        if (messages.isEmpty()) {
            addMessage(new ChatMessage("JARVIS", getString(R.string.jarvis_welcome), false));
        }
        view.findViewById(R.id.jarvis_send_button).setOnClickListener(button -> send());
        input.setOnEditorActionListener((field, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { send(); return true; }
            return false;
        });
        view.findViewById(R.id.suggestion_power_chip).setOnClickListener(button -> {
            input.setText(R.string.jarvis_suggestion_power); send();
        });
        view.findViewById(R.id.suggestion_team_chip).setOnClickListener(button -> {
            input.setText(R.string.jarvis_suggestion_team); send();
        });
        view.findViewById(R.id.camera_scan_card).setOnClickListener(button -> camera.launch(null));
    }

    private void send() {
        String question = input.getText().toString().trim();
        if (question.isEmpty()) return;
        addMessage(new ChatMessage("AGENTE", question, true));
        input.setText("");
        MarvelTopicClassifier.Result result = classifier.classify(question);
        if (result != MarvelTopicClassifier.Result.MARVEL) {
            addMessage(new ChatMessage("JARVIS", JarvisResponses.forClassification(result), false));
            return;
        }
        String subject = resolveSubject(question);
        if (subject.isEmpty()) {
            addMessage(new ChatMessage("JARVIS", JarvisResponses.forClassification(result)
                    + " Para uma consulta factual, informe o nome de um personagem.", false));
            return;
        }
        addMessage(new ChatMessage("JARVIS", "Consultando o dossiê de " + subject + "...", false));
        if (dataCall != null) dataCall.cancel();
        dataCall = CharacterRepository.getInstance(requireContext()).searchCharacters(
                subject, 1, repositoryResult -> {
                    if (repositoryResult.isSuccess() && !repositoryResult.getData().isEmpty()) {
                        CharacterDto character = repositoryResult.getData().get(0);
                        postIfVisible(JarvisResponses.characterSummary(
                                character.getName(), character.getRealName(), character.getDeck()));
                    } else {
                        String message = repositoryResult.getMessage().isEmpty()
                                ? JarvisResponses.noReliableData(subject) : repositoryResult.getMessage();
                        postIfVisible(message);
                    }
                });
    }

    private String resolveSubject(String question) {
        String lower = question.toLowerCase(java.util.Locale.ROOT);
        String[][] subjects = {
                {"homem aranha", "Spider-Man"}, {"spider-man", "Spider-Man"},
                {"miles morales", "Miles Morales"}, {"homem de ferro", "Iron Man"},
                {"iron man", "Iron Man"}, {"tony stark", "Iron Man"},
                {"capitão américa", "Captain America"}, {"capitao america", "Captain America"},
                {"pantera negra", "Black Panther"}, {"viúva negra", "Black Widow"},
                {"viuva negra", "Black Widow"}, {"doutor estranho", "Doctor Strange"},
                {"wanda maximoff", "Scarlet Witch"}, {"feiticeira escarlate", "Scarlet Witch"},
                {"wolverine", "Wolverine"}, {"deadpool", "Deadpool"},
                {"hulk", "Hulk"}, {"thor", "Thor"}, {"loki", "Loki"},
                {"thanos", "Thanos"}, {"venom", "Venom"}, {"stan lee", "Stan Lee"}
        };
        for (String[] subject : subjects) {
            if (lower.contains(subject[0])) return subject[1];
        }
        return "";
    }

    private void analyzeImage(Bitmap bitmap) {
        if (bitmap == null) {
            addMessage(new ChatMessage("JARVIS", "Captura cancelada, agente.", false));
            return;
        }
        addMessage(new ChatMessage("JARVIS", "Escaneando imagem pelo canal visual seguro...", false));
        if (visionCall != null) visionCall.cancel();
        if (dataCall != null) dataCall.cancel();
        visionCall = new GeminiVisionClient().identify(bitmap, new GeminiVisionClient.Listener() {
            @Override public void onSuccess(String answer) {
                postIfVisible(answer + "\n\nResultado visual preliminar. Confirme o nome na aba Explorar.");
            }
            @Override public void onError(String message) {
                postIfVisible(message);
            }
        });
    }

    private void postIfVisible(String message) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (getView() != null && getViewLifecycleOwner().getLifecycle()
                    .getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                addMessage(new ChatMessage("JARVIS", message, false));
            }
        });
    }

    private void addMessage(ChatMessage message) {
        messages.add(message);
        if (adapter != null) {
            adapter.notifyItemInserted(messages.size() - 1);
            list.scrollToPosition(messages.size() - 1);
        }
    }

    @Override
    public void onDestroyView() {
        if (visionCall != null) visionCall.cancel();
        if (list != null) list.setAdapter(null);
        adapter = null;
        list = null;
        input = null;
        super.onDestroyView();
    }
}
