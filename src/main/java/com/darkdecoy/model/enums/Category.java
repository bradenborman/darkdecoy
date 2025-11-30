package com.darkdecoy.model.enums;


public enum Category {

    CUSTOM("Custom", null),
    ANIMALS("Animals", "prompts/animals.txt"),
    COUNTRIES("Countries", "prompts/countries.txt"),
    US_STATES("US States", "prompts/us-states.txt"),
    NFL_PLAYERS("NFL Players", "prompts/nfl-players.txt"),
    NBA_PLAYERS("NBA Players", "prompts/nba-players.txt"),
    MLB_PLAYERS("MLB Players", "prompts/mlb-players.txt"),
    NHL_PLAYERS("NHL Players", "prompts/nhl-players.txt"),
    FAMOUS_ACTORS("Famous Actors", "prompts/famous-actors.txt"),
    FAMOUS_SINGERS("Famous Singers", "prompts/famous-singers.txt"),
    FAMOUS_DUOS("Famous Duos", "prompts/famous-duos.txt"),
    BREAKFAST_FOODS("Breakfast Foods", "prompts/breakfast-foods.txt"),
    VEHICLES("Vehicles", "prompts/vehicles.txt"),
    BRANDS("Brands", "prompts/brands.txt"),
    CARTOON_CHARACTERS("Cartoon Characters", "prompts/cartoon-characters.txt"),
    SUPERHEROES("Superheroes", "prompts/superheroes.txt"),
    CITIES("Cities", "prompts/cities.txt"),
    VIDEO_GAMES("Video Games", "prompts/video-games.txt"),
    ANIMALS_WITH_FUR("Animals With Fur", "prompts/animals-with-fur.txt"),
    SPORTS_TEAMS("Sports Teams", "prompts/sports-teams.txt");

    private final String label;
    private final String promptPath;

    Category(String label, String promptPath) {
        this.label = label;
        this.promptPath = promptPath;
    }

    public String getLabel() {
        return label;
    }

    public String getPromptPath() {
        return promptPath;
    }

    public static Category fromString(String value) {
        if (value == null) {
            return null;
        }

        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(value) || c.getLabel().equalsIgnoreCase(value)) {
                return c;
            }
        }

        return null;
    }

}