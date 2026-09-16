package Che.tronweapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** Right-click cycles compact baton -> energy sword -> extended staff. */
public class EnergyBatonItem extends SwordItem {
    private static final String MODE_TAG = "EnergyBatonMode";
    private static final UUID DAMAGE_ID = UUID.fromString("ad52f3d7-3b60-4a40-bf1b-725be7129b18");
    private static final UUID SPEED_ID = UUID.fromString("b22ab031-1f85-47b9-bd38-ff7e14cb5b42");
    private static final UUID REACH_ID = UUID.fromString("c88f7fcb-6b74-4f3b-9fcb-1387cfe28b43");

    public EnergyBatonItem(Properties properties) {
        super(Tiers.DIAMOND, 3, -2.0F, properties);
    }

    private static int mode(ItemStack stack) {
        return Math.floorMod(stack.getOrCreateTag().getInt(MODE_TAG), 3);
    }

    private static String modeName(int mode) {
        return switch (mode) {
            case 1 -> "Energy Sword";
            case 2 -> "Energy Staff";
            default -> "Compact Baton";
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int next = (mode(stack) + 1) % 3;
            stack.getOrCreateTag().putInt(MODE_TAG, next);
            player.displayClientMessage(Component.literal("Energy Baton: " + modeName(next)).withStyle(ChatFormatting.AQUA), true);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5F, 1.3F + next * 0.2F);
        }
        player.getCooldowns().addCooldown(this, 8);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);
        int mode = mode(stack);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributes = ImmutableMultimap.builder();
        // Diamond tier contributes 3 base attack damage; these bonuses yield 5 / 8 / 7 total weapon damage.
        attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_ID, "Energy baton damage", mode == 0 ? 2.0D : mode == 1 ? 5.0D : 4.0D, AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.ATTACK_SPEED, new AttributeModifier(SPEED_ID, "Energy baton speed", mode == 0 ? -1.4D : mode == 1 ? -2.2D : -2.7D, AttributeModifier.Operation.ADDITION));
        if (mode == 2) {
            attributes.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_ID, "Energy staff reach", 1.5D, AttributeModifier.Operation.ADDITION));
        }
        return attributes.build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && attacker.level() instanceof ServerLevel server) {
            int mode = mode(stack);
            if (mode == 2) target.knockback(1.0D, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), mode == 1 ? 24 : 12, 0.25, 0.35, 0.25, 0.04);
            server.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 5, 0.2, 0.2, 0.2, 0.01);
            server.playSound(null, target.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5F, 1.7F);
        }
        return hit;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal("Mode: " + modeName(mode(stack))).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Right-click to change modes").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
