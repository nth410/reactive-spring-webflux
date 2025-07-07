package com.reactivespring.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for creating ResponseFormat objects for structured AI responses
 */
@Slf4j
@Component
public class ResponseFormatUtil {

    private final ObjectMapper objectMapper;

    public ResponseFormatUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a ResponseFormat for SurveyTranslationResponse to ensure AI returns structured JSON
     * 
     * @return ResponseFormat configured for SurveyTranslationResponse structure
     */
    public ResponseFormat createSurveyTranslationResponseFormat() {
        return ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(createSurveyTranslationJsonSchema())
                .build();
    }

    /**
     * Creates a generic ResponseFormat for any class type
     * 
     * @param clazz The class to create schema for
     * @return ResponseFormat configured for the specified class
     */
    public ResponseFormat createResponseFormatForClass(Class<?> clazz) {
        return ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(createJsonSchemaForClass(clazz))
                .build();
    }

    /**
     * Creates JSON schema specifically for SurveyTranslationResponse
     */
    private JsonSchema createSurveyTranslationJsonSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.put("additionalProperties", false);

        // Define required properties
        ObjectNode properties = objectMapper.createObjectNode();
        
        // translatedSurvey property - Survey object
        ObjectNode translatedSurveyProperty = createSurveySchema();
        properties.set("translatedSurvey", translatedSurveyProperty);
        
        // sourceLanguage property
        ObjectNode sourceLanguageProperty = objectMapper.createObjectNode();
        sourceLanguageProperty.put("type", "string");
        sourceLanguageProperty.put("description", "The source language of the survey");
        properties.set("sourceLanguage", sourceLanguageProperty);
        
        // targetLanguage property  
        ObjectNode targetLanguageProperty = objectMapper.createObjectNode();
        targetLanguageProperty.put("type", "string");
        targetLanguageProperty.put("description", "The target language for translation");
        properties.set("targetLanguage", targetLanguageProperty);
        
        // metadata property
        ObjectNode metadataProperty = createTranslationMetadataSchema();
        properties.set("metadata", metadataProperty);

        schema.set("properties", properties);
        
        // Set required fields
        schema.putArray("required")
                .add("translatedSurvey")
                .add("sourceLanguage")
                .add("targetLanguage")
                .add("metadata");

        return JsonSchema.builder()
                .name("SurveyTranslationResponse")
                .description("Response containing the translated survey and metadata")
                .schema(schema)
                .build();
    }

    /**
     * Creates Survey schema for JSON structure
     */
    private ObjectNode createSurveySchema() {
        ObjectNode surveySchema = objectMapper.createObjectNode();
        surveySchema.put("type", "object");
        surveySchema.put("description", "The translated survey object");
        
        ObjectNode surveyProperties = objectMapper.createObjectNode();
        
        // Basic survey properties
        surveyProperties.set("title", createStringProperty("Survey title"));
        surveyProperties.set("language", createStringProperty("Survey language"));
        surveyProperties.set("description", createStringProperty("Survey description"));
        
        // Introduction block
        surveyProperties.set("introductionBlock", createIntroductionBlockSchema());
        
        // Content block
        surveyProperties.set("contentBlock", createContentBlockSchema());
        
        // Footer block
        surveyProperties.set("footerBlock", createFooterBlockSchema());
        
        surveySchema.set("properties", surveyProperties);
        
        return surveySchema;
    }

    /**
     * Creates TranslationMetadata schema
     */
    private ObjectNode createTranslationMetadataSchema() {
        ObjectNode metadataSchema = objectMapper.createObjectNode();
        metadataSchema.put("type", "object");
        metadataSchema.put("description", "Metadata about the translation process");
        
        ObjectNode metadataProperties = objectMapper.createObjectNode();
        metadataProperties.set("translatedAt", createStringProperty("Timestamp of translation"));
        metadataProperties.set("translationModel", createStringProperty("AI model used for translation"));
        metadataProperties.set("totalTextBlocks", createNumberProperty("Total number of text blocks"));
        metadataProperties.set("translatedBlocks", createNumberProperty("Number of translated blocks"));
        metadataProperties.set("confidenceScore", createNumberProperty("Confidence score of translation"));
        metadataProperties.set("processingTimeMs", createNumberProperty("Processing time in milliseconds"));
        metadataProperties.set("isComplete", createBooleanProperty("Whether translation is complete"));
        
        // Translation notes as object
        ObjectNode translationNotesProperty = objectMapper.createObjectNode();
        translationNotesProperty.put("type", "object");
        translationNotesProperty.put("description", "Additional notes about the translation");
        translationNotesProperty.put("additionalProperties", true);
        metadataProperties.set("translationNotes", translationNotesProperty);
        
        metadataSchema.set("properties", metadataProperties);
        
        return metadataSchema;
    }

    /**
     * Creates introduction block schema
     */
    private ObjectNode createIntroductionBlockSchema() {
        ObjectNode introSchema = objectMapper.createObjectNode();
        introSchema.put("type", "object");
        
        ObjectNode introProperties = objectMapper.createObjectNode();
        introProperties.set("title", createStringProperty("Introduction title"));
        introProperties.set("description", createStringProperty("Introduction description"));
        introProperties.set("welcomeMessage", createStringProperty("Welcome message"));
        introProperties.set("instructions", createStringArrayProperty("List of instructions"));
        
        introSchema.set("properties", introProperties);
        return introSchema;
    }

    /**
     * Creates content block schema
     */
    private ObjectNode createContentBlockSchema() {
        ObjectNode contentSchema = objectMapper.createObjectNode();
        contentSchema.put("type", "object");
        
        ObjectNode contentProperties = objectMapper.createObjectNode();
        
        // Sections array
        ObjectNode sectionsProperty = objectMapper.createObjectNode();
        sectionsProperty.put("type", "array");
        sectionsProperty.set("items", createSectionSchema());
        contentProperties.set("sections", sectionsProperty);
        
        contentSchema.set("properties", contentProperties);
        return contentSchema;
    }

    /**
     * Creates section schema
     */
    private ObjectNode createSectionSchema() {
        ObjectNode sectionSchema = objectMapper.createObjectNode();
        sectionSchema.put("type", "object");
        
        ObjectNode sectionProperties = objectMapper.createObjectNode();
        sectionProperties.set("title", createStringProperty("Section title"));
        sectionProperties.set("description", createStringProperty("Section description"));
        
        // Categories array
        ObjectNode categoriesProperty = objectMapper.createObjectNode();
        categoriesProperty.put("type", "array");
        categoriesProperty.set("items", createCategorySchema());
        sectionProperties.set("categories", categoriesProperty);
        
        sectionSchema.set("properties", sectionProperties);
        return sectionSchema;
    }

    /**
     * Creates category schema
     */
    private ObjectNode createCategorySchema() {
        ObjectNode categorySchema = objectMapper.createObjectNode();
        categorySchema.put("type", "object");
        
        ObjectNode categoryProperties = objectMapper.createObjectNode();
        categoryProperties.set("name", createStringProperty("Category name"));
        categoryProperties.set("description", createStringProperty("Category description"));
        
        // Questions array
        ObjectNode questionsProperty = objectMapper.createObjectNode();
        questionsProperty.put("type", "array");
        questionsProperty.set("items", createQuestionSchema());
        categoryProperties.set("questions", questionsProperty);
        
        categorySchema.set("properties", categoryProperties);
        return categorySchema;
    }

    /**
     * Creates question schema
     */
    private ObjectNode createQuestionSchema() {
        ObjectNode questionSchema = objectMapper.createObjectNode();
        questionSchema.put("type", "object");
        
        ObjectNode questionProperties = objectMapper.createObjectNode();
        questionProperties.set("questionText", createStringProperty("Question text"));
        questionProperties.set("description", createStringProperty("Question description"));
        questionProperties.set("type", createStringProperty("Question type"));
        
        // Choices array
        ObjectNode choicesProperty = objectMapper.createObjectNode();
        choicesProperty.put("type", "array");
        choicesProperty.set("items", createChoiceSchema());
        questionProperties.set("choices", choicesProperty);
        
        questionSchema.set("properties", questionProperties);
        return questionSchema;
    }

    /**
     * Creates choice schema
     */
    private ObjectNode createChoiceSchema() {
        ObjectNode choiceSchema = objectMapper.createObjectNode();
        choiceSchema.put("type", "object");
        
        ObjectNode choiceProperties = objectMapper.createObjectNode();
        choiceProperties.set("text", createStringProperty("Choice text"));
        choiceProperties.set("value", createStringProperty("Choice value"));
        
        choiceSchema.set("properties", choiceProperties);
        return choiceSchema;
    }

    /**
     * Creates footer block schema
     */
    private ObjectNode createFooterBlockSchema() {
        ObjectNode footerSchema = objectMapper.createObjectNode();
        footerSchema.put("type", "object");
        
        ObjectNode footerProperties = objectMapper.createObjectNode();
        footerProperties.set("thankYouMessage", createStringProperty("Thank you message"));
        footerProperties.set("submitButtonText", createStringProperty("Submit button text"));
        footerProperties.set("contactInformation", createStringProperty("Contact information"));
        footerProperties.set("additionalInstructions", createStringArrayProperty("Additional instructions"));
        
        footerSchema.set("properties", footerProperties);
        return footerSchema;
    }

    /**
     * Helper method to create a generic JSON schema for any class
     */
    private JsonSchema createJsonSchemaForClass(Class<?> clazz) {
        // This is a simplified implementation
        // In production, you might want to use a library like json-schema-generator
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.put("description", "Schema for " + clazz.getSimpleName());
        
        return JsonSchema.builder()
                .name(clazz.getSimpleName())
                .description("JSON schema for " + clazz.getSimpleName())
                .schema(schema)
                .build();
    }

    // Helper methods for common property types
    private ObjectNode createStringProperty(String description) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "string");
        property.put("description", description);
        return property;
    }

    private ObjectNode createNumberProperty(String description) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "number");
        property.put("description", description);
        return property;
    }

    private ObjectNode createBooleanProperty(String description) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "boolean");
        property.put("description", description);
        return property;
    }

    private ObjectNode createStringArrayProperty(String description) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "array");
        property.put("description", description);
        
        ObjectNode items = objectMapper.createObjectNode();
        items.put("type", "string");
        property.set("items", items);
        
        return property;
    }
}