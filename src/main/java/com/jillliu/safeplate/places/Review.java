package com.jillliu.safeplate.places;

public record Review(
        String text, // null if no text
        Integer rating, // null if no rating yet
        String publishTime,
        String sourceUri) {
}
