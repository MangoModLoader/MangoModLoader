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
        final List<FrameData> idleFrames = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            idleFrames.add(
                    new FrameData(
                            "assets/textures/idle.png",
                            new Pos(36 + (i * 96), 48),
                            20, 34
                    )
            );
        }

        final List<FrameData> runFrames = new ArrayList<>();
        runFrames.addAll(
                List.of(
                        new FrameData(
                            "assets/textures/run.png",
                            Pos.of(39, 52),
                            26, 29
                        ),
                        new FrameData(
                            "assets/textures/run.png",
                            Pos.of(131, 52),
                            30, 29
                        ),
                        new FrameData(
                                "assets/textures/run.png",
                                Pos.of(227, 51),
                                32, 27
                        ),
                        new FrameData(
                                "assets/textures/run.png",
                                Pos.of(324, 50),
                                32, 26
                        ),
                        new FrameData(
                                "assets/textures/run.png",
                                Pos.of(421, 50),
                                32, 28
                        ),
                        new FrameData(
                                "assets/textures/run.png",
                                Pos.of(518, 52),
                                30, 29
                        )
                )
        );

        SpriteData dataIdle = new SpriteData(
                "wizard/idle",
                5L,
                idleFrames
        );

        SpriteData dataRun = new SpriteData(
                "wizard/run",
                5L,
                runFrames
        );

        System.out.println(
                GSON.toJson(dataRun)
        );

    }
}
