package org.mangorage.loader.internal.minecraft;

import com.google.gson.Gson;
import net.minecraftforge.util.data.json.MinecraftVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public final class MinecraftFetcher {
    public static String readUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int status = connection.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Failed to fetch URL: " + status);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        connection.disconnect();
        return content.toString();
    }

    public static MinecraftVersion fetch(String url) {
        Gson gson = new Gson();
        final MinecraftVersion a;
        try {
            a = gson.fromJson(
                    readUrl(url),
                    MinecraftVersion.class
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return a;
    }


    public static void downloadLibraries(MinecraftVersion version) {

    }

    public static void main(String[] args) {
        final var MC = fetch("https://piston-meta.mojang.com/v1/packages/d7a33415a8e68a8fdff87ab2020e64de021df302/1.21.9.json");
        MinecraftFetcher.downloadLibraries(MC);
    }

}
