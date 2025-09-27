package com.example.dominionrising.forge.entity;

import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.unit.NationUnit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Forge implementation of NationUnit as a Minecraft entity
 * Uses Villager-like appearance as placeholder
 */
public class UnitEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> UNIT_TYPE = SynchedEntityData.defineId(UnitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> OWNER_NATION = SynchedEntityData.defineId(UnitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> UNIT_LEVEL = SynchedEntityData.defineId(UnitEntity.class, EntityDataSerializers.INT);
    
    private UUID unitId;
    private NationUnit unitData;

    public UnitEntity(EntityType<? extends UnitEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(UNIT_TYPE, "soldier");
        builder.define(OWNER_NATION, "");
        builder.define(UNIT_LEVEL, 1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new FollowOwnerNationPlayersGoal(this, 1.0D, 10.0F, 3.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    /**
     * Initialize this entity with unit data
     */
    public void initializeUnit(NationUnit unit) {
        this.unitData = unit;
        this.unitId = unit.getId();
        
        // Sync data to client
        this.entityData.set(UNIT_TYPE, unit.getType());
        this.entityData.set(OWNER_NATION, unit.getOwnerNation());
        this.entityData.set(UNIT_LEVEL, unit.getLevel());
        
        // Update entity attributes based on unit data
        updateAttributesFromUnit();
        
        // Set custom name
        this.setCustomName(net.minecraft.network.chat.Component.literal(
                unit.getType() + " (Lv." + unit.getLevel() + ")"
        ));
        this.setCustomNameVisible(true);
    }

    /**
     * Update entity attributes from unit data
     */
    private void updateAttributesFromUnit() {
        if (unitData == null) return;
        
        // Update health
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(unitData.getMaxHealth());
        this.setHealth(unitData.getHealth());
        
        // Update attack damage
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(unitData.getAttackDamage());
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (unitData != null) {
            boolean survived = unitData.takeDamage((int) amount);
            if (!survived) {
                // Unit died, remove from world
                this.remove(RemovalReason.KILLED);
                return true;
            }
            // Update entity health to match unit data
            this.setHealth(unitData.getHealth());
        }
        return super.hurt(damageSource, amount);
    }

    /**
     * Check if a player belongs to this unit's nation
     */
    public boolean isPlayerFromSameNation(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        
        NationManager manager = NationManager.getInstance();
        Nation playerNation = manager.getPlayerNation(serverPlayer.getUUID());
        
        if (playerNation == null) return false;
        
        return playerNation.getName().equals(getOwnerNation());
    }

    public String getUnitType() {
        return this.entityData.get(UNIT_TYPE);
    }

    public String getOwnerNation() {
        return this.entityData.get(OWNER_NATION);
    }

    public int getUnitLevel() {
        return this.entityData.get(UNIT_LEVEL);
    }

    public UUID getUnitId() {
        return unitId;
    }

    @Nullable
    public NationUnit getUnitData() {
        return unitData;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("UnitType", getUnitType());
        compound.putString("OwnerNation", getOwnerNation());
        compound.putInt("UnitLevel", getUnitLevel());
        if (unitId != null) {
            compound.putUUID("UnitId", unitId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(UNIT_TYPE, compound.getString("UnitType"));
        this.entityData.set(OWNER_NATION, compound.getString("OwnerNation"));
        this.entityData.set(UNIT_LEVEL, compound.getInt("UnitLevel"));
        if (compound.hasUUID("UnitId")) {
            this.unitId = compound.getUUID("UnitId");
            // Try to reconnect to unit data
            if (!level().isClientSide) {
                // TODO: Reconnect to UnitManager data on world load
            }
        }
    }

    /**
     * Custom AI goal to follow nation players
     */
    private static class FollowOwnerNationPlayersGoal extends Goal {
        private final UnitEntity unit;
        private final double speedModifier;
        private final float stopDistance;
        private final float startDistance;
        private Player closestNationPlayer;

        public FollowOwnerNationPlayersGoal(UnitEntity unit, double speedModifier, float startDistance, float stopDistance) {
            this.unit = unit;
            this.speedModifier = speedModifier;
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
        }

        @Override
        public boolean canUse() {
            Player closest = unit.level().getNearestPlayer(unit.getX(), unit.getY(), unit.getZ(), startDistance, false);
            if (closest != null && unit.isPlayerFromSameNation(closest)) {
                this.closestNationPlayer = closest;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return closestNationPlayer != null && 
                   !closestNationPlayer.isRemoved() && 
                   unit.distanceTo(closestNationPlayer) < startDistance &&
                   unit.isPlayerFromSameNation(closestNationPlayer);
        }

        @Override
        public void start() {
            // Empty
        }

        @Override
        public void stop() {
            this.closestNationPlayer = null;
            unit.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (closestNationPlayer != null) {
                unit.getLookControl().setLookAt(closestNationPlayer, 10.0F, unit.getMaxHeadXRot());
                if (unit.distanceTo(closestNationPlayer) > stopDistance) {
                    unit.getNavigation().moveTo(closestNationPlayer, speedModifier);
                } else {
                    unit.getNavigation().stop();
                }
            }
        }
    }
}