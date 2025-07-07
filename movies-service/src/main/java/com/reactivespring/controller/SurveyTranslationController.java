package com.reactivespring.controller;

import com.reactivespring.dto.SurveyTranslationRequest;
import com.reactivespring.dto.SurveyTranslationResponse;
import com.reactivespring.service.SurveyTranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/surveys")
@Validated
@Slf4j
public class SurveyTranslationController {
    
    private final SurveyTranslationService translationService;
    
    public SurveyTranslationController(SurveyTranslationService translationService) {
        this.translationService = translationService;
    }
    
    @PostMapping(value = "/translate", 
                 consumes = MediaType.APPLICATION_JSON_VALUE, 
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SurveyTranslationResponse>> translateSurvey(
            @Valid @RequestBody SurveyTranslationRequest request) {
        
        log.info("Received translation request from {} to {}", 
                request.getSourceLanguage(), request.getTargetLanguage());
        
        return translationService.translateSurvey(request)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response))
                .onErrorResume(exception -> {
                    log.error("Translation failed", exception);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
                });
    }
    
    @PostMapping(value = "/translate/async",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TranslationJobResponse>> translateSurveyAsync(
            @Valid @RequestBody SurveyTranslationRequest request) {
        
        log.info("Received async translation request from {} to {}", 
                request.getSourceLanguage(), request.getTargetLanguage());
        
        // For async processing, you would typically return a job ID
        // and process the translation in the background
        String jobId = generateJobId();
        
        // Start async processing (fire and forget)
        translationService.translateSurvey(request)
                .doOnSuccess(result -> log.info("Async translation completed for job: {}", jobId))
                .doOnError(error -> log.error("Async translation failed for job: {}", jobId, error))
                .subscribe();
        
        TranslationJobResponse jobResponse = TranslationJobResponse.builder()
                .jobId(jobId)
                .status("PROCESSING")
                .estimatedCompletionTimeMs(30000L) // 30 seconds estimate
                .build();
        
        return Mono.just(ResponseEntity.accepted().body(jobResponse));
    }
    
    @GetMapping("/translate/languages")
    public Mono<ResponseEntity<SupportedLanguagesResponse>> getSupportedLanguages() {
        SupportedLanguagesResponse response = SupportedLanguagesResponse.builder()
                .supportedLanguages(java.util.Arrays.asList(
                        new LanguageInfo("en", "English"),
                        new LanguageInfo("es", "Spanish"),
                        new LanguageInfo("fr", "French"),
                        new LanguageInfo("de", "German"),
                        new LanguageInfo("it", "Italian"),
                        new LanguageInfo("pt", "Portuguese"),
                        new LanguageInfo("ru", "Russian"),
                        new LanguageInfo("ja", "Japanese"),
                        new LanguageInfo("ko", "Korean"),
                        new LanguageInfo("zh", "Chinese"),
                        new LanguageInfo("ar", "Arabic"),
                        new LanguageInfo("hi", "Hindi"),
                        new LanguageInfo("nl", "Dutch"),
                        new LanguageInfo("sv", "Swedish"),
                        new LanguageInfo("no", "Norwegian"),
                        new LanguageInfo("da", "Danish"),
                        new LanguageInfo("fi", "Finnish")
                ))
                .build();
        
        return Mono.just(ResponseEntity.ok(response));
    }
    
    private String generateJobId() {
        return "job_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TranslationJobResponse {
        private String jobId;
        private String status;
        private Long estimatedCompletionTimeMs;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class SupportedLanguagesResponse {
        private java.util.List<LanguageInfo> supportedLanguages;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LanguageInfo {
        private String code;
        private String name;
    }
}