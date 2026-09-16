package Che.tronweapons;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Straight-moving energy bolt with a non-block-damaging area-of-effect impact. */
public class RecognizerBlastEntity extends ThrowableItemProjectile {
    private static final DustParticleOptions BLUE = new DustParticleOptions(new Vector3f(0.08F, 0.7F, 1.0F), 1.7F);
    private boolean impacted;

    public RecognizerBlastEntity(EntityType<? extends RecognizerBlastEntity> type, Level level) { super(type, level); }
    public RecognizerBlastEntity(Level level, LivingEntity owner) { super(Tronweapons.RECOGNIZER_BLAST_ENTITY.get(), owner, level); }

    @Override protected Item getDefaultItem() { return Tronweapons.RECOGNIZER_BOLT.get(); }
    @Override protected float getGravity() { return 0.0F; }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 velocity = getDeltaMovement();
            for (int i = 0; i < 3; i++) {
                double fraction = i / 3.0D;
                level().addParticle(BLUE, getX() - velocity.x * fraction, getY() - velocity.y * fraction,
                        getZ() - velocity.z * fraction, 0, 0, 0);
            }
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
        }
        if (!level().isClientSide && tickCount > 70) discard();
    }

    @Override protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        detonate();
    }

    @Override protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        detonate();
    }

    private void detonate() {
        if (level().isClientSide || impacted) return;
        impacted = true;
        Entity owner = getOwner();
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(3.0D), e -> e.isAlive() && e != owner)) {
            double distance = target.position().distanceTo(position());
            if (distance <= 3.5D) {
                target.hurt(damageSources().indirectMagic(this, owner), (float)(12.0D * (1.0D - distance / 5.0D)));
                Vec3 push = target.position().subtract(position());
                if (push.lengthSqr() > 0.001D) target.push(push.normalize().x * 0.65D, 0.25D, push.normalize().z * 0.65D);
            }
        }
        if (level() instanceof ServerLevel server) {
            server.sendParticles(BLUE, getX(), getY(), getZ(), 65, 1.25D, 1.25D, 1.25D, 0.16D);
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 25, 0.9D, 0.9D, 0.9D, 0.12D);
        }
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.85F, 1.45F);
        discard();
    }
}
