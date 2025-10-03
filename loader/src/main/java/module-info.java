module loader {
    requires java.desktop;
    requires java.naming;
    requires com.google.gson;

    exports org.mangorage.loader.api;
    exports org.mangorage.loader.api.mod;

    opens org.mangorage.loader;

    uses org.mangorage.loader.api.IClassTransformer;
    uses org.mangorage.loader.api.IModuleConfigurator;
    uses org.mangorage.loader.api.mod.IModContainer;
}