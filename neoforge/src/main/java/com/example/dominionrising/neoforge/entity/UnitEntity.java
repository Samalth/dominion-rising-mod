package com.example.dominionrising.neoforge.entity;

import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.unit.NationUnit;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * NeoForge implementation of NationUnit as a Minecraft entity
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
        this.goalSelector.addGoal(3, new UnitAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new UnitDefendGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new FollowOwnerNationPlayersGoal(this, 1.0D, 10.0F, 3.0F));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new UnitTargetGoal(this));
        this.targetSelector.addGoal(3, new DefendTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
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
        
        // Set custom name with nation information
        String displayName = "§6[" + unit.getOwnerNation() + "]§r " + 
                            unit.getType() + " §7(Lv." + unit.getLevel() + ")";
        this.setCustomName(net.minecraft.network.chat.Component.literal(displayName));
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
     * Custom AI goal to attack specific targets based on unit tactical state
     */
    private static class UnitAttackGoal extends MeleeAttackGoal {
        private final UnitEntity unit;

        public UnitAttackGoal(UnitEntity unit, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(unit, speedModifier, followingTargetEvenIfNotSeen);
            this.unit = unit;
        }

        @Override
        public boolean canUse() {
            // Only attack if unit has attack target and is in ATTACKING state
            if (unit.unitData == null) return false;
            if (unit.unitData.getCurrentState() != NationUnit.UnitState.ATTACKING) return false;
            
            UUID targetId = unit.unitData.getAttackTarget();
            if (targetId == null) return false;
            
            // Find target entity by UUID
            LivingEntity target = findEntityByUUID(targetId);
            if (target != null && target.isAlive()) {
                unit.setTarget(target);
                return super.canUse();
            }
            
            return false;
        }

        private LivingEntity findEntityByUUID(UUID targetId) {
            // Use server level entity iteration
            if (unit.level().isClientSide) return null;
            
            for (Entity entity : unit.level().getEntitiesOfClass(LivingEntity.class, 
                    unit.getBoundingBox().inflate(32.0D))) {
                if (entity.getUUID().equals(targetId)) {
                    return (LivingEntity) entity;
                }
            }
            return null;
        }
    }

    /**
     * Custom AI goal to defend specific positions based on unit tactical state
     * Units stay within 35 blocks of defend position and return to origin when not fighting
     */
    private static class UnitDefendGoal extends Goal {
        private final UnitEntity unit;
        private final double speedModifier;
        private BlockPos defendPosition;
        private static final double MAX_DEFEND_DISTANCE = 35.0D; // 35 block radius
        private static final double MAX_DEFEND_DISTANCE_SQ = MAX_DEFEND_DISTANCE * MAX_DEFEND_DISTANCE;

        public UnitDefendGoal(UnitEntity unit, double speedModifier) {
            this.unit = unit;
            this.speedModifier = speedModifier;
        }

        @Override
        public boolean canUse() {
            if (unit.unitData == null) return false;
            if (unit.unitData.getCurrentState() != NationUnit.UnitState.DEFENDING) return false;
            
            // Get defend position
            Vec3 defendPos = new Vec3(unit.unitData.getDefendX(), unit.unitData.getDefendY(), unit.unitData.getDefendZ());
            if (defendPos.x == 0 && defendPos.y == 0 && defendPos.z == 0) return false;
            
            this.defendPosition = BlockPos.containing(defendPos);
            double distanceToDefendPos = unit.distanceToSqr(defendPos);
            
            // If unit is outside the 35-block radius, force return to defend position
            if (distanceToDefendPos > MAX_DEFEND_DISTANCE_SQ) {
                return true; // Force return regardless of combat state
            }
            
            // If not fighting and far from defend position, return to it
            if (unit.getTarget() == null && distanceToDefendPos > 16.0D) {
                return true;
            }
            
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (unit.unitData == null || unit.unitData.getCurrentState() != NationUnit.UnitState.DEFENDING) {
                return false;
            }
            
            if (defendPosition == null) return false;
            
            Vec3 defendPos = Vec3.atCenterOf(defendPosition);
            double distanceToDefendPos = unit.distanceToSqr(defendPos);
            
            // Always continue if outside max range (priority override)
            if (distanceToDefendPos > MAX_DEFEND_DISTANCE_SQ) {
                return true;
            }
            
            // If we have a target but are too far from defend position, return to defend position
            if (unit.getTarget() != null && distanceToDefendPos > MAX_DEFEND_DISTANCE_SQ) {
                unit.setTarget(null); // Drop target and return
                return true;
            }
            
            // Continue returning to position if no target and still far away
            return unit.getTarget() == null && distanceToDefendPos > 4.0D;
        }

        @Override
        public void start() {
            if (defendPosition != null) {
                // Cancel any current attack to return to position if outside bounds
                Vec3 defendPos = Vec3.atCenterOf(defendPosition);
                if (unit.distanceToSqr(defendPos) > MAX_DEFEND_DISTANCE_SQ) {
                    unit.setTarget(null);
                }
                unit.getNavigation().moveTo(defendPosition.getX(), defendPosition.getY(), defendPosition.getZ(), speedModifier);
            }
        }

        @Override
        public void stop() {
            unit.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (defendPosition == null) return;
            
            Vec3 defendPos = Vec3.atCenterOf(defendPosition);
            double distanceToDefendPos = unit.distanceToSqr(defendPos);
            
            // Priority: Return to defend position if outside max range
            if (distanceToDefendPos > MAX_DEFEND_DISTANCE_SQ) {
                unit.setTarget(null); // Drop any target
                unit.getNavigation().moveTo(defendPosition.getX(), defendPosition.getY(), defendPosition.getZ(), speedModifier);
                return;
            }
            
            // Normal behavior: return to position when not fighting and far away
            if (unit.getTarget() == null && distanceToDefendPos > 4.0D) {
                unit.getNavigation().moveTo(defendPosition.getX(), defendPosition.getY(), defendPosition.getZ(), speedModifier);
            }
        }
    }

    /**
     * Custom target selector for unit tactical behavior
     */
    private static class UnitTargetGoal extends Goal {
        private final UnitEntity unit;

        public UnitTargetGoal(UnitEntity unit) {
            this.unit = unit;
        }

        @Override
        public boolean canUse() {
            if (unit.unitData == null) return false;
            if (unit.unitData.getCurrentState() != NationUnit.UnitState.ATTACKING) return false;
            
            UUID targetId = unit.unitData.getAttackTarget();
            if (targetId == null) return false;
            
            // Find and set target
            LivingEntity target = findEntityByUUID(targetId);
            if (target != null && target.isAlive()) {
                unit.setTarget(target);
                return true;
            }
            
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return unit.getTarget() != null && 
                   unit.getTarget().isAlive() && 
                   unit.unitData != null && 
                   unit.unitData.getCurrentState() == NationUnit.UnitState.ATTACKING;
        }

        private LivingEntity findEntityByUUID(UUID targetId) {
            // Use server level entity iteration
            if (unit.level().isClientSide) return null;
            
            for (Entity entity : unit.level().getEntitiesOfClass(LivingEntity.class, 
                    unit.getBoundingBox().inflate(32.0D))) {
                if (entity.getUUID().equals(targetId)) {
                    return (LivingEntity) entity;
                }
            }
            return null;
        }
    }

    /**
     * Custom target selector for defending against hostile mobs
     */
    private static class DefendTargetGoal extends Goal {
        private final UnitEntity unit;
        private LivingEntity hostileTarget;

        public DefendTargetGoal(UnitEntity unit) {
            this.unit = unit;
        }

        @Override
        public boolean canUse() {
            if (unit.unitData == null) return false;
            if (unit.unitData.getCurrentState() != NationUnit.UnitState.DEFENDING) return false;
            
            // Look for hostile entities around unit's current position (within defend range)
            Vec3 defendPos = new Vec3(unit.unitData.getDefendX(), unit.unitData.getDefendY(), unit.unitData.getDefendZ());
            if (defendPos.x == 0 && defendPos.y == 0 && defendPos.z == 0) return false;
            
            // Only defend if we're within the 35-block defend boundary
            double maxDefendDistanceSq = 35.0D * 35.0D;
            if (unit.distanceToSqr(defendPos) > maxDefendDistanceSq) return false;
            
            // Find hostile entities within 12 blocks of the unit's current position
            // But also ensure they're within the 35-block defend boundary
            List<LivingEntity> hostileEntities = unit.level().getEntitiesOfClass(LivingEntity.class,
                unit.getBoundingBox().inflate(12.0D),
                entity -> {
                    // Don't target self
                    if (entity == unit) return false;
                    
                    // Only target entities within the defend boundary (35 blocks from defend position)
                    if (entity.distanceToSqr(defendPos) > maxDefendDistanceSq) {
                        return false;
                    }
                    
                    // Target hostile mobs (monsters)
                    if (entity instanceof net.minecraft.world.entity.monster.Monster) {
                        return true;
                    }
                    // Target players from other nations or players without a nation
                    if (entity instanceof Player player) {
                        return !unit.isPlayerFromSameNation(player);
                    }
                    // Don't target other units or neutral entities
                    return false;
                }
            );
            
            if (!hostileEntities.isEmpty()) {
                // Find the closest hostile entity to the unit
                LivingEntity closest = null;
                double closestDistance = Double.MAX_VALUE;
                
                for (LivingEntity entity : hostileEntities) {
                    double distance = unit.distanceToSqr(entity);
                    if (distance < closestDistance) {
                        closest = entity;
                        closestDistance = distance;
                    }
                }
                
                if (closest != null) {
                    this.hostileTarget = closest;
                    unit.setTarget(closest);
                    return true;
                }
            }
            
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (hostileTarget == null || !hostileTarget.isAlive()) return false;
            if (unit.unitData == null || unit.unitData.getCurrentState() != NationUnit.UnitState.DEFENDING) return false;
            
            // Check if we're still within the 35-block defend boundary
            Vec3 defendPos = new Vec3(unit.unitData.getDefendX(), unit.unitData.getDefendY(), unit.unitData.getDefendZ());
            double maxDefendDistanceSq = 35.0D * 35.0D;
            
            // Stop targeting if either the unit or the target moved outside the defend boundary
            if (unit.distanceToSqr(defendPos) > maxDefendDistanceSq || 
                hostileTarget.distanceToSqr(defendPos) > maxDefendDistanceSq) {
                return false;
            }
            
            // Continue if target is within combat range
            return unit.distanceToSqr(hostileTarget) < 256.0D; // 16 block range
        }

        @Override
        public void stop() {
            this.hostileTarget = null;
            if (unit.unitData != null && unit.unitData.getCurrentState() == NationUnit.UnitState.DEFENDING) {
                unit.setTarget(null); // Clear target but stay in defending mode
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
            // Only follow players when in IDLE state
            if (unit.unitData != null && unit.unitData.getCurrentState() != NationUnit.UnitState.IDLE) {
                return false;
            }
            
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
                   unit.isPlayerFromSameNation(closestNationPlayer) &&
                   (unit.unitData == null || unit.unitData.getCurrentState() == NationUnit.UnitState.IDLE);
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