package Che.tronweapons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class IdentityDiscRenderer extends ThrownItemRenderer<IdentityDiscEntity> {

    public IdentityDiscRenderer(EntityRendererProvider.Context context) {
        // Larger and full-bright so the cyan circuitry stays vivid in dark areas.
        super(context, 1.55F, true);
    }

    @Override
    public void render(
            IdentityDiscEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();

        // Tilt the item into a disc-like flight plane and spin it extremely fast.
        poseStack.mulPose(Axis.XP.rotationDegrees(72.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partialTicks) * 52.0F));

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}
