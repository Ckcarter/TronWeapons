package Che.tronweapons;

import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import java.util.List;

/** Uses vanilla shield blocking, durability, enchantments and projectile defense. */
public class EnergyShieldItem extends ShieldItem {
    public EnergyShieldItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.tronweapons.energy_shield.tooltip"));
    }
}
