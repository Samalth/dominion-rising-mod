package com.example.dominionrising.common.nation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a nation with members, leader, roles, and balance
 */
public class Nation {
    private final String name;
    private UUID leader;
    private final List<UUID> members;
    private final Map<UUID, NationRole> memberRoles;
    private double balance;
    
    public Nation(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.memberRoles = new HashMap<>();
        this.members.add(leader); // Leader is also a member
        this.memberRoles.put(leader, NationRole.LEADER);
        this.balance = 0.0;
    }
    
    public String getName() {
        return name;
    }
    
    public UUID getLeader() {
        return leader;
    }
    
    public void setLeader(UUID leader) {
        this.leader = leader;
    }
    
    public List<UUID> getMembers() {
        return new ArrayList<>(members); // Return copy to prevent external modification
    }
    
    public boolean addMember(UUID playerId) {
        return addMember(playerId, NationRole.CITIZEN);
    }
    
    public boolean addMember(UUID playerId, NationRole role) {
        if (!members.contains(playerId)) {
            members.add(playerId);
            memberRoles.put(playerId, role);
            return true;
        }
        return false;
    }
    
    public boolean removeMember(UUID playerId) {
        if (!playerId.equals(leader)) { // Can't remove leader
            memberRoles.remove(playerId);
            return members.remove(playerId);
        }
        return false;
    }
    
    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }
    
    public boolean isLeader(UUID playerId) {
        return leader.equals(playerId);
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public void addToBalance(double amount) {
        this.balance += amount;
    }
    
    public boolean subtractFromBalance(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    /**
     * Get the role of a specific member
     */
    public NationRole getMemberRole(UUID playerId) {
        return memberRoles.getOrDefault(playerId, NationRole.CITIZEN);
    }
    
    /**
     * Set the role of a specific member
     */
    public boolean setMemberRole(UUID playerId, NationRole role) {
        if (isMember(playerId) && !playerId.equals(leader)) {
            memberRoles.put(playerId, role);
            return true;
        }
        return false;
    }
    
    /**
     * Get all members with their roles
     */
    public Map<UUID, NationRole> getMemberRoles() {
        return new HashMap<>(memberRoles);
    }
    
    /**
     * Count members by role
     */
    public long countMembersByRole(NationRole role) {
        return memberRoles.values().stream().filter(r -> r == role).count();
    }
    
    /**
     * Check if a player has a specific role or higher
     */
    public boolean hasRoleOrHigher(UUID playerId, NationRole minimumRole) {
        NationRole playerRole = getMemberRole(playerId);
        return playerRole.getPriority() >= minimumRole.getPriority();
    }
    
    @Override
    public String toString() {
        return "Nation{" +
                "name='" + name + '\'' +
                ", leader=" + leader +
                ", members=" + members.size() +
                ", leaders=" + countMembersByRole(NationRole.LEADER) +
                ", commanders=" + countMembersByRole(NationRole.COMMANDER) +
                ", citizens=" + countMembersByRole(NationRole.CITIZEN) +
                ", balance=" + balance +
                '}';
    }
}