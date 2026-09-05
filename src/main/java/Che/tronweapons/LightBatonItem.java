package Che.tronweapons;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class LightBatonItem extends SwordItem {

    public LightBatonItem(Properties properties) {
        // Fast, strong melee profile: diamond tier, +4 damage, very fast attack speed.
        super(Tiers.DIAMOND, 4, -1.7F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (!attacker.level().isClientSide) {
            target.knockback(
                    0.9D,
                    attacker.getX() - target.getX(),
                    attacker.getZ() - target.getZ()
            );

            attacker.level().playSound(
                    null,
                    target.blockPosition(),
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.PLAYERS,
                    0.45F,
                    1.9F
            );

            if (attacker.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        target.getX(),
                        target.getY() + target.getBbHeight() * 0.5D,
                        target.getZ(),
                        14,
                        0.25D,
                        0.35D,
                        0.25D,
                        0.04D
                );

                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        target.getX(),
                        target.getY() + target.getBbHeight() * 0.5D,
                        target.getZ(),
                        5,
                        0.16D,
                        0.24D,
                        0.16D,
                        0.015D
                );
            }
        }

        return result;
    }
}
