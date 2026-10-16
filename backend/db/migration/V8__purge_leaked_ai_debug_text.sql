-- Old Gemini fallbacks stored config/debug text as if they were explanations.

DELETE FROM ingredient_explanations
WHERE simple_explanation ILIKE '%gemini%'
   OR simple_explanation ILIKE '%api key%'
   OR simple_explanation ILIKE '%application-secrets%'
   OR simple_explanation ILIKE '%secrets.properties%';

DELETE FROM ai_response_cache
WHERE response_text ILIKE '%gemini%'
   OR response_text ILIKE '%api key%'
   OR response_text ILIKE '%application-secrets%'
   OR response_text ILIKE '%secrets.properties%';
