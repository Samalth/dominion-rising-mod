package com.example.dominionrising.common.unit;

import java.util.UUID;

/**
 * Represents a nation unit with stats, type, and owning nation
 */
public class NationUnit {
    private final UUID id;
    private final String type;
    private int level;
    private int health;
    private int maxHealth;
    private final String ownerNation;
    private boolean alive;

    public NationUnit(String type, String ownerNation, int initialLevel) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.ownerNation = ownerNation;
        this.level = initialLevel;
        this.maxHealth = calculateMaxHealth(initialLevel);
        this.health = this.maxHealth;
        this.alive = true;
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
        maxHealth = calculateMaxHealth(level);
        // Heal the unit proportionally when leveling up
        health += (maxHealth - oldMaxHealth);
    }

    /**
     * Calculate max health based on level
     * @param level Unit level
     * @return Maximum health for this level
     */
    private int calculateMaxHealth(int level) {
        return 20 + (level * 5); // Base 20 HP + 5 per level
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
     * Get unit's attack damage based on level and type
     * @return Attack damage
     */
    public int getAttackDamage() {
        int baseDamage = getBaseDamageForType(type);
        return baseDamage + (level - 1); // +1 damage per level above 1
    }

    /**
     * Get base damage for unit type
     * @param type Unit type
     * @return Base damage
     */
    private int getBaseDamageForType(String type) {
        return switch (type.toLowerCase()) {
            case "soldier" -> 6;
            case "archer" -> 5;
            case "knight" -> 8;
            case "mage" -> 7;
            default -> 4;
        };
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

    @Override
    public String toString() {
        return String.format("%s (Lv.%d) - %d/%d HP - Nation: %s", 
                type, level, health, maxHealth, ownerNation);
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