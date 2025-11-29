package com.darkdecoy.model;

import java.util.List;

public class DecoyBatch {

    private DecoyPair primary;
    private List<DecoyPair> fallbacks;

    public DecoyBatch(DecoyPair primary, List<DecoyPair> fallbacks) {
        this.primary = primary;
        this.fallbacks = fallbacks;
    }

    public DecoyPair getPrimary() {
        return primary;
    }

    public void setPrimary(DecoyPair primary) {
        this.primary = primary;
    }

    public List<DecoyPair> getFallbacks() {
        return fallbacks;
    }

    public void setFallbacks(List<DecoyPair> fallbacks) {
        this.fallbacks = fallbacks;
    }

}