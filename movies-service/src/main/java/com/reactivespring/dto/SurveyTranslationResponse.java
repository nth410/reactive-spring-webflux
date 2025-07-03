package com.reactivespring.dto;

import com.reactivespring.domain.Survey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyTranslationResponse {
    
    private Survey translatedSurvey;
    private String sourceLanguage;
    private String targetLanguage;
    private TranslationMetadata metadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranslationMetadata {
        private LocalDateTime translatedAt;
        private String translationModel;
        private Integer totalTextBlocks;
        private Integer translatedBlocks;
        private Double confidenceScore;
        private Long processingTimeMs;
        private Map<String, String> translationNotes;
        private Boolean isComplete;
    }
}