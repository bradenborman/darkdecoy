package com.darkdecoy.controller;

import com.darkdecoy.model.DecoyPair;
import com.darkdecoy.service.DecoyGenerationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DecoyGenerationController {

    private final DecoyGenerationService service;

    public DecoyGenerationController(DecoyGenerationService service) {
        this.service = service;
    }

    @GetMapping("/auto")
    public DecoyPair autoGenerate(@RequestParam String category) {
        return service.generatePair(category);
    }

}