# Survey AI Translation Service

A comprehensive survey translation service built with Java Spring WebFlux, MongoDB, and LangChain4j for AI-powered translation using OpenAI.

## Features

- **Complete Survey Translation**: Translates all blocks of a survey including:
  - Introduction block (title, description, welcome message, instructions)
  - Content blocks with sections, categories, questions, and choices
  - Footer block (thank you message, button text, contact info)
- **Custom Message Support**: Uses CustomMessage implementation of ChatMessage from LangChain4j
- **POJO Input/Output**: Accepts and returns structured POJOs
- **Reactive Programming**: Built with Spring WebFlux for non-blocking operations
- **MongoDB Integration**: Stores survey data in MongoDB with reactive support
- **Comprehensive Error Handling**: Global exception handler with detailed error responses
- **Translation Options**: Configurable translation settings (tone, context, formatting)

## Project Structure

```
src/main/java/com/reactivespring/
├── domain/
│   └── Survey.java                 # Survey domain model with all blocks
├── dto/
│   ├── CustomMessage.java          # Implementation of ChatMessage
│   ├── SurveyTranslationRequest.java
│   └── SurveyTranslationResponse.java
├── converter/
│   └── SurveyTranslationMessageConverter.java  # Converts request to message
├── service/
│   └── SurveyTranslationService.java           # Main translation service
├── controller/
│   └── SurveyTranslationController.java        # REST API endpoints
├── repository/
│   └── SurveyRepository.java                   # MongoDB reactive repository
├── config/
│   └── LangChain4jConfig.java                  # LangChain4j configuration
└── exception/
    ├── TranslationException.java
    └── GlobalExceptionHandler.java
```

## Setup and Configuration

### 1. Dependencies

The following dependencies are already configured in `build.gradle`:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
    implementation 'dev.langchain4j:langchain4j:0.34.0'
    implementation 'dev.langchain4j:langchain4j-open-ai:0.34.0'
    implementation 'dev.langchain4j:langchain4j-spring-boot-starter:0.34.0'
    // ... other dependencies
}
```

### 2. Environment Configuration

Set up your environment variables:

```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

Or configure in `application.yml`:

```yaml
openai:
  api:
    key: your-openai-api-key-here
  model:
    name: gpt-3.5-turbo
  timeout:
    seconds: 120
  max:
    tokens: 4000
  temperature: 0.3

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: surveydb
```

### 3. Start MongoDB

```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

## API Endpoints

### 1. Translate Survey (Synchronous)

**POST** `/api/v1/surveys/translate`

**Request Body Example:**

```json
{
  "survey": {
    "title": "Customer Satisfaction Survey",
    "language": "en",
    "introductionBlock": {
      "title": "Welcome to Our Survey",
      "description": "We value your feedback",
      "welcomeMessage": "Thank you for participating in our survey",
      "instructions": [
        "Please answer all questions honestly",
        "This survey will take approximately 5 minutes"
      ]
    },
    "contentBlock": {
      "sections": [
        {
          "title": "Service Quality",
          "description": "Questions about our service quality",
          "order": 1,
          "categories": [
            {
              "name": "Overall Satisfaction",
              "description": "General satisfaction questions",
              "order": 1,
              "questions": [
                {
                  "questionText": "How satisfied are you with our service?",
                  "type": "SINGLE_CHOICE",
                  "description": "Please rate your overall satisfaction",
                  "order": 1,
                  "required": true,
                  "choices": [
                    {
                      "text": "Very Satisfied",
                      "value": "5",
                      "order": 1
                    },
                    {
                      "text": "Satisfied",
                      "value": "4",
                      "order": 2
                    },
                    {
                      "text": "Neutral",
                      "value": "3",
                      "order": 3
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    "footerBlock": {
      "thankYouMessage": "Thank you for your valuable feedback!",
      "submitButtonText": "Submit Survey",
      "contactInformation": "For questions, contact support@example.com"
    }
  },
  "sourceLanguage": "en",
  "targetLanguage": "es",
  "options": {
    "preserveFormatting": true,
    "translateChoiceValues": false,
    "translateValidationMessages": true,
    "tone": "professional",
    "context": "Customer satisfaction survey for a technology company"
  }
}
```

**Response Example:**

```json
{
  "translatedSurvey": {
    "title": "Encuesta de Satisfacción del Cliente",
    "language": "es",
    "introductionBlock": {
      "title": "Bienvenido a Nuestra Encuesta",
      "description": "Valoramos sus comentarios",
      "welcomeMessage": "Gracias por participar en nuestra encuesta",
      "instructions": [
        "Por favor responda todas las preguntas honestamente",
        "Esta encuesta tomará aproximadamente 5 minutos"
      ]
    },
    "contentBlock": {
      "sections": [
        {
          "title": "Calidad del Servicio",
          "description": "Preguntas sobre la calidad de nuestro servicio",
          "order": 1,
          "categories": [
            {
              "name": "Satisfacción General",
              "description": "Preguntas de satisfacción general",
              "order": 1,
              "questions": [
                {
                  "questionText": "¿Qué tan satisfecho está con nuestro servicio?",
                  "type": "SINGLE_CHOICE",
                  "description": "Por favor califique su satisfacción general",
                  "order": 1,
                  "required": true,
                  "choices": [
                    {
                      "text": "Muy Satisfecho",
                      "value": "5",
                      "order": 1
                    },
                    {
                      "text": "Satisfecho",
                      "value": "4",
                      "order": 2
                    },
                    {
                      "text": "Neutral",
                      "value": "3",
                      "order": 3
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    "footerBlock": {
      "thankYouMessage": "¡Gracias por sus valiosos comentarios!",
      "submitButtonText": "Enviar Encuesta",
      "contactInformation": "Para preguntas, contacte a support@example.com"
    }
  },
  "sourceLanguage": "en",
  "targetLanguage": "es",
  "metadata": {
    "translatedAt": "2024-01-15T10:30:00",
    "translationModel": "OpenAI GPT",
    "totalTextBlocks": 15,
    "translatedBlocks": 15,
    "confidenceScore": 0.95,
    "processingTimeMs": 5000,
    "isComplete": true,
    "translationNotes": {
      "model": "OpenAI GPT",
      "tone": "professional",
      "context": "Customer satisfaction survey for a technology company"
    }
  }
}
```

### 2. Translate Survey (Asynchronous)

**POST** `/api/v1/surveys/translate/async`

**Response:**

```json
{
  "jobId": "job_1642234567890_123",
  "status": "PROCESSING",
  "estimatedCompletionTimeMs": 30000
}
```

### 3. Get Supported Languages

**GET** `/api/v1/surveys/translate/languages`

**Response:**

```json
{
  "supportedLanguages": [
    {
      "code": "en",
      "name": "English"
    },
    {
      "code": "es",
      "name": "Spanish"
    },
    {
      "code": "fr",
      "name": "French"
    }
  ]
}
```

## Usage Examples

### Java Client Example

```java
@Service
public class SurveyTranslationClient {
    
    private final WebClient webClient;
    
    public SurveyTranslationClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/api/v1/surveys")
                .build();
    }
    
    public Mono<SurveyTranslationResponse> translateSurvey(Survey survey, String targetLanguage) {
        SurveyTranslationRequest request = SurveyTranslationRequest.builder()
                .survey(survey)
                .sourceLanguage(survey.getLanguage())
                .targetLanguage(targetLanguage)
                .options(SurveyTranslationRequest.TranslationOptions.builder()
                        .preserveFormatting(true)
                        .translateChoiceValues(false)
                        .tone("professional")
                        .build())
                .build();
        
        return webClient.post()
                .uri("/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SurveyTranslationResponse.class);
    }
}
```

### CURL Example

```bash
curl -X POST http://localhost:8080/api/v1/surveys/translate \
  -H "Content-Type: application/json" \
  -d '{
    "survey": {
      "title": "Customer Survey",
      "language": "en",
      "introductionBlock": {
        "title": "Welcome",
        "description": "Please participate"
      },
      "contentBlock": {
        "sections": [{
          "title": "Questions",
          "categories": [{
            "name": "General",
            "questions": [{
              "questionText": "How are you?",
              "type": "TEXT"
            }]
          }]
        }]
      }
    },
    "sourceLanguage": "en",
    "targetLanguage": "es"
  }'
```

## Key Components

### CustomMessage Implementation

The `CustomMessage` class implements LangChain4j's `ChatMessage` interface:

```java
@Data
@Builder
public class CustomMessage implements ChatMessage {
    private String content;
    private ChatMessageType type;
    private String role;
    
    @Override
    public String text() {
        return content;
    }
    
    @Override
    public ChatMessageType type() {
        return type != null ? type : ChatMessageType.USER;
    }
    
    public static CustomMessage userMessage(String content) {
        return CustomMessage.builder()
                .content(content)
                .type(ChatMessageType.USER)
                .role("user")
                .build();
    }
}
```

### MessageConverter

The `SurveyTranslationMessageConverter` converts requests to `CustomMessage`:

```java
@Component
public class SurveyTranslationMessageConverter {
    
    public CustomMessage convertToMessage(SurveyTranslationRequest request) {
        String systemPrompt = buildSystemPrompt(request);
        String userContent = buildUserContent(request);
        return CustomMessage.userMessage(systemPrompt + "\n\n" + userContent);
    }
    
    private String buildSystemPrompt(SurveyTranslationRequest request) {
        // Builds detailed translation instructions for OpenAI
        // Includes guidelines for preserving structure and translating content
    }
}
```

## Best Practices

1. **Error Handling**: Always handle translation failures gracefully
2. **Validation**: Validate survey structure before translation
3. **Caching**: Consider caching translated surveys to avoid redundant API calls
4. **Monitoring**: Monitor OpenAI API usage and costs
5. **Testing**: Use the mock implementation for development and testing

## Testing

Run the tests:

```bash
./gradlew test
```

The test suite includes:
- Unit tests for the translation controller
- Integration tests with mock OpenAI responses
- Validation tests for request/response DTOs

## Development Notes

- The service includes a mock OpenAI implementation for development without API keys
- All survey components (blocks, sections, categories, questions, choices) are translated
- The original survey structure and field names are preserved
- Translation metadata is included for monitoring and debugging
- Reactive programming ensures non-blocking operations

## Troubleshooting

1. **OpenAI API Key Issues**: Ensure your API key is properly set as an environment variable
2. **MongoDB Connection**: Verify MongoDB is running on the configured port
3. **Memory Issues**: Increase JVM heap size for large surveys
4. **Timeout Issues**: Adjust the OpenAI timeout configuration for complex surveys

## Future Enhancements

- Support for batch translation of multiple surveys
- Translation quality scoring and validation
- Integration with other translation services (Google Translate, Azure Translator)
- Real-time translation progress updates via WebSocket
- Translation history and audit logging