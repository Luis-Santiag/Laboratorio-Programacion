package com.example.ejercicio27_09_2025;

/**
 * Clase modelo para representar una cita de la API de ZenQuotes
 */
public class Quote {
    private String q; // Texto de la cita
    private String a; // Autor de la cita
    private String h; // HTML de la cita (opcional)

    // Campos para la traducción
    private String translatedText; // Texto traducido al español
    private String translatedAuthor; // Autor traducido al español

    // Constructor vacío necesario para Gson
    public Quote() {}

    // Constructor con parámetros
    public Quote(String text, String author) {
        this.q = text;
        this.a = author;
    }

    // Getters y setters
    public String getText() {
        return q;
    }

    public void setText(String text) {
        this.q = text;
    }

    public String getAuthor() {
        return a;
    }

    public void setAuthor(String author) {
        this.a = author;
    }

    public String getHtml() {
        return h;
    }

    public void setHtml(String html) {
        this.h = html;
    }

    // Getters y setters para traducción
    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedAuthor() {
        return translatedAuthor;
    }

    public void setTranslatedAuthor(String translatedAuthor) {
        this.translatedAuthor = translatedAuthor;
    }

    @Override
    public String toString() {
        return "\"" + q + "\" - " + a;
    }
}
