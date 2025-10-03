package org.mangorage.example;

import org.json.JSONWriter;
import org.mangorage.loader.api.mod.IModContainer;

public class ExampleMod implements IModContainer<Object> {

    public ExampleMod() {
        var buffer = new StringBuilder();
        JSONWriter writer = new JSONWriter(buffer);

        writer.object()
                .key("amount")
                .value(100)
                .endObject();

        System.out.println(buffer);
    }

    public void init() {
        System.out.println("Mod Init");
    }

    @Override
    public Object getInstance() {
        return null;
    }

    @Override
    public String getId() {
        return "example";
    }
}
