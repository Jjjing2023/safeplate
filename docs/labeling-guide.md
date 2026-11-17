# Evidence labeling guide

How to label one piece of evidence (a review or a website snippet) for the
classification eval set. The model sees only the text, so labels must be based
only on the text.

The real labeled data is kept outside this repo. All examples below are made up.

## Categories

- **POSITIVE**: a specific accommodation experience, or an explicit statement that
  the restaurant accommodates dietary restrictions.
- **NEGATIVE**: a refusal, "no substitutions", dismissive staff, or a reported
  reaction after eating.
- **BOILERPLATE**: a generic disclaimer that any restaurant could print.
- **IRRELEVANT**: nothing about dietary needs.

## Two questions

1. Would this sentence make sense on any restaurant's menu? If yes, it is likely
   BOILERPLATE.
2. Does the restaurant commit to doing something? If yes, it is likely POSITIVE.

## Action vs. offering

|                    | Accommodation action       | Offering only |
| ------------------ | -------------------------- | ------------- |
| GF / DF            | POSITIVE                   | POSITIVE      |
| Other restrictions | POSITIVE + `other-allergy` | IRRELEVANT    |

- Action: staff or kitchen adjusted something for a person.
- Offering: the menu simply has such dishes.
- If a text has both GF/DF and other restrictions, label by the GF/DF part.

## Edge cases

1. GF menu with a cross-contamination disclaimer → POSITIVE.
   "GF menu available, but we can't guarantee no cross-contact."
2. Disclaimer only, no accommodation → BOILERPLATE.
   "Please inform your server of any allergies."
3. Positive and negative in one text → NEGATIVE + `mixed`.
   "They made it gluten free, but I got sick that night."
4. "Gluten-friendly", "can be made GF", "gluten sensitive options" → POSITIVE + `weak`.
5. Reviewer has a restriction and had no issues, no staff action mentioned → POSITIVE.
   "I'm celiac and had no problems here."
6. Vegan offerings only → IRRELEVANT.
   "Great vegan options!"
7. GF or DF labels on the menu → POSITIVE + `menu`.
   "Everything is clearly marked GF."
8. Gluten-removed or gluten-reduced beer → POSITIVE + `weak`.
9. Accommodation of any other or unspecified restriction → POSITIVE + `other-allergy`.
   "They left out the peanuts for my son." / "Very accommodating of our allergies."
10. "Cannot guarantee" is a disclaimer, not a refusal → BOILERPLATE.
    "We cannot guarantee any dish is free of gluten or nuts."
11. "Unable to accommodate" GF or DF → NEGATIVE.
    "We are unable to accommodate gluten-free requests."
12. "Very difficult to modify our dishes" → NEGATIVE.
13. "Mention allergies when booking" → BOILERPLATE.
    Asking for advance notice so the kitchen can prepare → POSITIVE + `weak`.
    "Please give us 72 hours' notice for dietary restrictions."
14. Dismissive staff, even without an explicit "no" → NEGATIVE.
    "The waiter rolled his eyes when I asked about gluten."
15. Reviewer told the server as the menu asks and had a good meal → POSITIVE + `weak`.
16. Food-safety notices ("raw or undercooked…") → BOILERPLATE.

## Tags

- `weak`: GF is mentioned, but the wording is weak or vague.
- `menu`: the evidence comes from menu labels.
- `other-allergy`: no GF/DF mentioned; another or unspecified restriction.
- `celiac`: the reviewer has the restriction (first-hand experience).
- `mixed`: both positive and negative signals (labeled NEGATIVE).
- `trap`: a diet keyword appears, but the text is unrelated to accommodation.
- `hard`: the labeler hesitated.

## Notes

A note is required for `hard` and `mixed`. One short sentence explaining the
decision is enough.

## Data format

One JSON object per line (JSONL):

```json
{
  "id": "e1",
  "source": "WEBSITE",
  "restaurant": "...",
  "text": "...",
  "expected": "BOILERPLATE",
  "tags": [],
  "note": ""
}
```

- `source`: `PLACES_CONTEXTUAL`, `WEBSITE`, or `SYNTHETIC`.
- `expected`: one of the four categories, uppercase.
- Label before running the model, so the model's answer does not influence the label.
