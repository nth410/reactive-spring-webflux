# Survey Translation Service - ChatRequest with ResponseFormat Implementation

## Overview

This document describes the implementation of ChatRequest with ResponseFormat in the Survey Translation Service using LangChain4j library. The implementation ensures that AI responses fit the structured SurveyTranslationResponse format.

## Key Components Implemented

### 1. ResponseFormatUtil Class
**Location**: `movies-service/src/main/java/com/reactivespring/util/ResponseFormatUtil.java`

**Purpose**: Utility class to create ResponseFormat objects for structured AI responses

**Key Features**:
- Creates JSON schema for SurveyTranslationResponse structure
- Supports generic ResponseFormat creation for any class type
- Defines comprehensive schema including Survey object structure
- Includes detailed field descriptions for better AI understanding

**Main Methods**:
```java
// Creates ResponseFormat specifically for SurveyTranslationResponse
public ResponseFormat createSurveyTranslationResponseFormat()

// Creates ResponseFormat for any class type (generic utility)
public ResponseFormat createResponseFormatForClass(Class<?> clazz)
```

### 2. Enhanced SurveyTranslationService
**Location**: `movies-service/src/main/java/com/reactivespring/service/SurveyTranslationService.java`

**Changes Made**:
- **Injected ResponseFormatUtil**: Added dependency injection for the new utility class
- **Replaced simple AI call**: Changed from `generate(String)` to `chat(ChatRequest)` with structured response format
- **Added structured response parsing**: Implements `parseStructuredResponse()` method to handle complete SurveyTranslationResponse objects
- **Fallback mechanism**: Includes `parseAsLegacyResponse()` for backward compatibility

**Key Implementation Details**:
```java
// Create ResponseFormat for structured output
ResponseFormat responseFormat = responseFormatUtil.createSurveyTranslationResponseFormat();

// Create ChatRequest with structured response format
ChatRequest chatRequest = ChatRequest.builder()
        .messages(List.of(UserMessage.from(message.text())))
        .responseFormat(responseFormat)
        .build();

// Call AI with structured output
ChatResponse chatResponse = chatLanguageModel.chat(chatRequest);
```

### 3. Updated LangChain4j Configuration
**Location**: `movies-service/src/main/java/com/reactivespring/config/LangChain4jConfig.java`

**Enhancements**:
- **Enabled JSON mode**: Added `responseFormat("json_object")` for structured outputs
- **Strict JSON schema**: Added `strictJsonSchema(true)` for supported models
- **Updated MockChatLanguageModel**: Enhanced mock to support ChatRequest and return structured responses

**Configuration Changes**:
```java
return OpenAiChatModel.builder()
        .apiKey(openAiApiKey)
        .modelName(modelName)
        .timeout(Duration.ofSeconds(timeoutSeconds))
        .maxTokens(maxTokens)
        .temperature(temperature)
        .responseFormat("json_object") // Enable JSON mode for structured outputs
        .strictJsonSchema(true) // Enable strict JSON schema validation
        .build();
```

### 4. Enhanced Message Converter
**Location**: `movies-service/src/main/java/com/reactivespring/converter/SurveyTranslationMessageConverter.java`

**Updates**:
- **Structured output instructions**: Updated system prompt to instruct AI to return complete SurveyTranslationResponse objects
- **Schema guidance**: Added detailed output format requirements with JSON structure example
- **Validation instructions**: Informed AI about strict JSON schema validation

## Benefits of the Implementation

### 1. Structured Output Guarantee
- **Schema Validation**: AI responses are validated against a strict JSON schema
- **Type Safety**: Responses are automatically parsed into strongly-typed objects
- **Error Reduction**: Eliminates parsing errors from malformed JSON responses

### 2. Enhanced Reliability
- **Consistent Format**: Every response follows the same structure
- **Fallback Mechanism**: Maintains backward compatibility with legacy response format
- **Better Error Handling**: Graceful degradation if structured parsing fails

### 3. Improved Performance
- **Reduced Post-processing**: No need for complex JSON cleaning and extraction
- **Better Token Usage**: AI generates only the required structure
- **Faster Parsing**: Direct object mapping instead of string manipulation

### 4. Better AI Guidance
- **Clear Instructions**: AI knows exactly what structure to return
- **Field Descriptions**: JSON schema includes descriptions for better understanding
- **Validation Feedback**: AI is informed about schema validation requirements

## Usage Example

```java
@Autowired
private SurveyTranslationService translationService;

// Create translation request
SurveyTranslationRequest request = SurveyTranslationRequest.builder()
    .survey(originalSurvey)
    .sourceLanguage("en")
    .targetLanguage("es")
    .options(SurveyTranslationRequest.TranslationOptions.builder()
        .tone("professional")
        .context("business survey")
        .build())
    .build();

// Call service - now returns structured response
Mono<SurveyTranslationResponse> response = translationService.translateSurvey(request);

// Response automatically includes:
// - translatedSurvey: Complete Survey object with all text translated
// - sourceLanguage: "en"
// - targetLanguage: "es"  
// - metadata: Processing information, confidence scores, etc.
```

## Response Schema Structure

The ResponseFormat ensures AI returns objects matching this structure:

```json
{
  "translatedSurvey": {
    "title": "Translated survey title",
    "language": "target_language",
    "description": "Translated description",
    "introductionBlock": { /* translated introduction */ },
    "contentBlock": { /* translated content with sections, categories, questions */ },
    "footerBlock": { /* translated footer */ }
  },
  "sourceLanguage": "source_language",
  "targetLanguage": "target_language",
  "metadata": {
    "translatedAt": "2024-01-01T12:00:00",
    "translationModel": "AI Translation Assistant",
    "totalTextBlocks": 10,
    "translatedBlocks": 10,
    "confidenceScore": 0.95,
    "processingTimeMs": 1000,
    "isComplete": true,
    "translationNotes": {
      "model": "AI Translation Assistant",
      "tone": "professional",
      "context": "business survey"
    }
  }
}
```

## Testing and Development

### Mock Implementation
- **Development Mode**: When no OpenAI API key is provided, mock implementation returns structured responses
- **Testing Support**: MockChatLanguageModel supports both `generate()` and `chat(ChatRequest)` methods
- **Realistic Responses**: Mock data follows the same schema as real AI responses

### Backward Compatibility
- **Legacy Support**: Service can still handle old-format responses (Survey objects only)
- **Graceful Fallback**: If structured parsing fails, attempts legacy parsing
- **Migration Path**: Smooth transition from old to new response format

## Dependencies Required

The implementation uses these LangChain4j dependencies (already in build.gradle):

```gradle
implementation 'dev.langchain4j:langchain4j:0.34.0'
implementation 'dev.langchain4j:langchain4j-open-ai:0.34.0'
implementation 'dev.langchain4j:langchain4j-spring-boot-starter:0.34.0'
```

## Conclusion

This implementation successfully integrates ChatRequest with ResponseFormat in the Survey Translation Service, providing:

1. **Structured AI Responses**: Guaranteed JSON schema compliance
2. **Type Safety**: Automatic parsing to SurveyTranslationResponse objects  
3. **Enhanced Reliability**: Better error handling and fallback mechanisms
4. **Improved Developer Experience**: Cleaner code and easier testing
5. **Future-Proof Design**: Easy to extend for additional response formats

The ResponseFormatUtil class provides a reusable pattern that can be applied to other AI services requiring structured outputs.