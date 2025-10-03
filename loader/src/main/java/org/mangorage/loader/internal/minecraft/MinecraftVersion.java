package org.mangorage.loader.internal.minecraft;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

// ===============================================
// 1. ROOT CLASS: Represents the entire JSON file
// ===============================================

public record MinecraftVersion(
        String id,
        String type,
        String assets,

        @SerializedName("complianceLevel") int complianceLevel,
        @SerializedName("minimumLauncherVersion") int minimumLauncherVersion,

        @SerializedName("assetIndex") AssetIndex assetIndex,
        @SerializedName("javaVersion") JavaVersion javaVersion,
        Downloads downloads,
        List<Library> libraries,

        // Arguments are complex: a list containing both simple strings and JSON objects (rules).
        // A Map is used for the top-level structure (e.g., "game": [...], "jvm": [...])
        Map<String, List<Object>> arguments,

        @SerializedName("releaseTime") String releaseTime,
        String time,
        Logging logging
) {}

// ===============================================
// 2. DOWNLOADS AND ASSET DATA RECORDS
// ===============================================

// Maps the information structure for client.jar, server.jar, etc.
record Downloads(
        DownloadInfo client,
        @SerializedName("client_mappings") DownloadInfo clientMappings,
        DownloadInfo server,
        @SerializedName("server_mappings") DownloadInfo serverMappings
) {}

// Basic structure for any downloadable file metadata
record DownloadInfo(
        String sha1,
        long size,
        String url
) {}

// Represents the "assetIndex" object
record AssetIndex(
        String id,
        String sha1,
        long size,
        @SerializedName("totalSize") long totalSize,
        String url
) {}

// Represents the "javaVersion" object
record JavaVersion(
        String component,
        @SerializedName("majorVersion") int majorVersion
) {}

// ===============================================
// 3. LIBRARY AND RULES RECORDS
// ===============================================

// Represents an entry in the "libraries" array
record Library(
        String name,
        @SerializedName("downloads") LibraryDownloads downloads, // Use the specific LibraryDownloads
        List<Rule> rules,
        // Add the "natives" field, which is a map of OS names to classifier names (e.g., "linux": "natives-linux")
        Map<String, String> natives,
        String url
) {}

// **CORRECTED:** Maps the download links for the library JARs and natives.
record LibraryDownloads(
        // The main JAR file download information
        DownloadInfo artifact,

        // The native libraries for different OSs, keyed by the classifier name.
        // e.g., "natives-windows": { ... DownloadInfo ... }
        Map<String, DownloadInfo> classifiers
) {}

// Represents a rule for conditional downloads (based on OS, features, etc.)
record Rule(
        String action,
        RuleOS os,
        RuleFeatures features,
        // Note: GSON automatically handles the 'value' field which can be a String or a List<String>
        // when using the Object type, but this is a complex case. For simplicity in the Rule, we'll
        // exclude the generic 'value' and rely on a custom deserializer or Gson's leniency for arguments.
        // For *Library* rules, only action, os, and features are usually present.
        String value // Added 'value' for the complex argument rules
) {}

// Nested object for OS-based rules
record RuleOS(
        String name,
        String version,
        String arch
) {}

// Nested object for feature-based rules
record RuleFeatures(
        @SerializedName("is_demo_user") Boolean isDemoUser,
        @SerializedName("has_custom_resolution") Boolean hasCustomResolution
        // Add other features as needed
) {}


// ===============================================
// 4. LOGGING CONFIGURATION RECORD
// ===============================================

record Logging(
        LoggingClient client
) {}

record LoggingClient(
        String argument,
        LoggingFile file,
        String type
) {}

record LoggingFile(
        String id,
        String sha1,
        long size,
        String url
) {}