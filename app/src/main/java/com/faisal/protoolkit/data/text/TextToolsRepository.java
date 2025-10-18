package com.faisal.protoolkit.data.text;

import androidx.annotation.NonNull;

/**
 * Provides text transformation utilities.
 */
public class TextToolsRepository {

    public String toUpperCase(@NonNull String input) {
        return input.toUpperCase();
    }

    public String toLowerCase(@NonNull String input) {
        return input.toLowerCase();
    }

    public String toTitleCase(@NonNull String input) {
        if (input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() == 0) {
                continue;
            }
            String word = words[i];
            String transformed = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(transformed);
        }
        return builder.toString();
    }

    public int countWords(@NonNull String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    public int countCharacters(@NonNull String input) {
        return input.length();
    }
}
