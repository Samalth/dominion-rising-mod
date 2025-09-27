package com.example.dominionrising.common.unit;

import java.util.UUID;

/**
 * Represents a nation unit with enhanced stats, tactical state, and persistence
 */
public class NationUnit {
    private final UUID id;
    private final String type;
    private int level;
    private int health;
    private int maxHealth;
    private int attackDamage;
    private int defense;
    private double attackSpeed;
    private final String ownerNation;
    private boolean alive;
    
    // Tactical state
    private UnitState currentState;
    private UUID attackTarget; // Target entity UUID
    private double defendX, defendY, defendZ; // Defend position
    private long stateChangeTime; // When state last changed
    
    // Experience and progression  
    private int experience;
    private int experienceToNextLevel;
    
    public enum UnitState {
        IDLE,       // Following nation players
        ATTACKING,  // Attacking specific target
        DEFENDING,  // Defending specific position
        RETURNING   // Returning to player after task
    }

    public NationUnit(String type, String ownerNation, int initialLevel) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.ownerNation = ownerNation;
        this.level = initialLevel;
        
        // Initialize stats based on type and level
        initializeStats();
        
        // Initialize tactical state
        this.currentState = UnitState.IDLE;
        this.attackTarget = null;
        this.defendX = this.defendY = this.defendZ = 0;
        this.stateChangeTime = System.currentTimeMillis();
        
        // Initialize experience
        this.experience = 0;
        this.experienceToNextLevel = calculateExperienceForNextLevel(level);
        
        this.alive = true;
    }
    
    /**
     * Constructor for loading from persistence data
     */
    public NationUnit(UUID id, String type, String ownerNation, int level, int health, int experience, 
                      UnitState state, UUID attackTarget, double defendX, double defendY, double defendZ) {
        this.id = id;
        this.type = type;
        this.ownerNation = ownerNation;
        this.level = level;
        this.experience = experience;
        
        // Initialize stats based on type and level
        initializeStats();
        
        // Set health (might be damaged)
        this.health = Math.min(health, this.maxHealth);
        this.alive = this.health > 0;
        
        // Restore tactical state
        this.currentState = state != null ? state : UnitState.IDLE;
        this.attackTarget = attackTarget;
        this.defendX = defendX;
        this.defendY = defendY;
        this.defendZ = defendZ;
        this.stateChangeTime = System.currentTimeMillis();
        
        this.experienceToNextLevel = calculateExperienceForNextLevel(level);
    }
    
    /**
     * Initialize unit stats based on type and level
     */
    private void initializeStats() {
        // Base stats per type
        switch (type.toLowerCase()) {
            case "soldier" -> {
                this.maxHealth = 25 + (level * 5);
                this.attackDamage = 6 + (level * 2);
                this.defense = 3 + level;
                this.attackSpeed = 1.0;
            }
            case "archer" -> {
                this.maxHealth = 20 + (level * 4);
                this.attackDamage = 5 + (level * 2);
                this.defense = 2 + level;
                this.attackSpeed = 0.8;
            }
            case "knight" -> {
                this.maxHealth = 35 + (level * 6);
                this.attackDamage = 8 + (level * 2);
                this.defense = 5 + (level * 2);
                this.attackSpeed = 1.2;
            }
            case "mage" -> {
                this.maxHealth = 22 + (level * 4);
                this.attackDamage = 7 + (level * 3);
                this.defense = 1 + level;
                this.attackSpeed = 0.6;
            }
            default -> {
                this.maxHealth = 20 + (level * 5);
                this.attackDamage = 4 + level;
                this.defense = 2 + level;
                this.attackSpeed = 1.0;
            }
        }
        
        // Initialize health to max if not set
        if (this.health == 0) {
            this.health = this.maxHealth;
        }
    }

    /**
     * Take damage and check if unit dies
     * @param amount Amount of damage to take
     * @return true if unit survived, false if unit died
     */
    public boolean takeDamage(int amount) {
        if (!alive) return false;
        
        health -= amount;
        if (health <= 0) {
            health = 0;
            alive = false;
            return false;
        }
        return true;
    }

    /**
     * Check if unit is still alive
     * @return true if unit is alive
     */
    public boolean isAlive() {
        return alive && health > 0;
    }

    /**
     * Level up the unit, increasing stats
     */
    public void levelUp() {
        if (!alive) return;
        
        level++;
        int oldMaxHealth = maxHealth;
        
        // Recalculate all stats
        initializeStats();
        
        // Heal the unit proportionally when leveling up
        health += (maxHealth - oldMaxHealth);
        
        // Reset experience for next level
        experience = 0;
        experienceToNextLevel = calculateExperienceForNextLevel(level);
    }

    /**
     * Add experience and check for level up
     * @param exp Experience to add
     * @return true if unit leveled up
     */
    public boolean addExperience(int exp) {
        if (!alive) return false;
        
        experience += exp;
        if (experience >= experienceToNextLevel) {
            levelUp();
            return true;
        }
        return false;
    }

    /**
     * Calculate experience needed for next level
     * @param currentLevel Current level
     * @return Experience needed for next level
     */
    private int calculateExperienceForNextLevel(int currentLevel) {
        return currentLevel * 100; // 100 exp per level
    }

    /**
     * Heal the unit by specified amount
     * @param amount Amount to heal
     */
    public void heal(int amount) {
        if (!alive) return;
        
        health = Math.min(health + amount, maxHealth);
    }

    /**
     * Get unit's attack damage
     * @return Attack damage
     */
    public int getAttackDamage() {
        return attackDamage;
    }

    /**
     * Get unit's defense value
     * @return Defense value
     */
    public int getDefense() {
        return defense;
    }

    /**
     * Get unit's attack speed
     * @return Attack speed multiplier
     */
    public double getAttackSpeed() {
        return attackSpeed;
    }

    // === TACTICAL COMMANDS ===

    /**
     * Set unit to attack a specific target
     * @param targetId UUID of target entity
     */
    public void setAttackTarget(UUID targetId) {
        this.attackTarget = targetId;
        this.currentState = UnitState.ATTACKING;
        this.stateChangeTime = System.currentTimeMillis();
    }

    /**
     * Set unit to defend a specific position
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public void setDefendPosition(double x, double y, double z) {
        this.defendX = x;
        this.defendY = y;
        this.defendZ = z;
        this.currentState = UnitState.DEFENDING;
        this.stateChangeTime = System.currentTimeMillis();
        this.attackTarget = null; // Clear attack target
    }

    /**
     * Set unit back to idle state
     */
    public void setIdle() {
        this.currentState = UnitState.IDLE;
        this.attackTarget = null;
        this.stateChangeTime = System.currentTimeMillis();
    }

    /**
     * Check if unit is currently attacking
     * @return true if unit is in attacking state
     */
    public boolean isAttacking() {
        return currentState == UnitState.ATTACKING && attackTarget != null;
    }

    /**
     * Check if unit is currently defending
     * @return true if unit is in defending state
     */
    public boolean isDefending() {
        return currentState == UnitState.DEFENDING;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public String getOwnerNation() {
        return ownerNation;
    }

    public double getHealthPercentage() {
        return maxHealth > 0 ? (double) health / maxHealth : 0.0;
    }

    // === TACTICAL STATE GETTERS ===

    public UnitState getCurrentState() {
        return currentState;
    }

    public UUID getAttackTarget() {
        return attackTarget;
    }

    public double getDefendX() {
        return defendX;
    }

    public double getDefendY() {
        return defendY;
    }

    public double getDefendZ() {
        return defendZ;
    }

    public long getStateChangeTime() {
        return stateChangeTime;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    // === PERSISTENCE METHODS ===

    /**
     * Serialize unit data to string for persistence
     * @return Serialized unit data
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(id.toString()).append("|");
        sb.append(type).append("|");
        sb.append(ownerNation).append("|");
        sb.append(level).append("|");
        sb.append(health).append("|");
        sb.append(maxHealth).append("|");
        sb.append(attackDamage).append("|");
        sb.append(defense).append("|");
        sb.append(attackSpeed).append("|");
        sb.append(alive).append("|");
        sb.append(currentState.name()).append("|");
        sb.append(attackTarget != null ? attackTarget.toString() : "null").append("|");
        sb.append(defendX).append("|");
        sb.append(defendY).append("|");
        sb.append(defendZ).append("|");
        sb.append(stateChangeTime).append("|");
        sb.append(experience).append("|");
        sb.append(experienceToNextLevel);
        return sb.toString();
    }

    /**
     * Deserialize unit data from string
     * @param data Serialized unit data
     * @return NationUnit instance or null if parsing failed
     */
    public static NationUnit deserialize(String data) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length < 18) return null;

            UUID id = UUID.fromString(parts[0]);
            String type = parts[1];
            String ownerNation = parts[2];
            int level = Integer.parseInt(parts[3]);
            int health = Integer.parseInt(parts[4]);
            int experience = Integer.parseInt(parts[16]);
            
            UnitState state = UnitState.valueOf(parts[10]);
            UUID attackTarget = "null".equals(parts[11]) ? null : UUID.fromString(parts[11]);
            double defendX = Double.parseDouble(parts[12]);
            double defendY = Double.parseDouble(parts[13]);
            double defendZ = Double.parseDouble(parts[14]);

            NationUnit unit = new NationUnit(id, type, ownerNation, level, health, experience, 
                                           state, attackTarget, defendX, defendY, defendZ);
            
            // Restore additional state
            unit.alive = Boolean.parseBoolean(parts[9]);
            unit.stateChangeTime = Long.parseLong(parts[15]);
            
            return unit;
        } catch (Exception e) {
            System.err.println("Failed to deserialize NationUnit: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        String stateInfo = switch (currentState) {
            case ATTACKING -> attackTarget != null ? " [ATTACKING]" : " [IDLE]";
            case DEFENDING -> String.format(" [DEFENDING %.0f,%.0f,%.0f]", defendX, defendY, defendZ);
            case RETURNING -> " [RETURNING]";
            default -> " [IDLE]";
        };
        
        return String.format("%s (Lv.%d) - %d/%d HP (%d ATK, %d DEF) - Nation: %s%s", 
                type, level, health, maxHealth, attackDamage, defense, ownerNation, stateInfo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NationUnit unit = (NationUnit) obj;
        return id.equals(unit.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}