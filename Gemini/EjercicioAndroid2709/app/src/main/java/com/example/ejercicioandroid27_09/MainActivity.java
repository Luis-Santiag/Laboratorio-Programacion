package com.example.ejercicioandroid27_09;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView quoteText;
    private TextView translatedQuoteText;
    private TextView quoteAuthor;
    private RequestQueue requestQueue;
    private Translator englishSpanishTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quoteText = findViewById(R.id.quote_text);
        translatedQuoteText = findViewById(R.id.translated_quote_text);
        quoteAuthor = findViewById(R.id.quote_author);
        Button generateButton = findViewById(R.id.generate_button);

        requestQueue = Volley.newRequestQueue(this);

        // Configurar el traductor
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.SPANISH)
                .build();
        englishSpanishTranslator = Translation.getClient(options);

        // Gestionar el ciclo de vida del traductor para liberar recursos
        getLifecycle().addObserver(englishSpanishTranslator);

        generateButton.setOnClickListener(v -> fetchQuote());
        
        // Cargar una frase al iniciar la app
        fetchQuote();
    }

    private void fetchQuote() {
        String url = "https://zenquotes.io/api/random";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject quoteObject = response.getJSONObject(0);
                        String quote = quoteObject.getString("q");
                        String author = quoteObject.getString("a");

                        quoteText.setText(String.format("\"%s\"", quote));
                        quoteAuthor.setText(String.format("- %s", author));
                        translatedQuoteText.setText("[Traduciendo...]");

                        // Traducir la frase después de obtenerla
                        translateQuote(quote);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        quoteText.setText("Error al parsear la frase.");
                        quoteAuthor.setText("");
                        translatedQuoteText.setText("");
                    }
                },
                error -> {
                    error.printStackTrace();
                    quoteText.setText("Error al obtener la frase.");
                    quoteAuthor.setText("");
                    translatedQuoteText.setText("");
                });

        requestQueue.add(jsonArrayRequest);
    }

    private void translateQuote(String text) {
        // Primero, nos aseguramos de que el modelo de traducción esté descargado.
        englishSpanishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(aVoid -> {
                    // El modelo está listo, ahora podemos traducir.
                    englishSpanishTranslator.translate(text)
                            .addOnSuccessListener(translatedText -> translatedQuoteText.setText(String.format("\"%s\"", translatedText)))
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                translatedQuoteText.setText("[Error de traducción]");
                            });
                })
                .addOnFailureListener(exception -> {
                    // No se pudo descargar el modelo. Probablemente no hay conexión a internet.
                    exception.printStackTrace();
                    translatedQuoteText.setText("[Traducción no disponible. Verifique su conexión a internet.]");
                });
    }
}
