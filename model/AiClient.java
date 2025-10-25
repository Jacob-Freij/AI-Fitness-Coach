package model;

import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class AiClient {
    // The API key for Gemini is stored here
    private final String apiKey;
    // This is the URL for the Gemini API endpoint
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";

    // When you make an AiClient, you give it your API key
    public AiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    // This method asks Gemini to make a workout plan based on your info
    public String generateWorkoutPlan(String goals, String level, String time, String fav, String special) throws IOException {
        // Build the prompt for the AI using all your info
        String prompt =
            "Create a brief, focused weekly workout plan for a person while taking into consideration the following:\n" +
            "Goal: " + goals + "\n" +
            "Experience level: " + level + "\n" +
            "Time commitment: " + time + "\n" +
            "Favorite exercises: " + (fav.isEmpty() ? "None specified" : fav) + "\n" +
            "Special conditions: " + (special.isEmpty() ? "None" : special) + "\n" +
            "FORMAT REQUIREMENTS:\n" +
            "1. Include only 1 short paragraph introduction (2-3 sentences maximum)\n" +
            "2. List days with minimal descriptions\n" +
            "3. For each exercise include ONLY: name, sets, reps - simple explanation/reasoning. No more than 1 sentence\n" +
            "4. If exercise is considered above the experience level indicated offer a small explanation\n" +
            "5. No detailed warm-up or cool-down sections if no special conditions\n" +
            "5. For the special condition, specify why an excercise was picked\n" +
            "6. If special condition is entered then off a brief description of warm up, cooldowns \n" +
            "7. Include theory or extended explanations if you think its needed.";
        // Build the URL with your API key
        URI uri = URI.create(API_URL + "?key=" + apiKey);
        URL url = uri.toURL();
        // Open a secure connection to the API
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        // Build the JSON body for the request
        String body = "{"
                + "\"contents\": [{\"parts\":[{\"text\": \"" + escapeJson(prompt) + "\"}]}],"
                + "\"generationConfig\": {"
                + "\"maxOutputTokens\": 800,"
                + "\"temperature\": 0.7"
                + "}"
                + "}";
        // Send the request body to the API
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        // Read the response from the API
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
        }
        // Check if the API call was successful
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Gemini API error response: " + response);
            throw new IOException("Gemini API error: " + response);
        }
        System.out.println("Full Gemini API response: " + response.toString());
        // Try to pull out just the workout plan text from the response
        return extractContent(response.toString());
    }

    // This tries to find the actual workout plan text in the API's JSON response
    private String extractContent(String response) {
        if (response == null || response.isEmpty()) return "Error: Empty response from API";
        try {
            // Look for the main text in the response using known patterns
            String marker = "\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"";
            int idx = response.indexOf(marker);
            if (idx != -1) {
                int start = idx + marker.length();
                int end = response.indexOf("\"}]", start);
                if (end != -1) {
                    String content = response.substring(start, end);
                    return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                }
            }
            marker = "\"parts\": [{\"text\": \"";
            idx = response.indexOf(marker);
            if (idx != -1) {
                int start = idx + marker.length();
                int end = response.indexOf("\"}", start);
                if (end != -1) {
                    String content = response.substring(start, end);
                    return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                }
            }
            // Fallback: try to find any "text" field
            if (response.contains("\"text\": \"")) {
                int textStart = response.indexOf("\"text\": \"") + 9;
                String partial = response.substring(textStart);
                int textEnd = 0;
                boolean inEscape = false;
                for (int i = 0; i < partial.length(); i++) {
                    char c = partial.charAt(i);
                    if (inEscape) inEscape = false;
                    else if (c == '\\') inEscape = true;
                    else if (c == '"') { textEnd = i; break; }
                }
                if (textEnd > 0) {
                    String content = partial.substring(0, textEnd);
                    return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                }
            }
            // Last resort: try to find a "content" field
            if (response.contains("\"content\":")) {
                int start = response.indexOf("\"content\":") + 10;
                int openBraces = 0, closeBraces = 0, end = -1;
                for (int i = start; i < response.length(); i++) {
                    if (response.charAt(i) == '{') openBraces++;
                    if (response.charAt(i) == '}') {
                        closeBraces++;
                        if (closeBraces > openBraces) { end = i; break; }
                    }
                }
                if (end != -1) {
                    String contentJson = response.substring(start, end);
                    if (contentJson.contains("\"text\":")) {
                        int textStart = contentJson.indexOf("\"text\":") + 7;
                        int textEnd = contentJson.indexOf("\"", textStart + 1);
                        if (textEnd != -1) {
                            String content = contentJson.substring(textStart + 1, textEnd);
                            return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                        }
                    }
                }
            }
            // If nothing worked, just return the whole response with an error message
            return "Failed to parse response: " + response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error extracting content: " + e.getMessage();
        }
    }

    // Escapes special characters so the prompt can be safely sent as JSON
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}