package com.example.dominionrising.common.army;

/**
 * Represents a basic military unit (stub implementation)
 * This is a placeholder for future AI unit implementation
 */
public class Unit {
    private final String type;
    private int level;
    private double health;
    private final double maxHealth;
    
    public Unit(String type, int level, double maxHealth) {
        this.type = type;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }
    
    public String getType() {
        return type;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }
    
    public double getHealth() {
        return health;
    }
    
    public void setHealth(double health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public void heal(double amount) {
        setHealth(health + amount);
    }
    
    public void damage(double amount) {
        setHealth(health - amount);
    }
    
    public double getHealthPercentage() {
        return maxHealth > 0 ? (health / maxHealth) * 100 : 0;
    }
    
    @Override
    public String toString() {
        return "Unit{" +
                "type='" + type + '\'' +
                ", level=" + level +
                ", health=" + String.format("%.1f", health) + "/" + String.format("%.1f", maxHealth) +
                " (" + String.format("%.1f", getHealthPercentage()) + "%)" +
                '}';
    }
}