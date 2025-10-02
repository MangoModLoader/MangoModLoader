package org.mangorage.example;

import org.json.JSONWriter;
import org.mangorage.paradise.IMod;

import java.io.IOException;

public class ExampleMod implements IMod {
    public ExampleMod() {
        System.out.println("LOADED MOD");
        JSONWriter writer = new JSONWriter(System.out);

        writer.object()
                .key("amount")
                .value(100)
                .endObject();
    }
}
