package com.example.ejercicio27_09_2025;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // URLs de las APIs
    private static final String ZENQUOTES_API_URL = "https://zenquotes.io/api/random";
    private static final String TRANSLATION_API_URL = "https://api.mymemory.translated.net/get";

    // Elementos de la interfaz
    private TextView tvQuoteText;
    private TextView tvQuoteTextSpanish;
    private TextView tvQuoteAuthor;
    private TextView tvQuoteAuthorSpanish;
    private MaterialButton btnGenerateQuote;
    private ProgressBar progressBar;
    private TextView tvStatus;

    // Ejecutor para peticiones en segundo plano
    private ExecutorService executor;
    private Handler mainHandler;
    private Gson gson;

    // Variable para almacenar la cita actual
    private Quote currentQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Configuración simplificada de WindowInsets
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar componentes
        initializeComponents();
        setupClickListeners();
    }

    /**
     * Inicializa todos los componentes necesarios
     */
    private void initializeComponents() {
        // Referencias a elementos de la interfaz
        tvQuoteText = findViewById(R.id.tvQuoteText);
        tvQuoteTextSpanish = findViewById(R.id.tvQuoteTextSpanish);
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthor);
        tvQuoteAuthorSpanish = findViewById(R.id.tvQuoteAuthorSpanish);
        btnGenerateQuote = findViewById(R.id.btnGenerateQuote);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        // Inicializar herramientas para peticiones HTTP
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupClickListeners() {
        btnGenerateQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchRandomQuote();
            }
        });
    }

    /**
     * Obtiene una frase aleatoria de la API de ZenQuotes
     */
    private void fetchRandomQuote() {
        // Mostrar indicadores de carga
        showLoading(true);
        updateStatus("Obteniendo frase inspiradora...");

        // Ocultar traducción anterior
        hideTranslation();

        // Realizar petición en segundo plano
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonResponse = makeHttpRequest(ZENQUOTES_API_URL);

                    if (jsonResponse != null) {
                        // Parsear respuesta JSON
                        Type listType = new TypeToken<List<Quote>>() {
                        }.getType();
                        List<Quote> quotes = gson.fromJson(jsonResponse, listType);

                        if (quotes != null && !quotes.isEmpty()) {
                            Quote randomQuote = quotes.get(0);

                            // Actualizar UI en el hilo principal
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    displayQuote(randomQuote);
                                    updateStatus("Traduciendo automáticamente...");
                                }
                            });

                            // Inmediatamente traducir la frase automáticamente
                            translateQuoteAutomatically(randomQuote);

                        } else {
                            handleError("No se pudo obtener la frase");
                        }
                    } else {
                        handleError("Error al conectar con el servidor");
                    }

                } catch (Exception e) {
                    handleError("Error inesperado: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Realiza una petición HTTP usando HttpURLConnection
     */
    private String makeHttpRequest(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds
            connection.setRequestProperty("User-Agent", "MotivationalQuotes/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            } else {
                return null;
            }

        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Traduce automáticamente una cita al español
     */
    private void translateQuoteAutomatically(Quote quote) {
        if (quote == null) return;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Traducir el texto de la cita
                    String translatedText = translateText(quote.getText(), "en", "es");
                    // Traducir el autor
                    String translatedAuthor = translateText(quote.getAuthor(), "en", "es");

                    if (translatedText != null && translatedAuthor != null) {
                        quote.setTranslatedText(translatedText);
                        quote.setTranslatedAuthor(translatedAuthor);

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                displayTranslation(quote);
                                showLoading(false);
                                updateStatus("¡Frase obtenida y traducida exitosamente!");

                                // Ocultar mensaje de estado después de 2 segundos
                                mainHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideStatus();
                                    }
                                }, 2000);
                            }
                        });
                    } else {
                        // Si falla la traducción, solo mostrar la frase original
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showLoading(false);
                                updateStatus("Frase obtenida (traducción no disponible)");
                                mainHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideStatus();
                                    }
                                }, 2000);
                            }
                        });
                    }

                } catch (Exception e) {
                    // Si falla la traducción, solo mostrar la frase original
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showLoading(false);
                            updateStatus("Frase obtenida (traducción no disponible)");
                            mainHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideStatus();
                                }
                            }, 2000);
                        }
                    });
                }
            }
        });
    }

    /**
     * Traduce un texto usando la API de MyMemory
     */
    private String translateText(String text, String fromLang, String toLang) {
        try {
            // Codificar el texto para la URL
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String urlString = TRANSLATION_API_URL + "?q=" + encodedText + "&langpair=" + fromLang + "|" + toLang;

            String jsonResponse = makeHttpRequest(urlString);

            if (jsonResponse != null) {
                TranslationResponse translationResponse = gson.fromJson(jsonResponse, TranslationResponse.class);

                if (translationResponse != null &&
                        translationResponse.getResponseData() != null &&
                        translationResponse.getResponseData().getTranslatedText() != null) {

                    return translationResponse.getResponseData().getTranslatedText();
                }
            }
        } catch (Exception e) {
            // Error en la traducción
        }

        return null;
    }

    /**
     * Muestra la frase obtenida en la interfaz
     */
    private void displayQuote(Quote quote) {
        if (quote != null) {
            currentQuote = quote;
            tvQuoteText.setText("\"" + quote.getText() + "\"");
            tvQuoteAuthor.setText("- " + quote.getAuthor());
            tvQuoteAuthor.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Muestra la traducción en la interfaz
     */
    private void displayTranslation(Quote quote) {
        if (quote != null && quote.getTranslatedText() != null && quote.getTranslatedAuthor() != null) {
            tvQuoteTextSpanish.setText("\"" + quote.getTranslatedText() + "\"");
            tvQuoteTextSpanish.setVisibility(View.VISIBLE);

            tvQuoteAuthorSpanish.setText("- " + quote.getTranslatedAuthor());
            tvQuoteAuthorSpanish.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Oculta la traducción
     */
    private void hideTranslation() {
        tvQuoteTextSpanish.setVisibility(View.GONE);
        tvQuoteAuthorSpanish.setVisibility(View.GONE);
    }

    /**
     * Maneja errores en las peticiones
     */
    private void handleError(String errorMessage) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                showLoading(false);
                updateStatus("Error: " + errorMessage);
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                // Ocultar mensaje de error después de 3 segundos
                mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideStatus();
                    }
                }, 3000);
            }
        });
    }

    /**
     * Muestra u oculta los indicadores de carga
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerateQuote.setEnabled(!show);

        if (show) {
            btnGenerateQuote.setText("Cargando...");
        } else {
            btnGenerateQuote.setText("✨ Generar Frase Motivacional");
        }
    }

    /**
     * Actualiza el mensaje de estado
     */
    private void updateStatus(String message) {
        tvStatus.setText(message);
        tvStatus.setVisibility(View.VISIBLE);
    }

    /**
     * Oculta el mensaje de estado
     */
    private void hideStatus() {
        tvStatus.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar recursos
        if (executor != null) {
            executor.shutdown();
        }
    }
}