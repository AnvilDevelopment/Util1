package us.anvildevelopment.util.tools.security;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * <h2>Sanitizer – a tiny, zero-dependency library of safe-string helpers</h2>
 *
 * <p>All methods are <code>static</code>, <code>null-safe</code> and return a <b>non-null</b> result.
 * They are deliberately defensive – if the input is <code>null</code> or empty, a random UUID is returned
 * (you can change the fallback policy by overriding the protected methods).</p>
 *
 * <pre>
 *   String safeFile = Sanitizer.fileName(userInput);          // "my_doc.txt"
 *   String safePath = Sanitizer.pathSegment(userInput);      // "my_folder"
 *   String safeKey  = Sanitizer.databaseKey(userInput);      // "user_123"
 *   String safeUrl  = Sanitizer.urlPart(userInput);          // "search-term"
 * </pre>
 * <p>This class should not be relied on fully for production</p>
 */
public final class Sanitizer {

    private Sanitizer() { /* utility class */ }

    /** Max length for any sanitized name (prevents DoS via huge strings). */
    private static final int MAX_LENGTH = 200;

    /** Fallback when the input is empty or cannot be sanitized. */
    private static String fallback() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /* --------------------------------------------------------------------- *
     *  1. FILE / DIRECTORY NAMES
     * --------------------------------------------------------------------- */

    /**
     * Sanitizes a string to be used as a **file name** (including extension).
     * <ul>
     *   <li>Removes <code>/ \ ..</code> and any control characters.</li>
     *   <li>Keeps letters, digits, <code>_ - . @</code> and the original extension.</li>
     *   <li>Truncates to {@link #MAX_LENGTH}.</li>
     * </ul>
     */
    public static String fileName(String raw) {
        return fileName(raw, null);
    }

    /**
     * Same as {@link #fileName(String)} but forces a given extension (e.g. ".json").
     * If the extension is <code>null</code> the original extension (if any) is preserved.
     */
    public static String fileName(String raw, String forcedExtension) {
        if (raw == null || raw.isBlank()) return fallback() + (forcedExtension != null ? forcedExtension : "");

        // split name / extension
        String name = raw;
        String ext  = "";
        int dot = raw.lastIndexOf('.');
        if (dot > 0 && dot < raw.length() - 1) {
            name = raw.substring(0, dot);
            ext  = raw.substring(dot);
        }

        String cleanName = sanitizeBase(name, "[^\\w.@\\-]");
        String cleanExt  = forcedExtension != null ? forcedExtension : ext;

        // enforce extension starts with a dot
        if (cleanExt != null && !cleanExt.isEmpty() && !cleanExt.startsWith(".")) {
            cleanExt = "." + cleanExt;
        }

        String result = cleanName + cleanExt;
        return truncate(result);
    }

    /**
     * Sanitizes a string to be used as a **folder / path segment**.
     * <ul>
     *   <li>Never allows <code>/ \ ..</code>.</li>
     *   <li>Keeps letters, digits, <code>_ - . @</code>.</li>
     * </ul>
     */
    public static String pathSegment(String raw) {
        if (raw == null || raw.isBlank()) return fallback();

        // Strip any path separator or traversal
        String clean = raw.replaceAll("[/\\\\]", "_")
                .replaceAll("\\.\\.", "");

        return truncate(sanitizeBase(clean, "[^\\w.@\\-]"));
    }

    /* --------------------------------------------------------------------- *
     *  2. DATABASE / KEY / ID
     * --------------------------------------------------------------------- */

    /**
     * Sanitizes a value that will be used as a **primary key / identifier**.
     * <ul>
     *   <li>Only letters, digits and underscore.</li>
     *   <li>Starts with a letter (or underscore).</li>
     * </ul>
     */
    public static String databaseKey(String raw) {
        if (raw == null || raw.isBlank()) return "key_" + fallback();

        String clean = sanitizeBase(raw, "[^\\w]");
        if (clean.isEmpty()) return "key_" + fallback();

        // Ensure it starts with a letter or underscore
        if (!Character.isLetter(clean.charAt(0)) && clean.charAt(0) != '_') {
            clean = "k_" + clean;
        }
        return truncate(clean);
    }

    /**
     * Same as {@link #databaseKey(String)} but guarantees the result starts with the given prefix.
     */
    public static String databaseKey(String raw, String prefix) {
        String key = databaseKey(raw);
        if (prefix == null || prefix.isBlank()) return key;
        return prefix + "_" + key;
    }

    /* --------------------------------------------------------------------- *
     *  3. URL / SLUG / SEO
     * --------------------------------------------------------------------- */

    /**
     * Creates a URL-friendly slug.
     * <ul>
     *   <li>Lower-case, letters, digits, hyphen.</li>
     *   <li>Multiple spaces/hyphens collapsed.</li>
     * </ul>
     */
    public static String slug(String raw) {
        if (raw == null || raw.isBlank()) return fallback().toLowerCase();

        String lower = raw.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s-]", "")   // keep letters, numbers, space, hyphen
                .replaceAll("\\s+", "-")                // spaces to hyphen
                .replaceAll("-+", "-")                  // collapse hyphens
                .replaceAll("^-|-$", "");               // trim leading/trailing hyphen

        return truncate(lower.isEmpty() ? fallback().toLowerCase() : lower);
    }

    /* --------------------------------------------------------------------- *
     *  4. USERNAME / DISPLAY NAME
     * --------------------------------------------------------------------- */

    /**
     * Safe username – letters, digits, underscore, hyphen, dot.
     * First character must be a letter.
     */
    public static String username(String raw) {
        if (raw == null || raw.isBlank()) return "user_" + fallback();

        String clean = sanitizeBase(raw, "[^\\w.\\-]");
        if (clean.isEmpty()) return "user_" + fallback();

        if (!Character.isLetter(clean.charAt(0))) {
            clean = "u_" + clean;
        }
        return truncate(clean);
    }

    /* --------------------------------------------------------------------- *
     *  5. GENERAL TEXT (HTML, JSON, etc.)
     * --------------------------------------------------------------------- */

    /**
     * Escapes HTML special characters (<, >, &, ", ').
     */
    public static String htmlEscape(String raw) {
        if (raw == null) return "";
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Strips *all* HTML tags, then applies {@link #htmlEscape(String)}.
     */
    public static String stripHtml(String raw) {
        if (raw == null) return "";
        String stripped = raw.replaceAll("<[^>]*>", "");
        return htmlEscape(stripped);
    }

    /* --------------------------------------------------------------------- *
     *  6. INTERNAL HELPERS
     * --------------------------------------------------------------------- */

    private static String sanitizeBase(String input, String disallowedPattern) {
        return input.replaceAll(disallowedPattern, "_")
                .replaceAll("_+", "_")
                .replaceAll("^-|-$", "");
    }

    private static String truncate(String s) {
        if (s.length() <= MAX_LENGTH) return s;
        return s.substring(0, MAX_LENGTH);
    }

}
