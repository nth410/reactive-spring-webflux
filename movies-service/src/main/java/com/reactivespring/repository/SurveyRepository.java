package com.reactivespring.repository;

import com.reactivespring.domain.Survey;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SurveyRepository extends ReactiveMongoRepository<Survey, String> {
    
    Flux<Survey> findByLanguage(String language);
    
    Flux<Survey> findByCreatedBy(String createdBy);
    
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    Flux<Survey> findByTitleContainingIgnoreCase(String title);
    
    @Query("{ 'language': ?0, 'createdBy': ?1 }")
    Flux<Survey> findByLanguageAndCreatedBy(String language, String createdBy);
    
    Mono<Boolean> existsByTitleAndLanguage(String title, String language);
}