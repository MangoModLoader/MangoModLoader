package org.mangorage.paradise.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mangorage.paradise.core.asset.sprite.FrameData;
import org.mangorage.paradise.core.asset.sprite.SpriteData;
import org.mangorage.paradise.core.util.Pos;

import java.util.ArrayList;
import java.util.List;

public final class DataGenerator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        final List<FrameData> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            list.add(
                    new FrameData(
                            "assets/textures/idle.png",
                            new Pos(36 + (i * 96), 48),
                            20, 34
                    )
            );
        }


        SpriteData data = new SpriteData(
                "idle",
                5L,
                list
        );

        System.out.println(
                GSON.toJson(data)
        );


    }
}
