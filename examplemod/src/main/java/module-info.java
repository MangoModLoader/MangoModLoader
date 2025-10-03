module examplemod {
    requires minersparadise;
    requires org.json;
    requires loader;

    opens org.mangorage.example to loader;

    provides org.mangorage.loader.api.mod.IModContainer with org.mangorage.example.ExampleMod;
}