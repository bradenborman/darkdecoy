package com.darkdecoy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "generated_words")
public class GeneratedWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    @Column(name = "real_word")
    private String realWord;

    public GeneratedWord() {}

    public GeneratedWord(String category, String realWord) {
        this.category = category;
        this.realWord = realWord;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getRealWord() {
        return realWord;
    }

}