package com.jillliu.safeplate.assess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jillliu.safeplate.llm.EvidenceCategory;
import com.jillliu.safeplate.llm.EvidenceClassification;

public class AssessmentRulesTest {

    private static ClassifiedEvidence review(EvidenceCategory category, String quote) {
        return new ClassifiedEvidence(
                new EvidenceClassification(category, quote, ""),
                EvidenceSource.PLACES_CONTEXTUAL);
    }

    private static ClassifiedEvidence website(EvidenceCategory category, String quote) {
        return new ClassifiedEvidence(
                new EvidenceClassification(category, quote, ""),
                EvidenceSource.WEBSITE);
    }

    @Test
    void anyNegativeMarksRed() {
        List<ClassifiedEvidence> reviews = List.of(
                review(EvidenceCategory.POSITIVE, "Menu has GF label"),
                review(EvidenceCategory.POSITIVE, "Waiter said they can accommodate diet restrictions"),
                review(EvidenceCategory.POSITIVE, "Waiter knows all the alergens we told him"),
                review(EvidenceCategory.POSITIVE, "They have dairy free options"),
                review(EvidenceCategory.POSITIVE, "they can do nut free"),
                review(EvidenceCategory.NEGATIVE, "I got sick after, despite waiter said its gluten free!"));

        Assessment result = AssessmentRules.assess(reviews);

        assertEquals(AccommodationLevel.RED, result.level());
        assertEquals(5, result.positiveCount());
        assertEquals(1, result.negativeCount());
        assertEquals("I got sick after, despite waiter said its gluten free!", result.quote());
    }

    @Test
    void websiteNegativeMarksRed() {
        List<ClassifiedEvidence> evidence = List.of(
                review(EvidenceCategory.POSITIVE, "waiter listened to all our allergies"),
                review(EvidenceCategory.POSITIVE, "chef modified the dish to exclude shellfish"),
                website(EvidenceCategory.NEGATIVE, "we are unable to accommodate allergies"));

        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.RED, result.level());
        assertEquals(2, result.positiveCount());
        assertEquals(1, result.negativeCount());
        assertEquals("we are unable to accommodate allergies", result.quote());
    }

    @Test
    void reviewPositiveMarksGreen() {
        List<ClassifiedEvidence> evidence = List.of(
                review(EvidenceCategory.POSITIVE, "they have gluten free options"),
                review(EvidenceCategory.POSITIVE, "chef is knowlegable about gluten free"),
                review(EvidenceCategory.POSITIVE, "the gluten free pasta is amazing"),
                website(EvidenceCategory.BOILERPLATE, "Please tell waiters regarding the diet restrictions"));

        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.GREEN, result.level());
        assertEquals(3, result.positiveCount());
        assertEquals(0, result.negativeCount());
        assertEquals("they have gluten free options", result.quote());
    }

    @Test
    void singleReviewPositiveMarksGreen() {
        List<ClassifiedEvidence> evidence = List.of(
                review(EvidenceCategory.POSITIVE, "waiter said the chef can make gluten free"),
                review(EvidenceCategory.IRRELEVANT, "this is my favorite restaurant in town!"),
                review(EvidenceCategory.IRRELEVANT, "my whole family loves dining here"));

        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.GREEN, result.level());
        assertEquals(1, result.positiveCount());
        assertEquals(0, result.negativeCount());
        assertEquals("waiter said the chef can make gluten free", result.quote());
    }

    @Test
    void positiveFromBothSourcesMarksGreen() {
        List<ClassifiedEvidence> evidence = List.of(
                website(EvidenceCategory.POSITIVE, "We accommodate most of dietary restrictions"),
                review(EvidenceCategory.POSITIVE, "They have clear GF and DF label on the menu"));

        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.GREEN, result.level());
        assertEquals(2, result.positiveCount());
        assertEquals(0, result.negativeCount());
        assertEquals("They have clear GF and DF label on the menu", result.quote());
    }

    @Test
    void positiveOnlyFromWebsiteMarksYellow() {
        List<ClassifiedEvidence> evidence = List.of(
                website(EvidenceCategory.POSITIVE, "we accommodation allergies"),
                review(EvidenceCategory.IRRELEVANT, "Their pad thai is so good!"),
                review(EvidenceCategory.IRRELEVANT, "All the dishes we ordered were delicious"));

        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.YELLOW, result.level());
        assertEquals(1, result.positiveCount());
        assertEquals(0, result.negativeCount());
        assertEquals("we accommodation allergies", result.quote());
    }

    @Test
    void onlyBoilerplateAndIrrelevant() {
        List<ClassifiedEvidence> evidence = List.of(
                review(EvidenceCategory.BOILERPLATE, ""),
                review(EvidenceCategory.IRRELEVANT, ""));

        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.UNKNOWN, result.level());
        assertEquals(0, result.positiveCount());
        assertEquals(0, result.negativeCount());
        assertEquals("", result.quote());
    }

    @Test
    void noEvidenceMarksUnknown() {
        List<ClassifiedEvidence> evidence = List.of();
        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.UNKNOWN, result.level());
        assertEquals(0, result.positiveCount());
        assertEquals(0, result.negativeCount());
        assertEquals("", result.quote());
    }

    @Test
    void otherAllergenMarksGreen() {
        List<ClassifiedEvidence> evidence = List.of(
                review(EvidenceCategory.POSITIVE, "I'm allergic to peanut and they make it peanut free"),
                review(EvidenceCategory.POSITIVE, "They have peanut free sandwishes"));
        Assessment result = AssessmentRules.assess(evidence);
        assertEquals(AccommodationLevel.GREEN, result.level());
        assertEquals(2, result.positiveCount());
        assertEquals(0, result.negativeCount());
        assertEquals("I'm allergic to peanut and they make it peanut free", result.quote());
    }

}
