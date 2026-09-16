package Che.tronweapons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the disc in world space instead of using the camera-facing thrown-item billboard. */
public class IdentityDiscRenderer extends EntityRenderer<IdentityDiscEntity> {
    private final ItemRenderer itemRenderer;

    public IdentityDiscRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(IdentityDiscEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // The generated item texture lies in its local XY plane. Lay it flat in XZ,
        // then spin about its OWN normal (local Z), not the camera or flight axis.
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partialTicks) * 45.0F));
        poseStack.scale(1.55F, 1.55F, 1.55F);

        ItemStack disc = entity.getItem();
        itemRenderer.renderStatic(disc, ItemDisplayContext.FIXED, 0x00F000F0,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(IdentityDiscEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
