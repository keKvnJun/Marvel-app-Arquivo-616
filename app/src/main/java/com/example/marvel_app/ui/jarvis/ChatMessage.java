package com.example.marvel_app.ui.jarvis;

public final class ChatMessage {
    public final String sender;
    public final String text;
    public final boolean user;

    public ChatMessage(String sender, String text, boolean user) {
        this.sender = sender;
        this.text = text;
        this.user = user;
    }
}
