module examplemod {
    requires org.json;
    requires org.mangorage.mangomodloader;
    requires static org.spongepowered.mixin;
    requires static minecraft;

    opens org.mangorage.example to loader;

    provides org.mangorage.loader.api.mod.IModContainer with org.mangorage.example.ExampleMod;
    provides org.mangorage.loader.api.IClassTransformer with org.mangorage.example.MyClassTransformer;
}