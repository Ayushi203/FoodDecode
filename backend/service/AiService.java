package com.fooddecode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fooddecode.dto.product.ExplanationResponse;
import com.fooddecode.entity.AiResponseCache;
import com.fooddecode.entity.Ingredient;
import com.fooddecode.entity.IngredientExplanation;
import com.fooddecode.entity.Product;
import com.fooddecode.enums.AiRequestType;
import com.fooddecode.exception.ApiException;
import com.fooddecode.repository.AiResponseCacheRepository;
import com.fooddecode.repository.IngredientExplanationRepository;
import com.fooddecode.repository.IngredientRepository;
import com.fooddecode.security.TokenHasher;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final String USER_FACING_AI_ERROR =
            "Something went wrong generating this explanation — please try again in a moment.";

    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-3.6-flash",
            "gemini-3-flash",
            "gemini-flash-latest",
            "gemini-2.0-flash");

    private final IngredientRepository ingredientRepository;
    private final IngredientExplanationRepository explanationRepository;
    private final AiResponseCacheRepository cacheRepository;
    private final RestClient restClient;

    /** Accepts both gemini.api-key (code) and gemini.api-key (common secrets name). */
    @Value("${gemini.api-key:${gemini.api-key:CHANGE_ME}}")
    private String apiKey;

    @Value("${gemini.model:${gemini.model:gemini-3.6-flash}}")
    private String model;

    @Transactional
    public ExplanationResponse explainIngredient(Long ingredientId, String lang) {
        String language = (lang == null || lang.isBlank()) ? "en" : lang;
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ingredient not found"));

        return explanationRepository.findByIngredientIdAndLanguageCode(ingredientId, language)
                .filter(row -> !isLeakedDebugText(row.getSimpleExplanation()))
                .map(row -> new ExplanationResponse(
                        ingredientId, language, row.getSimpleExplanation(), row.isAiGenerated()))
                .orElseGet(() -> {
                    explanationRepository.findByIngredientIdAndLanguageCode(ingredientId, language)
                            .filter(row -> isLeakedDebugText(row.getSimpleExplanation()))
                            .ifPresent(explanationRepository::delete);
                    return generateAndStore(ingredient, language);
                });
    }

    public String translateText(String text, String targetLanguage) {
        return cachedCall(
                AiRequestType.TRANSLATION,
                "translate|" + targetLanguage + "|" + text,
                "Translate the following food-label text into " + targetLanguage
                        + ". Keep it short and plain. Text: " + text);
    }

    public String summarizeProduct(Product product) {
        String prompt = "Summarise this packaged food in 3 short sentences for a shopper: "
                + product.getName() + " by " + product.getBrand()
                + ". Health score " + product.getHealthScore() + "/100. Category: " + product.getCategory();
        return cachedCall(AiRequestType.SUMMARY, "summary|" + product.getId(), prompt);
    }

    public String suggestAlternatives(Product product) {
        String prompt = "Suggest one healthier packaged-food alternative to "
                + product.getName() + " (" + product.getCategory()
                + "). One sentence, no medical claims.";
        return cachedCall(AiRequestType.ALTERNATIVE, "alt|" + product.getId(), prompt);
    }

    public boolean isConfigured() {
        String key = normalizeKey(apiKey);
        return key != null && !key.isBlank() && !"CHANGE_ME".equals(key);
    }

    private ExplanationResponse generateAndStore(Ingredient ingredient, String language) {
        String prompt = "Explain the food ingredient \"" + ingredient.getName()
                + "\" in simple " + language
                + " for a shopper. 2-3 sentences. Mention if it is commonly flagged as "
                + (ingredient.isHarmful() ? ingredient.getHarmCategory() : "generally fine in typical amounts")
                + ". No medical advice.";
        String text = cachedCall(AiRequestType.EXPLANATION, "explain|" + ingredient.getId() + "|" + language, prompt);

        IngredientExplanation row = new IngredientExplanation();
        row.setIngredient(ingredient);
        row.setLanguageCode(language);
        row.setSimpleExplanation(text);
        row.setAiGenerated(true);
        explanationRepository.save(row);
        return new ExplanationResponse(ingredient.getId(), language, text, true);
    }

    private String cachedCall(AiRequestType type, String rawKey, String prompt) {
        String cacheKey = TokenHasher.sha256(type.name() + "|" + rawKey);
        cacheRepository.findByCacheKeyAndExpiresAtAfter(cacheKey, Instant.now())
                .filter(row -> isLeakedDebugText(row.getResponseText()))
                .ifPresent(cacheRepository::delete);
        return cacheRepository.findByCacheKeyAndExpiresAtAfter(cacheKey, Instant.now())
                .map(AiResponseCache::getResponseText)
                .filter(text -> !isLeakedDebugText(text))
                .orElseGet(() -> {
                    String text = callGemini(prompt);
                    AiResponseCache cache = new AiResponseCache();
                    cache.setCacheKey(cacheKey);
                    cache.setRequestType(type);
                    cache.setResponseText(text);
                    cache.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
                    cacheRepository.save(cache);
                    return text;
                });
    }

    private static boolean isLeakedDebugText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase();
        return lower.contains("gemini")
                || lower.contains("api key")
                || lower.contains("application-secrets")
                || lower.contains("secrets.properties")
                || lower.contains("generatecontent");
    }

    private String callGemini(String prompt) {
        String key = normalizeKey(apiKey);
        if (key == null || key.isBlank() || "CHANGE_ME".equals(key)) {
            log.error("Gemini API key is not configured. Set gemini.api-key in application-secrets.properties.");
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, USER_FACING_AI_ERROR);
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        RestClientResponseException lastHttp = null;
        for (String candidate : modelsToTry()) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + candidate + ":generateContent";
            try {
                JsonNode response = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-goog-api-key", key)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
                JsonNode text = response == null ? null : response.at("/candidates/0/content/parts/0/text");
                if (text == null || text.isMissingNode() || text.asText().isBlank()) {
                    log.warn("Gemini model={} returned empty candidates: {}", candidate, response);
                    throw new ApiException(HttpStatus.BAD_GATEWAY, USER_FACING_AI_ERROR);
                }
                log.info("Gemini call succeeded with model={}", candidate);
                return text.asText();
            } catch (ApiException ex) {
                throw ex;
            } catch (RestClientResponseException ex) {
                lastHttp = ex;
                log.error(
                        "Gemini request failed model={} status={} body={}",
                        candidate,
                        ex.getStatusCode().value(),
                        ex.getResponseBodyAsString());
                String errBody = ex.getResponseBodyAsString();
                int status = ex.getStatusCode().value();
                boolean tryNext = status == 404
                        || status == 429
                        || status == 503
                        || (errBody != null && (errBody.contains("NOT_FOUND") || errBody.contains("not found")
                                || errBody.contains("UNAVAILABLE") || errBody.contains("high demand")));
                if (tryNext) {
                    continue;
                }
                throw new ApiException(HttpStatus.BAD_GATEWAY, USER_FACING_AI_ERROR);
            } catch (Exception ex) {
                log.error("Gemini request failed model={} error={}", candidate, ex.toString(), ex);
                throw new ApiException(HttpStatus.BAD_GATEWAY, USER_FACING_AI_ERROR);
            }
        }
        throw new ApiException(HttpStatus.BAD_GATEWAY, USER_FACING_AI_ERROR);
    }

    private List<String> modelsToTry() {
        Set<String> models = new LinkedHashSet<>();
        String configured = model == null ? "" : model.trim();
        if (!configured.isBlank()) {
            models.add(configured);
        }
        models.addAll(FALLBACK_MODELS);
        return new ArrayList<>(models);
    }

    static String normalizeKey(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.trim();
        if (key.length() >= 2
                && ((key.startsWith("\"") && key.endsWith("\""))
                        || (key.startsWith("'") && key.endsWith("'")))) {
            key = key.substring(1, key.length() - 1).trim();
        }
        return key;
    }
}
