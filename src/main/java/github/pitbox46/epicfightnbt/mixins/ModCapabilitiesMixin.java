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

@SuppressWarnings("rawtypes")

@Mixin(value = ModCapabilities.class, remap = false)
public class ModCapabilitiesMixin {
    @Inject(at = @At(value = "HEAD"), method = "getItemStackCapability(Lnet/minecraft/item/ItemStack;)Lmaninhouse/epicfight/capabilities/item/CapabilityItem;", cancellable = true)
    private static void onGetItemStackCapability(ItemStack stack, CallbackInfoReturnable<CapabilityItem> cir) {
        if(stack.isEmpty()) cir.setReturnValue( CapabilityItem.EMPTY );
        CapabilityItem cap = stack.getCapability(ModCapabilities.CAPABILITY_ITEM, null).orElse( CapabilityItem.EMPTY );
        if(cap == CapabilityItem.EMPTY) {
            cir.setReturnValue(Config.findWeaponByNBT(stack));
        }
    }
}