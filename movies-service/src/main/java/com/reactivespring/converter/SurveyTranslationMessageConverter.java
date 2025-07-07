package com.reactivespring.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactivespring.dto.CustomMessage;
import com.reactivespring.dto.SurveyTranslationRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SurveyTranslationMessageConverter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static CustomMessage convertToMessage(SurveyTranslationRequest request) {
        try {
            String systemPrompt = buildSystemPrompt(request);
            String userContent = buildUserContent(request);
            
            return CustomMessage.userMessage(systemPrompt + "\n\n" + userContent);
            
        } catch (Exception e) {
            log.error("Error converting request to message", e);
            throw new RuntimeException("Failed to convert request to message", e);
        }
    }
    
    private static String buildSystemPrompt(SurveyTranslationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional translator specializing in survey localization. ");
        prompt.append("Your task is to translate all text content in the provided survey from ");
        prompt.append(request.getSourceLanguage()).append(" to ").append(request.getTargetLanguage()).append(". ");
        
        prompt.append("\nTranslation Guidelines:\n");
        prompt.append("1. Maintain the exact JSON structure and field names\n");
        prompt.append("2. Translate all human-readable text content including:\n");
        prompt.append("   - Survey title\n");
        prompt.append("   - Introduction block (title, description, welcome message, instructions)\n");
        prompt.append("   - Section titles and descriptions\n");
        prompt.append("   - Category names and descriptions\n");
        prompt.append("   - Question texts and descriptions\n");
        prompt.append("   - Choice texts\n");
        prompt.append("   - Footer content (thank you message, button text, contact info, additional instructions)\n");
        
        if (request.getOptions() != null) {
            if (request.getOptions().getTranslateChoiceValues() != null && request.getOptions().getTranslateChoiceValues()) {
                prompt.append("   - Choice values (when they are human-readable)\n");
            }
            if (request.getOptions().getTranslateValidationMessages() != null && request.getOptions().getTranslateValidationMessages()) {
                prompt.append("   - Validation error messages\n");
            }
            if (request.getOptions().getTone() != null) {
                prompt.append("3. Use a ").append(request.getOptions().getTone()).append(" tone\n");
            }
            if (request.getOptions().getContext() != null) {
                prompt.append("4. Context: ").append(request.getOptions().getContext()).append("\n");
            }
        }
        
        prompt.append("\nDO NOT translate:\n");
        prompt.append("- Field names/keys\n");
        prompt.append("- Technical values (IDs, enum values, etc.)\n");
        prompt.append("- Timestamps or metadata\n");
        prompt.append("- Choice values that are technical codes\n");
        
        prompt.append("\nReturn ONLY the translated JSON object with the same structure.");
        
        return prompt.toString();
    }
    
    private static String buildUserContent(SurveyTranslationRequest request) throws JsonProcessingException {
        return "Survey to translate:\n" + objectMapper.writeValueAsString(request.getSurvey());
    }
}