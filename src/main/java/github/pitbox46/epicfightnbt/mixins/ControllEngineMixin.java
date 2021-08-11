package github.pitbox46.epicfightnbt.mixins;

import github.pitbox46.epicfightnbt.Config;
import maninhouse.epicfight.animation.types.StaticAnimation;
import maninhouse.epicfight.capabilities.ModCapabilities;
import maninhouse.epicfight.capabilities.item.CapabilityItem;
import maninhouse.epicfight.client.capabilites.entity.ClientPlayerData;
import maninhouse.epicfight.client.events.engine.ControllEngine;
import maninhouse.epicfight.entity.eventlistener.EntityEventListener;
import maninhouse.epicfight.entity.eventlistener.PlayerBasicAttackEvent;
import maninhouse.epicfight.network.ModNetworkManager;
import maninhouse.epicfight.network.client.CTSPlayAnimation;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = ControllEngine.class, remap = false)
public class ControllEngineMixin {
    @Shadow private ClientPlayerData playerdata;

    @Shadow private ClientPlayerEntity player;

    @Shadow private int comboCounter;

    @Shadow private int comboHoldCounter;

    @Inject(at = @At(value = "HEAD"), method = "playAttackMotion", cancellable = true)
    private void onAccessFirstPlayerInPlayAttackMotion(ItemStack holdItem, boolean dashAttack, CallbackInfo ci) {
        if (!this.playerdata.getEventListener().activateEvents(EntityEventListener.EventType.BASIC_ATTACK_EVENT, new PlayerBasicAttackEvent(this.playerdata))) {
            CapabilityItem cap = holdItem.getCapability(ModCapabilities.CAPABILITY_ITEM, null).orElse(null);
            if(cap == null) cap = Config.findWeaponByNBT(holdItem);
            StaticAnimation attackMotion = null;
            if (this.player.getRidingEntity() != null) {
                if (this.player.isRidingHorse() && cap != null && cap.canUseOnMount() && cap.getMountAttackMotion() != null) {
                    attackMotion = cap.getMountAttackMotion().get(this.comboCounter);
                    ++this.comboCounter;
                    this.comboCounter %= cap.getMountAttackMotion().size();
                }
            } else {
                List<StaticAnimation> combo = cap != null ? cap.getAutoAttckMotion(this.playerdata) : CapabilityItem.getBasicAutoAttackMotion();
                int comboSize = combo.size();
                if (dashAttack) {
                    this.comboCounter = comboSize - 1;
                } else {
                    this.comboCounter %= comboSize - 1;
                }

                attackMotion = combo.get(this.comboCounter);
                this.comboCounter = dashAttack ? 0 : this.comboCounter + 1;
            }

            this.comboHoldCounter = 10;
            if (attackMotion != null) {
                this.playerdata.getAnimator().playAnimation(attackMotion, 0.0F);
                ModNetworkManager.sendToServer(new CTSPlayAnimation(attackMotion, 0.0F, false, false));
            }
        }
        ci.cancel();
    }
}
