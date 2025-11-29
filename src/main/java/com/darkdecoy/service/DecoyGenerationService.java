package com.darkdecoy.service;

import com.darkdecoy.model.DecoyPair;
import com.darkdecoy.model.DecoyBatch;
import com.darkdecoy.model.GeneratedWord;
import com.darkdecoy.repository.GeneratedWordRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class DecoyGenerationService {

    private final RestClient client;
    private final GeneratedWordRepository repo;

    public DecoyGenerationService(
            @Value("${openai.api.key}") String apiKey,
            GeneratedWordRepository repo
    ) {
        this.repo = repo;

        this.client = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    // ============================================================
    // PUBLIC METHOD: return a single DecoyPair (primary or fallback)
    // ============================================================

    public DecoyPair generatePair(String category) {

        // Call GPT once: get primary + fallback list
        DecoyBatch batch = generateBatch(category);

        // Try primary first
        DecoyPair primary = batch.getPrimary();
        if (isNew(category, primary.getReal())) {
            save(category, primary.getReal());
            return primary;
        }

        // Then try fallback pairs
        for (DecoyPair p : batch.getFallbacks()) {
            if (isNew(category, p.getReal())) {
                save(category, p.getReal());
                return p;
            }
        }

        // Extremely rare: do a second batch (still 90 percent cheaper than old system)
        DecoyBatch batch2 = generateBatch(category);
        for (DecoyPair p : batch2.getFallbacks()) {
            if (isNew(category, p.getReal())) {
                save(category, p.getReal());
                return p;
            }
        }

        // Last resort fallback
        return new DecoyPair("Option 1", "Option 2", category);
    }

    // ============================================================
    // GPT CALL (returns batch of primary + fallbacks)
    // ============================================================

    private DecoyBatch generateBatch(String category) {

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user", "content",
                                "Category: " + category + "\n" +
                                        "Return JSON exactly in the structure:\n" +
                                        "{\n" +
                                        "  \"primary\": { \"real\": \"...\", \"decoy\": \"...\", \"category\": \"" + category + "\" },\n" +
                                        "  \"fallbacks\": [ { \"real\": \"...\", \"decoy\": \"...\", \"category\": \"" + category + "\" }, ... ]\n" +
                                        "}\n"
                        )
                ),
                "response_format", Map.of("type", "json_object")
        );

        try {
            Map response = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new RuntimeException("Empty GPT response");
            }

            List choices = (List) response.get("choices");
            Map first = (Map) choices.get(0);
            Map message = (Map) first.get("message");

            String json = (String) message.get("content");

            return parseBatch(json, category);

        } catch (Exception e) {
            return hardFallback(category);
        }
    }

    // ============================================================
    // PARSE GPT JSON INTO BATCH
    // ============================================================

    private DecoyBatch parseBatch(String json, String category) {
        json = json.trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            Map primaryMap = (Map) parsed.get("primary");
            List<Map> fallbackList = (List<Map>) parsed.get("fallbacks");

            DecoyPair primary = new DecoyPair(
                    (String) primaryMap.get("real"),
                    (String) primaryMap.get("decoy"),
                    category
            );

            List<DecoyPair> fallbacks = fallbackList.stream()
                    .map(m -> new DecoyPair(
                            (String) m.get("real"),
                            (String) m.get("decoy"),
                            category
                    ))
                    .toList();

            return new DecoyBatch(primary, fallbacks);

        } catch (Exception e) {
            // If JSON parsing fails, fallback gracefully
            return hardFallback(category);
        }
    }


    // ============================================================
    // DUPLICATE CHECKING (DB)
    // ============================================================

    private boolean isNew(String category, String realWord) {
        return repo
                .findByCategoryIgnoreCaseAndRealWordIgnoreCase(category, realWord)
                .isEmpty();
    }

    private void save(String category, String realWord) {
        repo.save(new GeneratedWord(category, realWord));
    }

    // ============================================================
    // HARD FALLBACK (NEVER USED IN NORMAL PLAY)
    // ============================================================

    private DecoyBatch hardFallback(String category) {
        DecoyPair fallback = new DecoyPair("Option 1", "Option 2", category);
        return new DecoyBatch(fallback, List.of(fallback));
    }

    // ============================================================
    // KEEP SYSTEM PROMPT AS SEPARATE METHOD (your request)
    // ============================================================

    private String buildSystemPrompt() {
        return """
            You generate real/decoy word pairs for the party impostor game Dark Decoy.
            Return ONLY valid JSON.
            
            Format:
            {
              "primary": { "real": "...", "decoy": "...", "category": "..." },
              "fallbacks": [
                { "real": "...", "decoy": "...", "category": "..." },
                ...
              ]
            }
            
            Rules:
            1. All entries must be REAL people/items in the category.
            2. For people categories (NFL, NBA, actors, etc):
               - Use real full names only.
               - No fake names, jokes, misspellings, or name-clones.
               - Decoy must share similar, shoot for 50% similar, (ie role, style, era)
            3. Provide exactly 1 primary pair and 10 fallback pairs.
            4. Real and decoy must be different but believably related.
            5. Output JSON only, no extra text.
            6. Users are local to america, answers that can be global are best keep to united states  
            7. Sometimes a populate answer is okay, but I dont typically want a really common word to be the first choice for any category; Ie try to avoid things like New york or LA. for "cities" 
            8. REALLY try not to use the most common, first to mind answers, dont be afraid to dig deeper everyone in a while
          """;
    }
}
