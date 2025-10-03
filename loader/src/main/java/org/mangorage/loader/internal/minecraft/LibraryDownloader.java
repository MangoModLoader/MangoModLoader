package org.mangorage.loader.internal.minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class and logic for handling Minecraft libraries.
 */
public final class LibraryDownloader {

    /**
     * Enum representing the target operating system.
     */
    public enum OS {
        WINDOWS("windows"),
        LINUX("linux"),
        OSX("osx"),
        UNKNOWN("unknown");

        public final String name;

        OS(String name) {
            this.name = name;
        }
    }

    /**
     * Iterates through the libraries and collects the download URLs required for the specified OS.
     * This method handles both main artifacts and platform-specific native libraries.
     *
     * @param version The parsed MinecraftVersion object.
     * @param targetOS The target operating system (e.g., OS.WINDOWS).
     * @return A list of strings, where each string is a URL for a required library.
     */
    public static List<String> downloadLibraries(MinecraftVersion version, OS targetOS) {
        List<String> urls = new ArrayList<>();

        if (version.libraries() == null) {
            return urls;
        }

        for (Library library : version.libraries()) {
            // 1. Check the general rules for the library
            if (!isLibraryApplicable(library, targetOS)) {
                continue; // Skip this library
            }

            // 2. Add the main artifact URL (if available)
            // Most libraries are non-native and only have the main artifact.
            if (library.downloads() != null && library.downloads().artifact() != null) {
                String artifactUrl = library.downloads().artifact().url();
                if (artifactUrl != null) {
                    urls.add(artifactUrl);
                }
            }

            // 3. Check for platform-specific native files
            if (library.natives() != null && library.downloads() != null && library.downloads().classifiers() != null) {
                // The 'natives' map tells us the classifier string (e.g., "natives-windows")
                String classifierKey = library.natives().get(targetOS.name);

                if (classifierKey != null) {
                    // The 'classifiers' map uses that key to find the DownloadInfo
                    DownloadInfo nativeInfo = library.downloads().classifiers().get(classifierKey);

                    if (nativeInfo != null && nativeInfo.url() != null) {
                        urls.add(nativeInfo.url());
                    }
                }
            }
        }

        return urls;
    }

    /**
     * Determines if a library is applicable based on its rules and the target OS.
     * * Minecraft's rule logic is:
     * - If rules is null, assume 'allow'.
     * - If rules exist, iterate them. The FIRST rule that matches the OS/features determines
     * the action. If no rule matches, the default action is "disallow" unless the final rule is "allow"
     * without conditions, or there is a final "disallow" without conditions.
     * * For simplicity and robustness against missing rules, we use the standard Minecraft launcher logic:
     * - Default action is 'allow'.
     * - If any rule matches and is 'disallow', it overrides to 'disallow'.
     * - If any rule matches and is 'allow', it overrides to 'allow'.
     * - If no rule matches, the original default of 'allow' stands.
     *
     * @param library The Library object to check.
     * @param targetOS The OS to check against.
     * @return True if the library should be included, false otherwise.
     */
    private static boolean isLibraryApplicable(Library library, OS targetOS) {
        // If there are no rules, the library is always included (default: allow)

        if (library.rules() == null || library.rules().isEmpty()) {
            return true;
        }


        // Standard default action for Minecraft libraries is 'allow'
        boolean defaultAllow = true;
        boolean matchedRuleFound = false;

        for (Rule rule : library.rules()) {
            // Check if the rule matches the target OS
            boolean osMatch = rule.os() == null || targetOS.name.equals(rule.os().name());

            // NOTE: We ignore the 'features' rules for this simple OS-focused logic.
            // A production launcher would need to evaluate both OS and features.

            if (osMatch) {
                matchedRuleFound = true;
                if ("allow".equals(rule.action())) {
                    return true; // Explicit match to allow, stop processing
                } else if ("disallow".equals(rule.action())) {
                    return false; // Explicit match to disallow, stop processing
                }
            }
        }

        // If we found rules but none matched the target OS, the default action is usually implicit 'allow'
        // unless a final rule without OS condition specified "disallow".
        // Based on analysis of version manifest files, if a library has rules, but none match,
        // it is typically disallowed, but if a match occurs, that action is taken.
        // The most robust approach for these manifests is: if rules exist and none matched,
        // the library is excluded *unless* we explicitly saw an 'allow' without conditions.
        // For the sake of matching the "only for windows" example, we stick to the core logic:

        if (matchedRuleFound) {
            // This case shouldn't be hit with the logic above (since we return immediately),
            // but if we modified the logic to process all rules, the final action would be taken.
            // Since we return on the first match, this part is only hit if rules exist but none matched.
            return false;
        }

        // If there were rules, but none matched any condition (including the OS), and we reached the end,
        // it means the library should probably be skipped (disallowed).
        return defaultAllow;
    }
}