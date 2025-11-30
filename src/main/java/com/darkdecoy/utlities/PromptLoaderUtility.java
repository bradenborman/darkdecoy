package com.darkdecoy.utlities;

import com.darkdecoy.model.enums.Category;
import org.springframework.core.io.ClassPathResource;

public class PromptLoaderUtility {

    public static final String DEFAULT_PROMPT = """
            Generate one primary pair and ten fallback pairs based on the category. Output JSON only with no extra text.                 
            Rules:
            1. All entries must be real and must fit the category provided.
            2. The real entry and the decoy must be different but still reasonably related.
            3. The decoy should be similar enough to be believable without being the same item.
            4. Provide exactly one primary pair and ten fallback pairs.
            5. Output must be valid JSON only.
            6. Avoid extremely obvious or overly common answers. Choose entries that are recognizable but not the first that come to mind.
            7. Answers should be varied and not repeated across requests.
            8. If the category represents people, use real full names only.
            9. Make sure all outputs are appropriate for a general United States audience.
            """;

    public static String load(Category category) {
        if (category.getPromptPath() == null) {
            return DEFAULT_PROMPT;
        }

        try {
            var resource = new ClassPathResource(category.getPromptPath());
            String prompt = new String(resource.getInputStream().readAllBytes());

            if(prompt.trim().equals(""))
                return DEFAULT_PROMPT;

            return prompt;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt for " + category.name(), e);
        }
    }

}