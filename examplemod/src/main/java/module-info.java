module examplemod {
    requires minersparadise;
    requires org.json;

    provides org.mangorage.paradise.IMod with org.mangorage.example.ExampleMod;
    uses org.mangorage.paradise.IMod;
}