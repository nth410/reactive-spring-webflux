package com.reactivespring.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class LangChain4jConfig {
    
    @Value("${openai.api.key:#{null}}")
    private String openAiApiKey;
    
    @Value("${openai.model.name:gpt-3.5-turbo}")
    private String modelName;
    
    @Value("${openai.timeout.seconds:120}")
    private int timeoutSeconds;
    
    @Value("${openai.max.tokens:4000}")
    private int maxTokens;
    
    @Value("${openai.temperature:0.3}")
    private double temperature;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            log.warn("OpenAI API key is not configured. Using mock implementation.");
            return new MockChatLanguageModel();
        }
        
        log.info("Initializing OpenAI Chat Model with model: {} and JSON schema support", modelName);
        
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .maxTokens(maxTokens)
                .temperature(temperature)
                .responseFormat("json_object") // Enable JSON mode for structured outputs
                .strictJsonSchema(true) // Enable strict JSON schema validation for supported models
                .build();
    }
    
    /**
     * Mock implementation for development/testing when OpenAI API key is not available
     */
    private static class MockChatLanguageModel implements ChatLanguageModel {
        
        @Override
        public String generate(String message) {
            log.info("Mock translation service called with message length: {}", message.length());
            return getMockStructuredResponse();
        }

        @Override
        public dev.langchain4j.model.chat.response.ChatResponse chat(dev.langchain4j.model.chat.request.ChatRequest request) {
            log.info("Mock chat service called with ChatRequest, messages count: {}", request.messages().size());
            
            String mockResponse = getMockStructuredResponse();
            
            // Create a mock ChatResponse
            dev.langchain4j.data.message.AiMessage aiMessage = dev.langchain4j.data.message.AiMessage.from(mockResponse);
            
            return dev.langchain4j.model.chat.response.ChatResponse.builder()
                    .aiMessage(aiMessage)
                    .tokenUsage(dev.langchain4j.model.output.TokenUsage.builder()
                            .inputTokenCount(100)
                            .outputTokenCount(200)
                            .totalTokenCount(300)
                            .build())
                    .finishReason(dev.langchain4j.model.output.FinishReason.STOP)
                    .build();
        }
        
        private String getMockStructuredResponse() {
            // Return a mock SurveyTranslationResponse JSON that matches our ResponseFormat schema
            return "{\n" +
                   "  \"translatedSurvey\": {\n" +
                   "    \"title\": \"Translated Survey Title\",\n" +
                   "    \"language\": \"es\",\n" +
                   "    \"description\": \"Descripción traducida de la encuesta\",\n" +
                   "    \"introductionBlock\": {\n" +
                   "      \"title\": \"Título de Introducción\",\n" +
                   "      \"description\": \"Descripción traducida\",\n" +
                   "      \"welcomeMessage\": \"Mensaje de bienvenida traducido\",\n" +
                   "      \"instructions\": [\"Instrucción 1\", \"Instrucción 2\"]\n" +
                   "    },\n" +
                   "    \"contentBlock\": {\n" +
                   "      \"sections\": [\n" +
                   "        {\n" +
                   "          \"title\": \"Sección Traducida\",\n" +
                   "          \"description\": \"Descripción de la sección\",\n" +
                   "          \"categories\": [\n" +
                   "            {\n" +
                   "              \"name\": \"Categoría Traducida\",\n" +
                   "              \"description\": \"Descripción de categoría\",\n" +
                   "              \"questions\": [\n" +
                   "                {\n" +
                   "                  \"questionText\": \"¿Pregunta traducida?\",\n" +
                   "                  \"description\": \"Descripción de pregunta\",\n" +
                   "                  \"type\": \"SINGLE_CHOICE\",\n" +
                   "                  \"choices\": [\n" +
                   "                    {\n" +
                   "                      \"text\": \"Opción 1 traducida\",\n" +
                   "                      \"value\": \"option1\"\n" +
                   "                    },\n" +
                   "                    {\n" +
                   "                      \"text\": \"Opción 2 traducida\",\n" +
                   "                      \"value\": \"option2\"\n" +
                   "                    }\n" +
                   "                  ]\n" +
                   "                }\n" +
                   "              ]\n" +
                   "            }\n" +
                   "          ]\n" +
                   "        }\n" +
                   "      ]\n" +
                   "    },\n" +
                   "    \"footerBlock\": {\n" +
                   "      \"thankYouMessage\": \"Mensaje de agradecimiento\",\n" +
                   "      \"submitButtonText\": \"Enviar\",\n" +
                   "      \"contactInformation\": \"Información de contacto\",\n" +
                   "      \"additionalInstructions\": [\"Instrucción adicional 1\"]\n" +
                   "    }\n" +
                   "  },\n" +
                   "  \"sourceLanguage\": \"en\",\n" +
                   "  \"targetLanguage\": \"es\",\n" +
                   "  \"metadata\": {\n" +
                   "    \"translatedAt\": \"2024-01-01T12:00:00\",\n" +
                   "    \"translationModel\": \"Mock GPT\",\n" +
                   "    \"totalTextBlocks\": 10,\n" +
                   "    \"translatedBlocks\": 10,\n" +
                   "    \"confidenceScore\": 0.95,\n" +
                   "    \"processingTimeMs\": 1000,\n" +
                   "    \"isComplete\": true,\n" +
                   "    \"translationNotes\": {\n" +
                   "      \"model\": \"Mock GPT\",\n" +
                   "      \"tone\": \"professional\"\n" +
                   "    }\n" +
                   "  }\n" +
                   "}";
        }
    }
}