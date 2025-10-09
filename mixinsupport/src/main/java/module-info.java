import org.mangorage.loader.api.IClassTransformer;
import org.mangorage.loader.api.IModuleConfigurator;
import org.mangorage.loader.api.mod.IModContainer;
import org.mangorage.mangomodloaderspongemixinsupport.mixin.services.MangoModLoaderMixinBlackboardImpl;
import org.mangorage.mangomodloaderspongemixinsupport.mod.MangoModLoaderSpongeMixinSupport;
import org.mangorage.mangomodloaderspongemixinsupport.services.MangoModLoaderModuleConfigService;
import org.mangorage.mangomodloaderspongemixinsupport.services.MangoModLoaderSpongeMixinClassTransformerImpl;

module org.mangorage.mangomodloadermixinsupport {
    requires org.mangorage.mangomodloader;
    requires org.spongepowered.mixin;
    requires mixinextras.common;

    exports org.mangorage.mangomodloaderspongemixinsupport.mod to org.mangorage.mangomodloader;
    exports org.mangorage.mangomodloaderspongemixinsupport.mixin to org.spongepowered.mixin;
    exports org.mangorage.mangomodloaderspongemixinsupport.mixin.services to org.spongepowered.mixin;
    exports org.mangorage.mangomodloaderspongemixinsupport.mixin.core to org.spongepowered.mixin;

    exports org.mangorage.mangomodloaderspongemixinsupport.services to org.spongepowered.mixin;


    // Loader
    uses IModuleConfigurator;
    uses IClassTransformer;
    uses IModContainer;

    // Mixin
    uses org.spongepowered.asm.service.IGlobalPropertyService;

    // Loader Services
    provides IModuleConfigurator with MangoModLoaderModuleConfigService;
    provides IClassTransformer with MangoModLoaderSpongeMixinClassTransformerImpl;
    provides IModContainer with MangoModLoaderSpongeMixinSupport;

    // Mixin Service
    provides org.spongepowered.asm.service.IGlobalPropertyService with MangoModLoaderMixinBlackboardImpl;
}