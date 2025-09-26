package com.example.dominionrising.common.nation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a nation with members, leader, and balance
 */
public class Nation {
    private final String name;
    private UUID leader;
    private final List<UUID> members;
    private double balance;
    
    public Nation(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader); // Leader is also a member
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
        if (!members.contains(playerId)) {
            members.add(playerId);
            return true;
        }
        return false;
    }
    
    public boolean removeMember(UUID playerId) {
        if (!playerId.equals(leader)) { // Can't remove leader
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
    
    @Override
    public String toString() {
        return "Nation{" +
                "name='" + name + '\'' +
                ", leader=" + leader +
                ", members=" + members.size() +
                ", balance=" + balance +
                '}';
    }
}