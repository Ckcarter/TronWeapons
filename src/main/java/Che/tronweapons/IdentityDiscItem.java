package Che.tronweapons;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class IdentityDiscItem extends SwordItem {

    public static final String DISC_OUT_TAG = "tronweapons_identity_disc_out";

    public IdentityDiscItem(Properties properties) {
        super(Tiers.DIAMOND, 5, -2.15F, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Only one Identity Disc may be in flight for this player at a time.
        // This also prevents Creative mode from throwing unlimited copies.
        if (player.getPersistentData().getBoolean(DISC_OUT_TAG)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            IdentityDiscEntity disc = new IdentityDiscEntity(level, player);
            disc.setItem(stack.copy());

            disc.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    2.25F,
                    0.0F
            );

            level.addFreshEntity(disc);

            // Lock the player's disc until this projectile comes back.
            player.getPersistentData().putBoolean(DISC_OUT_TAG, true);

            // In Survival the physical disc leaves the hand.
            // Creative keeps the item visually, but the lock still prevents another throw.
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.TRIDENT_THROW,
                SoundSource.PLAYERS,
                0.85F,
                1.55F
        );

        player.getCooldowns().addCooldown(this, 7);
        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
