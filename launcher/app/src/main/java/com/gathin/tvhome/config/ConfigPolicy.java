package com.gathin.tvhome.config;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure Java configuration resolution policy.
 * Resolves feature flags between compiled defaults and optional runtime JSON overlays.
 *
 * Primary design: receives an already-parsed nullable Boolean from the host config loader.
 * Secondary design: provides a strict, fail-closed fallback parser for raw JSON strings in test environments.
 */
public final class ConfigPolicy {

    public static final String KEY_SHOW_SYNTHETIC_SPOTLIGHT = "show_synthetic_spotlight";
    public static final boolean DEFAULT_SHOW_SYNTHETIC_SPOTLIGHT = false;

    private ConfigPolicy() {
    }

    /**
     * Preferred resolution method: receives an already-parsed nullable Boolean from the configuration loader.
     * If the overlay value is non-null, it takes precedence; otherwise falls back to the compiled default.
     *
     * @param compiledDefault Default value compiled in XML resources (false).
     * @param overlayValue Parsed Boolean value from runtime configuration, or null if absent/unparsed.
     * @return Resolved boolean flag.
     */
    public static boolean resolveSpotlightEnabled(boolean compiledDefault, Boolean overlayValue) {
        return overlayValue != null ? overlayValue : compiledDefault;
    }

    /**
     * Resolves the spotlight shelf toggle from an Android JSONObject overlay.
     * Safely extracts a nullable Boolean and delegates to {@link #resolveSpotlightEnabled(boolean, Boolean)}.
     */
    public static boolean resolveSpotlightEnabled(boolean compiledDefault, JSONObject overlayJson) {
        if (overlayJson == null) {
            return compiledDefault;
        }
        try {
            Object val = overlayJson.opt(KEY_SHOW_SYNTHETIC_SPOTLIGHT);
            Boolean parsed = (val instanceof Boolean) ? (Boolean) val : null;
            return resolveSpotlightEnabled(compiledDefault, parsed);
        } catch (Throwable ignored) {
            return compiledDefault;
        }
    }

    /**
     * Fallback parser for raw JSON strings in headless test environments.
     * Strictly limited to the single root boolean key {@value #KEY_SHOW_SYNTHETIC_SPOTLIGHT}.
     * Fails closed (returning compiledDefault) on malformed JSON, duplicate keys,
     * nested occurrences, string values, numeric values, or similarly named keys.
     *
     * @param compiledDefault Default value compiled in XML resources (false).
     * @param rawJsonString Raw string content of runtime overlay, or null.
     * @return Resolved boolean flag.
     */
    public static boolean resolveSpotlightEnabled(boolean compiledDefault, String rawJsonString) {
        if (rawJsonString == null || rawJsonString.trim().isEmpty()) {
            return compiledDefault;
        }
        Boolean parsed = parseBooleanKey(rawJsonString, KEY_SHOW_SYNTHETIC_SPOTLIGHT);
        return resolveSpotlightEnabled(compiledDefault, parsed);
    }

    /**
     * Extracts a boolean value strictly for {@value #KEY_SHOW_SYNTHETIC_SPOTLIGHT}.
     * Fails closed (returns null) on:
     * - non-matching keys
     * - malformed JSON syntax / missing enclosing braces
     * - duplicate keys
     * - nested keys inside inner objects
     * - string values ("true", "false")
     * - numeric values (1, 0)
     * - similarly named keys
     */
    public static Boolean parseBooleanKey(String json, String key) {
        if (json == null || key == null || !KEY_SHOW_SYNTHETIC_SPOTLIGHT.equals(key)) {
            return null;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return null;
        }

        // Count occurrences of the exact quoted key to reject duplicates
        Pattern anyKeyPattern = Pattern.compile("\"" + Pattern.quote(key) + "\"");
        Matcher anyKeyMatcher = anyKeyPattern.matcher(trimmed);
        int keyOccurrences = 0;
        while (anyKeyMatcher.find()) {
            keyOccurrences++;
        }
        if (keyOccurrences != 1) {
            // Missing (0) or duplicate (>1): fail-closed
            return null;
        }

        // Ensure key is at root level (not nested in inner braces)
        int keyIndex = trimmed.indexOf("\"" + key + "\"");
        int braceDepth = 0;
        boolean inQuotes = false;
        for (int i = 0; i < keyIndex; i++) {
            char c = trimmed.charAt(i);
            if (c == '\\' && i + 1 < keyIndex) {
                i++;
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') braceDepth++;
                else if (c == '}') braceDepth--;
            }
        }
        if (braceDepth != 1) {
            // Nested inside inner object/array: fail-closed
            return null;
        }

        // Match exact key at root level with strict boolean token
        Pattern strictPattern = Pattern.compile(
                "(?:^|[{,\\s])\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false|\\\"[^\\\"]*\\\"|\\d+|[^,\\}\\s]+)\\s*(?:[,}\\s]|$)"
        );
        Matcher matcher = strictPattern.matcher(trimmed);
        if (matcher.find()) {
            String token = matcher.group(1).trim();
            if ("true".equals(token)) {
                return Boolean.TRUE;
            }
            if ("false".equals(token)) {
                return Boolean.FALSE;
            }
            // Strings ("true", "false"), numbers (1, 0), or arbitrary tokens fail closed
            return null;
        }

        return null;
    }
}
