package com.reactivespring.controller;

import com.reactivespring.domain.Survey;
import com.reactivespring.dto.SurveyTranslationRequest;
import com.reactivespring.dto.SurveyTranslationResponse;
import com.reactivespring.service.SurveyTranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(SurveyTranslationController.class)
public class SurveyTranslationControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private SurveyTranslationService translationService;
    
    @Test
    public void testTranslateSurvey_Success() {
        // Given - Create a sample survey
        Survey sampleSurvey = createSampleSurvey();
        
        SurveyTranslationRequest request = SurveyTranslationRequest.builder()
                .survey(sampleSurvey)
                .sourceLanguage("en")
                .targetLanguage("es")
                .options(SurveyTranslationRequest.TranslationOptions.builder()
                        .preserveFormatting(true)
                        .translateChoiceValues(true)
                        .tone("professional")
                        .build())
                .build();
        
        // Create expected response
        Survey translatedSurvey = createTranslatedSurvey();
        SurveyTranslationResponse expectedResponse = SurveyTranslationResponse.builder()
                .translatedSurvey(translatedSurvey)
                .sourceLanguage("en")
                .targetLanguage("es")
                .metadata(SurveyTranslationResponse.TranslationMetadata.builder()
                        .translatedAt(LocalDateTime.now())
                        .translationModel("OpenAI GPT")
                        .totalTextBlocks(10)
                        .translatedBlocks(10)
                        .confidenceScore(0.95)
                        .processingTimeMs(5000L)
                        .isComplete(true)
                        .build())
                .build();
        
        // Mock the service call
        when(translationService.translateSurvey(any(SurveyTranslationRequest.class)))
                .thenReturn(Mono.just(expectedResponse));
        
        // When & Then
        webTestClient.post()
                .uri("/api/v1/surveys/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(SurveyTranslationResponse.class)
                .value(response -> {
                    assert response.getTargetLanguage().equals("es");
                    assert response.getSourceLanguage().equals("en");
                    assert response.getTranslatedSurvey() != null;
                    assert response.getMetadata().getIsComplete();
                });
    }
    
    @Test
    public void testGetSupportedLanguages_Success() {
        webTestClient.get()
                .uri("/api/v1/surveys/translate/languages")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.supportedLanguages").isArray()
                .jsonPath("$.supportedLanguages[0].code").isEqualTo("en")
                .jsonPath("$.supportedLanguages[0].name").isEqualTo("English");
    }
    
    @Test
    public void testTranslateSurveyAsync_Success() {
        // Given
        Survey sampleSurvey = createSampleSurvey();
        SurveyTranslationRequest request = SurveyTranslationRequest.builder()
                .survey(sampleSurvey)
                .sourceLanguage("en")
                .targetLanguage("fr")
                .build();
        
        // Mock the service to return a successful response (for async it starts processing)
        when(translationService.translateSurvey(any(SurveyTranslationRequest.class)))
                .thenReturn(Mono.just(SurveyTranslationResponse.builder().build()));
        
        // When & Then
        webTestClient.post()
                .uri("/api/v1/surveys/translate/async")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.jobId").exists()
                .jsonPath("$.status").isEqualTo("PROCESSING")
                .jsonPath("$.estimatedCompletionTimeMs").isEqualTo(30000);
    }
    
    private Survey createSampleSurvey() {
        return Survey.builder()
                .title("Customer Satisfaction Survey")
                .language("en")
                .createdAt(LocalDateTime.now())
                .introductionBlock(Survey.IntroductionBlock.builder()
                        .title("Welcome to Our Survey")
                        .description("We value your feedback")
                        .welcomeMessage("Thank you for participating in our survey")
                        .instructions(Arrays.asList(
                                "Please answer all questions honestly",
                                "This survey will take approximately 5 minutes"
                        ))
                        .build())
                .contentBlock(Survey.ContentBlock.builder()
                        .sections(Arrays.asList(
                                Survey.Section.builder()
                                        .title("Service Quality")
                                        .description("Questions about our service quality")
                                        .order(1)
                                        .categories(Arrays.asList(
                                                Survey.Category.builder()
                                                        .name("Overall Satisfaction")
                                                        .description("General satisfaction questions")
                                                        .order(1)
                                                        .questions(Arrays.asList(
                                                                Survey.Question.builder()
                                                                        .questionText("How satisfied are you with our service?")
                                                                        .type(Survey.QuestionType.SINGLE_CHOICE)
                                                                        .description("Please rate your overall satisfaction")
                                                                        .order(1)
                                                                        .required(true)
                                                                        .choices(Arrays.asList(
                                                                                Survey.Choice.builder()
                                                                                        .text("Very Satisfied")
                                                                                        .value("5")
                                                                                        .order(1)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Satisfied")
                                                                                        .value("4")
                                                                                        .order(2)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Neutral")
                                                                                        .value("3")
                                                                                        .order(3)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Dissatisfied")
                                                                                        .value("2")
                                                                                        .order(4)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Very Dissatisfied")
                                                                                        .value("1")
                                                                                        .order(5)
                                                                                        .build()
                                                                        ))
                                                                        .build()
                                                        ))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .footerBlock(Survey.FooterBlock.builder()
                        .thankYouMessage("Thank you for your valuable feedback!")
                        .submitButtonText("Submit Survey")
                        .contactInformation("For questions, contact support@example.com")
                        .additionalInstructions(Collections.singletonList(
                                "Your responses are confidential and will be used to improve our services"
                        ))
                        .build())
                .build();
    }
    
    private Survey createTranslatedSurvey() {
        return Survey.builder()
                .title("Encuesta de Satisfacción del Cliente")
                .language("es")
                .createdAt(LocalDateTime.now())
                .introductionBlock(Survey.IntroductionBlock.builder()
                        .title("Bienvenido a Nuestra Encuesta")
                        .description("Valoramos sus comentarios")
                        .welcomeMessage("Gracias por participar en nuestra encuesta")
                        .instructions(Arrays.asList(
                                "Por favor responda todas las preguntas honestamente",
                                "Esta encuesta tomará aproximadamente 5 minutos"
                        ))
                        .build())
                .contentBlock(Survey.ContentBlock.builder()
                        .sections(Arrays.asList(
                                Survey.Section.builder()
                                        .title("Calidad del Servicio")
                                        .description("Preguntas sobre la calidad de nuestro servicio")
                                        .order(1)
                                        .categories(Arrays.asList(
                                                Survey.Category.builder()
                                                        .name("Satisfacción General")
                                                        .description("Preguntas de satisfacción general")
                                                        .order(1)
                                                        .questions(Arrays.asList(
                                                                Survey.Question.builder()
                                                                        .questionText("¿Qué tan satisfecho está con nuestro servicio?")
                                                                        .type(Survey.QuestionType.SINGLE_CHOICE)
                                                                        .description("Por favor califique su satisfacción general")
                                                                        .order(1)
                                                                        .required(true)
                                                                        .choices(Arrays.asList(
                                                                                Survey.Choice.builder()
                                                                                        .text("Muy Satisfecho")
                                                                                        .value("5")
                                                                                        .order(1)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Satisfecho")
                                                                                        .value("4")
                                                                                        .order(2)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Neutral")
                                                                                        .value("3")
                                                                                        .order(3)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Insatisfecho")
                                                                                        .value("2")
                                                                                        .order(4)
                                                                                        .build(),
                                                                                Survey.Choice.builder()
                                                                                        .text("Muy Insatisfecho")
                                                                                        .value("1")
                                                                                        .order(5)
                                                                                        .build()
                                                                        ))
                                                                        .build()
                                                        ))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .footerBlock(Survey.FooterBlock.builder()
                        .thankYouMessage("¡Gracias por sus valiosos comentarios!")
                        .submitButtonText("Enviar Encuesta")
                        .contactInformation("Para preguntas, contacte a support@example.com")
                        .additionalInstructions(Collections.singletonList(
                                "Sus respuestas son confidenciales y se utilizarán para mejorar nuestros servicios"
                        ))
                        .build())
                .build();
    }
}