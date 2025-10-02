module loader {
    requires java.desktop;
    requires java.naming;

    exports org.mangorage.boot.loader.api;
    exports org.mangorage.boot;

    uses org.mangorage.boot.loader.api.IClassTransformer;
    uses org.mangorage.boot.loader.api.IModuleConfigurator;
}