package org.mangorage.example.mixin;


import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = LivingEntity.class)
public class MainMixin {


    @Overwrite
    public void tick() {
        System.out.println("TICKING ANIMAL");
    }
}
