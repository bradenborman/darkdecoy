package com.darkdecoy.repository;

import com.darkdecoy.model.GeneratedWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeneratedWordRepository extends JpaRepository<GeneratedWord, Long> {

    Optional<GeneratedWord> findByCategoryIgnoreCaseAndRealWordIgnoreCase(String category, String realWord);

}