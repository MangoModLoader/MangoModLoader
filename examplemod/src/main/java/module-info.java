module examplemod {
    requires minersparadise;
    requires org.json;
    requires loader;
    requires static org.spongepowered.mixin;
    requires static joined;

    opens org.mangorage.example to loader;

    provides org.mangorage.loader.api.mod.IModContainer with org.mangorage.example.ExampleMod;
    provides org.mangorage.loader.api.IClassTransformer with org.mangorage.example.MyClassTransformer;
}