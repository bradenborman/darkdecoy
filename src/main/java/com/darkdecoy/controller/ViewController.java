package com.darkdecoy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/lobby"})
    public String index() {
        return "forward:/index.html";
    }
}
