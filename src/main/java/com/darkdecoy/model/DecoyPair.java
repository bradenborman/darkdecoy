package com.darkdecoy.model;

public class DecoyPair {
    private String real;
    private String decoy;
    private String category;

    public DecoyPair() {}

    public DecoyPair(String real, String decoy, String category) {
        this.real = real;
        this.decoy = decoy;
        this.category = category;
    }

    public String getReal() {
        return real;
    }

    public void setReal(String real) {
        this.real = real;
    }

    public String getDecoy() {
        return decoy;
    }

    public void setDecoy(String decoy) {
        this.decoy = decoy;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
