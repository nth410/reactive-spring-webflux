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
        
        log.info("Initializing OpenAI Chat Model with model: {}", modelName);
        
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
    }
    
    /**
     * Mock implementation for development/testing when OpenAI API key is not available
     */
    private static class MockChatLanguageModel implements ChatLanguageModel {
        
        @Override
        public String generate(String message) {
            log.info("Mock translation service called with message length: {}", message.length());
            
            // Return a mock translated survey JSON
            return "{\n" +
                   "  \"title\": \"Translated Survey Title\",\n" +
                   "  \"language\": \"es\",\n" +
                   "  \"introductionBlock\": {\n" +
                   "    \"title\": \"Título de Introducción\",\n" +
                   "    \"description\": \"Descripción traducida\",\n" +
                   "    \"welcomeMessage\": \"Mensaje de bienvenida traducido\"\n" +
                   "  },\n" +
                   "  \"contentBlock\": {\n" +
                   "    \"sections\": [\n" +
                   "      {\n" +
                   "        \"title\": \"Sección Traducida\",\n" +
                   "        \"description\": \"Descripción de la sección\",\n" +
                   "        \"categories\": [\n" +
                   "          {\n" +
                   "            \"name\": \"Categoría Traducida\",\n" +
                   "            \"questions\": [\n" +
                   "              {\n" +
                   "                \"questionText\": \"¿Pregunta traducida?\",\n" +
                   "                \"type\": \"SINGLE_CHOICE\",\n" +
                   "                \"choices\": [\n" +
                   "                  {\n" +
                   "                    \"text\": \"Opción 1 traducida\"\n" +
                   "                  },\n" +
                   "                  {\n" +
                   "                    \"text\": \"Opción 2 traducida\"\n" +
                   "                  }\n" +
                   "                ]\n" +
                   "              }\n" +
                   "            ]\n" +
                   "          }\n" +
                   "        ]\n" +
                   "      }\n" +
                   "    ]\n" +
                   "  },\n" +
                   "  \"footerBlock\": {\n" +
                   "    \"thankYouMessage\": \"Mensaje de agradecimiento\",\n" +
                   "    \"submitButtonText\": \"Enviar\"\n" +
                   "  }\n" +
                   "}";
        }
    }
}