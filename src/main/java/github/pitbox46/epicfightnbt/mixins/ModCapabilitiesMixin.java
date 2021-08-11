package github.pitbox46.epicfightnbt.mixins;

import github.pitbox46.epicfightnbt.Config;
import maninhouse.epicfight.capabilities.ModCapabilities;
import maninhouse.epicfight.capabilities.item.CapabilityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModCapabilities.class, remap = false)
public class ModCapabilitiesMixin {
    @Inject(at = @At(value = "HEAD"), method = "stackCapabilityGetter(Lnet/minecraft/item/ItemStack;)Lmaninhouse/epicfight/capabilities/item/CapabilityItem;", cancellable = true)
    private static void onStackCapabilityGetter(ItemStack stack, CallbackInfoReturnable<CapabilityItem> cir) {
        if(stack.isEmpty()) cir.setReturnValue(null);
        CapabilityItem cap = stack.getCapability(ModCapabilities.CAPABILITY_ITEM, null).orElse(null);
        if(cap == null) {
            cir.setReturnValue(Config.findWeaponByNBT(stack));
        }
    }
}
