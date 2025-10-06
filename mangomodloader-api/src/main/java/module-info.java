module org.mangorage.mangomodloader {
    requires java.desktop;
    requires java.net.http;
    requires java.management;
    requires java.naming;

    requires com.google.gson;
    requires net.minecraftforge.utils.json_data;

    exports org.mangorage.loader.api;
    exports org.mangorage.loader.api.mod;

    opens org.mangorage.loader;

    opens org.mangorage.loader.internal.minecraft to com.google.gson;

    uses org.mangorage.loader.api.IClassTransformer;
    uses org.mangorage.loader.api.IModuleConfigurator;
    uses org.mangorage.loader.api.mod.IModContainer;
}