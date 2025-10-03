package org.mangorage.loader.internal.minecraft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for downloading files over HTTP.
 * This class requires Java 11 or later for the HttpClient API.
 */
public class Downloader {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * Downloads a file from a URL and saves it to the specified destination directory.
     * The file name is automatically derived from the end of the URL path.
     *
     * @param fileUrl The complete URL of the file to download.
     * @param destinationDir The path to the directory where the file should be saved.
     * @return The Path object representing the downloaded file, or null if download failed.
     * @throws IOException If an I/O error occurs (e.g., file system failure).
     * @throws InterruptedException If the operation is interrupted.
     */
    public static Path downloadFile(String fileUrl, Path destinationDir) throws IOException, InterruptedException {
        // 1. Validate input URL
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            System.err.println("Error: Provided URL is empty or null.");
            return null;
        }

        // 2. Prepare the destination path and ensure the directory exists
        // Extract the file name from the URL path (e.g., "..."/some-file.jar")
        String fileName = getFileNameFromUrl(fileUrl);
        Path destinationPath = destinationDir.resolve(fileName);

        // Create the directory structure if it doesn't exist
        Files.createDirectories(destinationDir);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .GET()
                .build();

        System.out.println("Starting download: " + fileName + " to " + destinationPath);

        // 3. Send the request and handle the response
        try {
            HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                // Use Files.copy for efficient stream handling
                Files.copy(
                        response.body(),
                        destinationPath,
                        StandardCopyOption.REPLACE_EXISTING // Overwrite if file already exists
                );
                System.out.println("Successfully downloaded: " + fileName);
                return destinationPath;
            } else {
                System.err.println("Download failed for " + fileName + ". HTTP Status Code: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("An exception occurred during download of " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the file name from a URL string.
     *
     * @param url The full URL.
     * @return The derived file name.
     */
    private static String getFileNameFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            // Get the last segment of the path after the last '/'
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            // Fallback: If URI parsing fails, use the whole URL hash for a unique name
            return "download_" + url.hashCode();
        }
    }
}
