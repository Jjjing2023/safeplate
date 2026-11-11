package com.jillliu.safeplate.places;

public enum PriceLevel {
    UNSPECIFIED, FREE, INEXPENSIVE, MODERATE, EXPENSIVE, VERY_EXPENSIVE;

    public static PriceLevel fromApi(String value) {
        if (value == null || value.isBlank()) {
            return UNSPECIFIED;
        }
        String price = value.strip().replaceFirst("^PRICE_LEVEL_", "");
        try {
            return PriceLevel.valueOf(price);
        } catch (IllegalArgumentException e) {
            return UNSPECIFIED;
        }
    }
}
