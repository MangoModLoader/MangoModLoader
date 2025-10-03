import org.mangorage.loader.api.IClassTransformer;
import org.mangorage.loader.api.IModuleConfigurator;
import org.mangorage.loader.api.mod.IModContainer;
import org.mangorage.mixinsupport.mixin.services.MangoBotMixinBlackboardImpl;

open module mixinsupport {
    requires loader;
    requires org.spongepowered.mixin;
    requires mixinextras.common;

    // Loader
    uses IModuleConfigurator;
    uses IClassTransformer;
    uses IModContainer;

    // Mixin
    uses org.spongepowered.asm.service.IGlobalPropertyService;

    // Loader Services
    provides IModuleConfigurator with org.mangorage.mixinsupport.services.ModuleConfigService;
    provides IClassTransformer with org.mangorage.mixinsupport.services.SpongeMixinClassTransformerImpl;
    provides IModContainer with org.mangorage.mixinsupport.plugin.MangoBotMixinPlugin;

    // Mixin Service
    provides org.spongepowered.asm.service.IGlobalPropertyService with MangoBotMixinBlackboardImpl;
}