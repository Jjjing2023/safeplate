package com.jillliu.safeplate.assess;

import java.util.List;

import com.jillliu.safeplate.llm.EvidenceCategory;

/**
 * Decides a restaurant's accommodation level from its classified evidence,
 * output by LLM.
 * The LLM classifies each piece of text; these rules make the final call
 */
public class AssessmentRules {

    /**
     * Assesses one restaurant. Rules are checked in order; the first match wins:
     * <ol>
     * <li>Any NEGATIVE from any source -> {@link AccommodationLevel#RED}.
     * A single refusal outweighs any number of positive.</li>
     * <li>At least one POSITIVE from a review ->
     * {@link AccommodationLevel#GREEN}.</li>
     * <li>POSITIVE only from the website -> {@link AccommodationLevel#YELLOW}.
     * The restaurant claims it, but no customer has confirmed it.</li>
     * <li>Otherwise -> {@link AccommodationLevel#UNKNOWN}.</li>
     * </ol>
     * 
     * <p>
     * BOILERPLATE and IRRELEVANT never affect the level. POSITIVEs about
     * other allergies count fully: they show the kitchen is willing to adapt.
     * 
     * <p>
     * Counts include all sources. The quote is the first one that explains
     * the level (first NEGATIVE, first review POSITIVE, or first website POSITIVE),
     * or {@code ""} for UNKNOWN.
     * 
     * @param evidence all classified evidence for one restaurant; may be empty
     * @return the level, POSITIVE and NEGATIVE counts, and a supporting quote
     */
    public static Assessment assess(List<ClassifiedEvidence> evidence) {
        int positiveCount = 0;
        int negativeCount = 0;
        String firstNegativeQuote = null;
        String firstPositiveQuoteFromReview = null;
        String firstPositiveQuoteFromWebsite = null;
        for (ClassifiedEvidence e : evidence) {
            EvidenceCategory category = e.classification().category();
            EvidenceSource source = e.source();
            if (category == EvidenceCategory.NEGATIVE) {
                negativeCount++;
                if (firstNegativeQuote == null)
                    firstNegativeQuote = e.classification().quote();
            } else if (category == EvidenceCategory.POSITIVE) {
                positiveCount++;
                if (source == EvidenceSource.PLACES_CONTEXTUAL && firstPositiveQuoteFromReview == null)
                    firstPositiveQuoteFromReview = e.classification().quote();
                if (source == EvidenceSource.WEBSITE && firstPositiveQuoteFromWebsite == null)
                    firstPositiveQuoteFromWebsite = e.classification().quote();
            }
        }
        // TODO: a single MEGATIVE might be too strict; revisit with real data
        if (negativeCount > 0) {
            return new Assessment(AccommodationLevel.RED, positiveCount, negativeCount, firstNegativeQuote);
        } else if (firstPositiveQuoteFromReview != null) {
            return new Assessment(AccommodationLevel.GREEN, positiveCount, negativeCount, firstPositiveQuoteFromReview);
        } else if (firstPositiveQuoteFromWebsite != null) {
            return new Assessment(AccommodationLevel.YELLOW, positiveCount, negativeCount,
                    firstPositiveQuoteFromWebsite);
        } else {
            return new Assessment(AccommodationLevel.UNKNOWN, positiveCount, negativeCount, "");
        }
    }

}
