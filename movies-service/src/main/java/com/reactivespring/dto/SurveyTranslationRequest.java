package com.reactivespring.dto;

import com.reactivespring.domain.Survey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyTranslationRequest {
    
    @Valid
    @NotNull(message = "Survey data is required")
    private Survey survey;
    
    @NotBlank(message = "Target language is required")
    private String targetLanguage;
    
    @NotBlank(message = "Source language is required")
    private String sourceLanguage;
    
    private TranslationOptions options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranslationOptions {
        private Boolean preserveFormatting;
        private Boolean translateChoiceValues;
        private Boolean translateValidationMessages;
        private String tone; // formal, casual, professional, etc.
        private String context; // additional context for better translation
    }
}