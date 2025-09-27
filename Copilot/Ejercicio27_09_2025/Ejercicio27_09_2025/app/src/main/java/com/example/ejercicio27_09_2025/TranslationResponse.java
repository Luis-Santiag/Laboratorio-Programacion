package com.example.ejercicio27_09_2025;

/**
 * Clase modelo para manejar la respuesta de la API de traducción MyMemory
 */
public class TranslationResponse {
    private ResponseData responseData;
    private String responseStatus;
    private String responseDetails;

    public TranslationResponse() {}

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseDetails() {
        return responseDetails;
    }

    public void setResponseDetails(String responseDetails) {
        this.responseDetails = responseDetails;
    }

    /**
     * Clase interna para los datos de respuesta
     */
    public static class ResponseData {
        private String translatedText;
        private double match;

        public ResponseData() {}

        public String getTranslatedText() {
            return translatedText;
        }

        public void setTranslatedText(String translatedText) {
            this.translatedText = translatedText;
        }

        public double getMatch() {
            return match;
        }

        public void setMatch(double match) {
            this.match = match;
        }
    }
}
