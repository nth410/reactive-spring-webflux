package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "surveys")
public class Survey {
    
    @Id
    private String id;
    
    @NotBlank(message = "Survey title is required")
    private String title;
    
    @NotBlank(message = "Language is required")
    private String language;
    
    @Valid
    @NotNull(message = "Introduction block is required")
    private IntroductionBlock introductionBlock;
    
    @Valid
    @NotNull(message = "Content block is required")
    private ContentBlock contentBlock;
    
    @Valid
    private FooterBlock footerBlock;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntroductionBlock {
        @NotBlank(message = "Introduction title is required")
        private String title;
        
        private String description;
        private String welcomeMessage;
        private List<String> instructions;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentBlock {
        @Valid
        @NotNull(message = "At least one section is required")
        private List<Section> sections;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Section {
        @NotBlank(message = "Section title is required")
        private String title;
        
        private String description;
        private Integer order;
        
        @Valid
        private List<Category> categories;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Category {
        @NotBlank(message = "Category name is required")
        private String name;
        
        private String description;
        private Integer order;
        
        @Valid
        @NotNull(message = "At least one question is required")
        private List<Question> questions;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Question {
        @NotBlank(message = "Question text is required")
        private String questionText;
        
        @NotNull(message = "Question type is required")
        private QuestionType type;
        
        private String description;
        private Integer order;
        private Boolean required;
        
        @Valid
        private List<Choice> choices;
        
        private ValidationRules validationRules;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Choice {
        @NotBlank(message = "Choice text is required")
        private String text;
        
        private String value;
        private Integer order;
        private Boolean isDefault;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FooterBlock {
        private String thankYouMessage;
        private String submitButtonText;
        private String contactInformation;
        private List<String> additionalInstructions;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationRules {
        private Integer minLength;
        private Integer maxLength;
        private Integer minValue;
        private Integer maxValue;
        private String pattern;
        private String errorMessage;
    }
    
    public enum QuestionType {
        SINGLE_CHOICE,
        MULTIPLE_CHOICE,
        TEXT,
        NUMBER,
        EMAIL,
        DATE,
        RATING,
        BOOLEAN
    }
}