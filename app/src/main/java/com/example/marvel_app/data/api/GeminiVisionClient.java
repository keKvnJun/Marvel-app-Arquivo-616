package com.example.marvel_app.data.api;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class GeminiVisionClient {
    public interface Listener {
        void onSuccess(String answer);
        void onError(String message);
    }

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public Call identify(Bitmap bitmap, Listener listener) {
        if (!ApiConfiguration.hasGeminiApiKey()) {
            listener.onError("Adicione GEMINI_API_KEY ao local.properties para ativar o reconhecimento visual.");
            return null;
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 78, bytes);
        String encoded = Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP);

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", "Analise a imagem somente no contexto Marvel. Identifique o personagem, cosplay ou capa provável. Responda em português, de forma curta, informe até três possibilidades e admita incerteza. Não responda sobre temas externos.");
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", "image/jpeg");
        inlineData.addProperty("data", encoded);
        JsonObject imagePart = new JsonObject();
        imagePart.add("inline_data", inlineData);
        JsonArray parts = new JsonArray();
        parts.add(textPart);
        parts.add(imagePart);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject body = new JsonObject();
        body.add("contents", contents);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + "gemini-2.5-flash:generateContent";
        Request request = new Request.Builder().url(url)
                .header("x-goog-api-key", ApiConfiguration.getGeminiApiKey())
                .post(RequestBody.create(body.toString(), JSON)).build();
        Call call = CLIENT.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                listener.onError("O canal visual do JARVIS está indisponível no momento.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response closeable = response) {
                    String payload = closeable.body() == null ? "" : closeable.body().string();
                    if (!closeable.isSuccessful()) {
                        listener.onError("O Gemini recusou a análise da imagem.");
                        return;
                    }
                    try {
                        JsonObject root = JsonParser.parseString(payload).getAsJsonObject();
                        String answer = root.getAsJsonArray("candidates").get(0).getAsJsonObject()
                                .getAsJsonObject("content").getAsJsonArray("parts")
                                .get(0).getAsJsonObject().get("text").getAsString();
                        listener.onSuccess(answer);
                    } catch (RuntimeException exception) {
                        listener.onError("Não consegui interpretar a resposta visual, agente.");
                    }
                }
            }
        });
        return call;
    }
}
