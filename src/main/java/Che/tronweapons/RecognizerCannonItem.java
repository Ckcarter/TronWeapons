package Che.tronweapons;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Heavy, cooldown-limited ranged energy weapon. */
public class RecognizerCannonItem extends Item {
    public RecognizerCannonItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            RecognizerBlastEntity blast = new RecognizerBlastEntity(level, player);
            blast.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 0.0F);
            level.addFreshEntity(blast);
            if (!player.getAbilities().instabuild) stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.4F, 0.55F);
        player.getCooldowns().addCooldown(this, 35);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
