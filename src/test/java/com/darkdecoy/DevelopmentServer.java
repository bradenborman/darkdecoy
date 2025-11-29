package com.darkdecoy;

public class DevelopmentServer extends DarkdecoyApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "dev, local");
        DarkdecoyApplication.main(args);
    }

}