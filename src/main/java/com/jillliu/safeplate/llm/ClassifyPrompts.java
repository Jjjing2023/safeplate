package com.jillliu.safeplate.llm;

public class ClassifyPrompts {

    private static final String CLASSIFY_SYSTEM_RULE_V1 = """
                You classify ONE piece of restaurant evidence about dietary accommodation \
                (gluten-free, dairy-free). Answer in JSON.
                category:
                POSITIVE = a specific experience or explicit statement that the restaurant accommodated a dietary restriction;
                NEGATIVE = refusal, 'no substitutions', or a reported reaction;
                BOILERPLATE = generic allergy disclaimers like 'please inform your server of any allergies';
                IRRELEVANT = nothing about dietary needs.
                quote = the exact words from the evidence that support the category, or empty string.
                reason = one short sentence.
            """;

    private static final String CLASSIFY_SYSTEM_RULE_V2 = """
            You classify ONE piece of restaurant evidence (a customer review or text from the \
            restaurant's website) about how the restaurant handles dietary restrictions and food allergies. \
            Any restriction or allergy counts, not only gluten-free or dairy-free. \
            Read the WHOLE text before deciding. Answer in JSON.

            category:
            POSITIVE = the restaurant accommodated, or states it is willing to accommodate, a dietary \
            restriction or allergy. For example: staff checked ingredients or changed a dish; a diner told \
            the staff about an allergy and had a good meal; a diner with celiac disease or an allergy ate \
            without problems; the menu marks gluten-free or dairy-free items; the restaurant offers an \
            allergen menu or accommodates on request or with advance notice. A cross-contamination \
            disclaimer does not cancel a POSITIVE.
            NEGATIVE = the restaurant refused or says it cannot accommodate (no substitutions, an ingredient \
            cannot be removed, dishes are very difficult to modify), staff were dismissive, or a diner \
            reported a reaction or got sick. A reported reaction is NEGATIVE even if the restaurant also \
            tried to accommodate.
            BOILERPLATE = generic text that neither offers nor refuses anything: asking guests to mention \
            allergies, saying no item can be guaranteed allergen-free or that not every request can be met, \
            cross-contamination warnings, and food-safety warnings about raw or undercooked food.
            IRRELEVANT = nothing about the restaurant handling a restriction. Vegan or vegetarian options \
            alone are IRRELEVANT, because vegan does not mean gluten-free or dairy-free. A reviewer's own \
            food preferences are IRRELEVANT.

            quote = copy the exact words from the evidence that support the category, or empty string for IRRELEVANT.
            reason = one short sentence.
            """;
    private static final String CLASSIFY_SYSTEM_RULE_V3 = """
            You classify ONE piece of restaurant evidence (a customer review or text from the \
            restaurant's website) about dietary restrictions and food allergies at the restaurant. \
            Any restriction or allergy counts, not only gluten-free or dairy-free. \
            Read the WHOLE text before deciding. Answer in JSON.

            category:
            POSITIVE = the restaurant accommodated, or states it is willing to accommodate, a dietary \
            restriction or allergy, or the evidence says the restaurant has gluten-free or dairy-free \
            options or dishes. For example: staff checked ingredients or changed a dish; a diner told \
            the staff about an allergy and had a good meal; a diner with celiac disease or an allergy ate \
            without problems; a review mentions gluten-free dishes; the menu marks gluten-free or \
            dairy-free items; the restaurant offers an allergen menu or accommodates on request or with \
            advance notice. A cross-contamination disclaimer does not cancel a POSITIVE.
            NEGATIVE = the restaurant refused or says it cannot accommodate (no substitutions, dishes \
            are very difficult to modify), staff were dismissive, or a diner reported a reaction or got \
            sick. A reported reaction is NEGATIVE even if the restaurant also tried to accommodate.
            BOILERPLATE = generic text that neither offers nor refuses anything: asking guests to mention \
            allergies, saying no item can be guaranteed allergen-free, that some ingredients cannot be \
            completely removed, or that not every request can be met, cross-contamination warnings, and \
            food-safety warnings about raw or undercooked food.
            IRRELEVANT = nothing about dietary restrictions or allergies at the restaurant. Vegan or \
            vegetarian options alone are IRRELEVANT, because vegan does not mean gluten-free or \
            dairy-free. A reviewer's own food preferences are IRRELEVANT.

            quote = copy the exact words from the evidence that support the category, without shortening \
            them or using "...", or empty string for IRRELEVANT.
            reason = one short sentence.
            """;

    public static String promptFor(String version) {
        return switch (version) {
            case "v1" -> CLASSIFY_SYSTEM_RULE_V1;
            case "v2" -> CLASSIFY_SYSTEM_RULE_V2;
            case "v3" -> CLASSIFY_SYSTEM_RULE_V3;
            default -> throw new IllegalArgumentException("Unknown prompt version: " + version);
        };
    }

}
