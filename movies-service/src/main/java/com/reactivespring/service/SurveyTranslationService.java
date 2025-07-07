package com.reactivespring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactivespring.converter.SurveyTranslationMessageConverter;
import com.reactivespring.domain.Survey;
import com.reactivespring.dto.CustomMessage;
import com.reactivespring.dto.SurveyTranslationRequest;
import com.reactivespring.dto.SurveyTranslationResponse;
import com.reactivespring.util.ResponseFormatUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SurveyTranslationService {
    
    private final ChatLanguageModel chatLanguageModel;
    private final SurveyTranslationMessageConverter messageConverter;
    private final ObjectMapper objectMapper;
    private final ResponseFormatUtil responseFormatUtil;
    
    @Autowired
    public SurveyTranslationService(
            ChatLanguageModel chatLanguageModel,
            SurveyTranslationMessageConverter messageConverter,
            ObjectMapper objectMapper,
            ResponseFormatUtil responseFormatUtil) {
        this.chatLanguageModel = chatLanguageModel;
        this.messageConverter = messageConverter;
        this.objectMapper = objectMapper;
        this.responseFormatUtil = responseFormatUtil;
    }
    
    public Mono<SurveyTranslationResponse> translateSurvey(SurveyTranslationRequest request) {
        log.info("Starting translation from {} to {}", request.getSourceLanguage(), request.getTargetLanguage());
        
        return Mono.fromCallable(() -> performTranslation(request))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(response -> log.info("Translation completed successfully"))
                .doOnError(error -> log.error("Translation failed", error));
    }
    
    private SurveyTranslationResponse performTranslation(SurveyTranslationRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Convert request to CustomMessage
            CustomMessage message = messageConverter.convertToMessage(request);
            
            // Create ResponseFormat for structured output
            ResponseFormat responseFormat = responseFormatUtil.createSurveyTranslationResponseFormat();
            
            // Create ChatRequest with structured response format
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(List.of(UserMessage.from(message.text())))
                    .responseFormat(responseFormat)
                    .build();
            
            // Call OpenAI via LangChain4j with structured output
            log.info("Calling AI model with structured response format");
            ChatResponse chatResponse = chatLanguageModel.chat(chatRequest);
            String aiResponseContent = chatResponse.aiMessage().text();
            
            log.debug("AI response received: {}", aiResponseContent);
            
            // Parse the structured response directly to SurveyTranslationResponse
            SurveyTranslationResponse translationResponse = parseStructuredResponse(aiResponseContent, request);
            
            // Update metadata with actual processing time
            SurveyTranslationResponse.TranslationMetadata metadata = buildMetadata(
                    request, startTime, System.currentTimeMillis(), true
            );
            
            // Override metadata in the response
            translationResponse.setMetadata(metadata);
            
            return translationResponse;
                    
        } catch (Exception e) {
            log.error("Error during translation", e);
            
            // Build error metadata
            SurveyTranslationResponse.TranslationMetadata metadata = buildMetadata(
                    request, startTime, System.currentTimeMillis(), false
            );
            
            throw new RuntimeException("Translation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parses the structured AI response into SurveyTranslationResponse
     * The AI should return a complete SurveyTranslationResponse object based on the ResponseFormat schema
     */
    private SurveyTranslationResponse parseStructuredResponse(String response, SurveyTranslationRequest request) {
        try {
            // Clean the response to extract JSON
            String cleanedResponse = extractJsonFromResponse(response);
            
            // Parse the JSON to SurveyTranslationResponse object
            SurveyTranslationResponse translationResponse = objectMapper.readValue(cleanedResponse, SurveyTranslationResponse.class);
            
            // Ensure the translated survey has the correct metadata
            if (translationResponse.getTranslatedSurvey() != null) {
                translationResponse.getTranslatedSurvey().setLanguage(request.getTargetLanguage());
                translationResponse.getTranslatedSurvey().setUpdatedAt(LocalDateTime.now());
            }
            
            // Ensure source and target languages are set correctly
            translationResponse.setSourceLanguage(request.getSourceLanguage());
            translationResponse.setTargetLanguage(request.getTargetLanguage());
            
            return translationResponse;
            
        } catch (Exception e) {
            log.error("Failed to parse structured translation response", e);
            // Fallback: try to parse just the survey if the full response parsing fails
            return parseAsLegacyResponse(response, request);
        }
    }

    /**
     * Fallback method to parse response in the old format (just Survey object)
     * This maintains backward compatibility
     */
    private SurveyTranslationResponse parseAsLegacyResponse(String response, SurveyTranslationRequest request) {
        try {
            log.warn("Falling back to legacy response parsing");
            
            // Clean the response to extract JSON
            String cleanedResponse = extractJsonFromResponse(response);
            
            // Parse the JSON to Survey object
            Survey translatedSurvey = objectMapper.readValue(cleanedResponse, Survey.class);
            
            // Update metadata fields
            translatedSurvey.setLanguage(request.getTargetLanguage());
            translatedSurvey.setUpdatedAt(LocalDateTime.now());
            
            // Create SurveyTranslationResponse manually
            return SurveyTranslationResponse.builder()
                    .translatedSurvey(translatedSurvey)
                    .sourceLanguage(request.getSourceLanguage())
                    .targetLanguage(request.getTargetLanguage())
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to parse response even with legacy format", e);
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        // Remove markdown code blocks if present
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        // Find the first { and last } to extract JSON
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        
        return cleaned.trim();
    }
    
    private SurveyTranslationResponse.TranslationMetadata buildMetadata(
            SurveyTranslationRequest request, 
            long startTime, 
            long endTime, 
            boolean isComplete) {
        
        Map<String, String> translationNotes = new HashMap<>();
        translationNotes.put("model", "OpenAI GPT");
        
        if (request.getOptions() != null) {
            if (request.getOptions().getTone() != null) {
                translationNotes.put("tone", request.getOptions().getTone());
            }
            if (request.getOptions().getContext() != null) {
                translationNotes.put("context", request.getOptions().getContext());
            }
        }
        
        // Count text blocks in the survey
        int totalTextBlocks = countTextBlocks(request.getSurvey());
        
        return SurveyTranslationResponse.TranslationMetadata.builder()
                .translatedAt(LocalDateTime.now())
                .translationModel("OpenAI GPT")
                .totalTextBlocks(totalTextBlocks)
                .translatedBlocks(isComplete ? totalTextBlocks : 0)
                .confidenceScore(isComplete ? 0.95 : 0.0)
                .processingTimeMs(endTime - startTime)
                .translationNotes(translationNotes)
                .isComplete(isComplete)
                .build();
    }
    
    private int countTextBlocks(Survey survey) {
        int count = 0;
        
        // Count survey title
        if (survey.getTitle() != null) count++;
        
        // Count introduction block
        if (survey.getIntroductionBlock() != null) {
            if (survey.getIntroductionBlock().getTitle() != null) count++;
            if (survey.getIntroductionBlock().getDescription() != null) count++;
            if (survey.getIntroductionBlock().getWelcomeMessage() != null) count++;
            if (survey.getIntroductionBlock().getInstructions() != null) {
                count += survey.getIntroductionBlock().getInstructions().size();
            }
        }
        
        // Count content block
        if (survey.getContentBlock() != null && survey.getContentBlock().getSections() != null) {
            for (Survey.Section section : survey.getContentBlock().getSections()) {
                if (section.getTitle() != null) count++;
                if (section.getDescription() != null) count++;
                
                if (section.getCategories() != null) {
                    for (Survey.Category category : section.getCategories()) {
                        if (category.getName() != null) count++;
                        if (category.getDescription() != null) count++;
                        
                        if (category.getQuestions() != null) {
                            for (Survey.Question question : category.getQuestions()) {
                                if (question.getQuestionText() != null) count++;
                                if (question.getDescription() != null) count++;
                                
                                if (question.getChoices() != null) {
                                    for (Survey.Choice choice : question.getChoices()) {
                                        if (choice.getText() != null) count++;
                                    }
                                }
                                
                                if (question.getValidationRules() != null && 
                                    question.getValidationRules().getErrorMessage() != null) {
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Count footer block
        if (survey.getFooterBlock() != null) {
            if (survey.getFooterBlock().getThankYouMessage() != null) count++;
            if (survey.getFooterBlock().getSubmitButtonText() != null) count++;
            if (survey.getFooterBlock().getContactInformation() != null) count++;
            if (survey.getFooterBlock().getAdditionalInstructions() != null) {
                count += survey.getFooterBlock().getAdditionalInstructions().size();
            }
        }
        
        return count;
    }
}