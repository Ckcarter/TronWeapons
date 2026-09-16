package Che.tronweapons;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class IdentityDiscEntity extends ThrowableItemProjectile {

    private boolean returning = false;
    private int flightTicks = 0;
    private int ricochets = 0;
    private Vec3 straightVelocity = Vec3.ZERO;

    public void setStraightFlight(Vec3 direction) {
        straightVelocity = direction.normalize().scale(2.25D);
        setNoGravity(true);
        setDeltaMovement(straightVelocity);
    }

    public IdentityDiscEntity(EntityType<? extends IdentityDiscEntity> type, Level level) {
        super(type, level);
    }

    public IdentityDiscEntity(Level level, LivingEntity owner) {
        super(Tronweapons.IDENTITY_DISC_ENTITY.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Tronweapons.IDENTITY_DISC.get();
    }

    @Override
    public void tick() {
        // Preserve the original throw direction and speed; vanilla projectile drag
        // must not bend, slow, or drop the disc during its outbound flight.
        if (!returning && straightVelocity.lengthSqr() > 0.0D) {
            setNoGravity(true);
            setDeltaMovement(straightVelocity);
        }
        super.tick();
        flightTicks++;
        if (!returning && straightVelocity.lengthSqr() > 0.0D) {
            setDeltaMovement(straightVelocity);
        }

        // Movie-style glowing trail.
        if (level().isClientSide) {
            Vec3 motion = getDeltaMovement();

            // Bright retro-blue light trail behind the 1982 Identity Disc.
            DustParticleOptions blueGlow =
                    new DustParticleOptions(new Vector3f(0.15F, 0.75F, 1.0F), 1.35F);

            for (int i = 0; i < 4; i++) {
                double offset = 0.16D + (i * 0.11D);

                double px = getX() - motion.x * offset;
                double py = getY() - motion.y * offset;
                double pz = getZ() - motion.z * offset;

                level().addParticle(
                        blueGlow,
                        px, py, pz,
                        0.0D, 0.0D, 0.0D
                );
            }

            // A smaller white-blue core makes the trail look luminous.
            if ((flightTicks & 1) == 0) {
                level().addParticle(
                        ParticleTypes.END_ROD,
                        getX() - motion.x * 0.12D,
                        getY() - motion.y * 0.12D,
                        getZ() - motion.z * 0.12D,
                        0.0D, 0.0D, 0.0D
                );
            }
        }

        Entity ownerEntity = getOwner();
        if (!(ownerEntity instanceof Player player)) {
            if (!level().isClientSide && flightTicks > 200) {
                discard();
            }
            return;
        }

        // If the owner dies, do not leave their one-disc lock stuck forever.
        if (!player.isAlive()) {
            if (!level().isClientSide) {
                unlockOwner(player);
                discard();
            }
            return;
        }

        // If it misses everything, begin a direct return after a short flight.
        if (flightTicks >= 24) {
            returning = true;
        }

        if (!returning) {
            return;
        }

        setNoGravity(true);

        // Aim for the player's catching hand/chest area rather than the feet.
        Vec3 target = player.getEyePosition().add(0.0D, -0.55D, 0.0D);
        Vec3 toOwner = target.subtract(position());
        double distance = toOwner.length();

        if (distance < 1.25D) {
            if (!level().isClientSide) {
                returnDiscToPlayer(player);
            }
            return;
        }

        // Straight return: point directly at the owner with no boomerang-style side swing.
        Vec3 desiredVelocity = toOwner.normalize().scale(1.35D);
        setDeltaMovement(desiredVelocity);
    }

    private void returnDiscToPlayer(Player player) {
        ItemStack returnedStack = getItem().copy();

        if (returnedStack.isEmpty()) {
            returnedStack = new ItemStack(Tronweapons.IDENTITY_DISC.get());
        }

        if (!player.getAbilities().instabuild) {
            if (!player.getInventory().add(returnedStack)) {
                player.drop(returnedStack, false);
            }
        }

        level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.65F,
                1.85F
        );

        // The disc is back in the owner's possession, so another throw is allowed.
        unlockOwner(player);
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity target = hitResult.getEntity();
        Entity ownerEntity = getOwner();

        // The owner's own returning disc is safe to catch.
        if (target == ownerEntity) {
            return;
        }

        super.onHitEntity(hitResult);

        if (!level().isClientSide) {
            target.hurt(damageSources().thrown(this, ownerEntity), 12.0F);

            level().playSound(
                    null,
                    blockPosition(),
                    SoundEvents.TRIDENT_HIT,
                    SoundSource.PLAYERS,
                    0.9F,
                    1.55F
            );
        }

        // Movie discs turn back toward the user after landing a hit.
        returning = true;
        setNoGravity(true);
        setDeltaMovement(getDeltaMovement().scale(-0.25D));
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);

        if (returning) {
            return;
        }

        Vec3 motion = getDeltaMovement();
        Direction face = hitResult.getDirection();

        Vec3 bounced;
        switch (face.getAxis()) {
            case X -> bounced = new Vec3(-motion.x, motion.y, motion.z);
            case Y -> bounced = new Vec3(motion.x, -motion.y, motion.z);
            case Z -> bounced = new Vec3(motion.x, motion.y, -motion.z);
            default -> bounced = motion.scale(-1.0D);
        }

        ricochets++;

        setPos(
                hitResult.getLocation().x + face.getStepX() * 0.08D,
                hitResult.getLocation().y + face.getStepY() * 0.08D,
                hitResult.getLocation().z + face.getStepZ() * 0.08D
        );

        straightVelocity = bounced.normalize().scale(2.25D);
        setDeltaMovement(straightVelocity);
        setNoGravity(true);

        if (!level().isClientSide) {
            level().playSound(
                    null,
                    blockPosition(),
                    SoundEvents.ANVIL_LAND,
                    SoundSource.PLAYERS,
                    0.45F,
                    1.9F
            );
        }

        // Let skilled throws bounce a couple of times before the automatic return.
        if (ricochets >= 3) {
            returning = true;
        }
    }

    @Override
    protected float getGravity() {
        // An energized disc flies almost flat like it does on the Grid.
        return 0.0F;
    }

    private void unlockOwner(Player player) {
        player.getPersistentData().putBoolean(IdentityDiscItem.DISC_OUT_TAG, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Returning", returning);
        tag.putInt("FlightTicks", flightTicks);
        tag.putInt("Ricochets", ricochets);
        tag.putDouble("StraightX", straightVelocity.x);
        tag.putDouble("StraightY", straightVelocity.y);
        tag.putDouble("StraightZ", straightVelocity.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        returning = tag.getBoolean("Returning");
        flightTicks = tag.getInt("FlightTicks");
        ricochets = tag.getInt("Ricochets");
        straightVelocity = new Vec3(tag.getDouble("StraightX"), tag.getDouble("StraightY"), tag.getDouble("StraightZ"));
    }
}
