package com.example.dominionrising.common.nation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for serializing and deserializing nation data for persistence
 * Uses a simple string-based format for compatibility
 */
public class NationDataSerializer {
    
    /**
     * Serialize nation data to string format
     */
    public static String serializeNations(Map<String, Nation> nations, Map<UUID, String> playerToNation) {
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("DOMINION_RISING_DATA_V1\n");
        
        // Serialize nations
        sb.append("NATIONS_START\n");
        for (Map.Entry<String, Nation> entry : nations.entrySet()) {
            Nation nation = entry.getValue();
            sb.append("NATION:").append(nation.getName()).append("\n");
            sb.append("LEADER:").append(nation.getLeader().toString()).append("\n");
            sb.append("BALANCE:").append(nation.getBalance()).append("\n");
            
            // Serialize members and roles
            sb.append("MEMBERS_START\n");
            for (UUID member : nation.getMembers()) {
                NationRole role = nation.getMemberRole(member);
                sb.append("MEMBER:").append(member.toString()).append(":").append(role.name()).append("\n");
            }
            sb.append("MEMBERS_END\n");
            sb.append("NATION_END\n");
        }
        sb.append("NATIONS_END\n");
        
        // Serialize player mappings
        sb.append("PLAYER_MAPPINGS_START\n");
        for (Map.Entry<UUID, String> entry : playerToNation.entrySet()) {
            sb.append("MAPPING:").append(entry.getKey().toString()).append(":").append(entry.getValue()).append("\n");
        }
        sb.append("PLAYER_MAPPINGS_END\n");
        
        return sb.toString();
    }
    
    /**
     * Deserialize nation data from string format
     */
    public static NationData deserializeNations(String data) {
        Map<String, Nation> nations = new HashMap<>();
        Map<UUID, String> playerToNation = new HashMap<>();
        
        String[] lines = data.split("\n");
        int i = 0;
        
        // Check header
        if (i < lines.length && lines[i].equals("DOMINION_RISING_DATA_V1")) {
            i++;
        } else {
            return new NationData(nations, playerToNation); // Invalid format, return empty
        }
        
        // Parse nations
        while (i < lines.length) {
            if (lines[i].equals("NATIONS_START")) {
                i++;
                while (i < lines.length && !lines[i].equals("NATIONS_END")) {
                    if (lines[i].startsWith("NATION:")) {
                        String nationName = lines[i].substring(7);
                        i++;
                        
                        UUID leader = null;
                        double balance = 0.0;
                        Map<UUID, NationRole> members = new HashMap<>();
                        
                        // Parse nation data
                        while (i < lines.length && !lines[i].equals("NATION_END")) {
                            if (lines[i].startsWith("LEADER:")) {
                                leader = UUID.fromString(lines[i].substring(7));
                            } else if (lines[i].startsWith("BALANCE:")) {
                                balance = Double.parseDouble(lines[i].substring(8));
                            } else if (lines[i].equals("MEMBERS_START")) {
                                i++;
                                while (i < lines.length && !lines[i].equals("MEMBERS_END")) {
                                    if (lines[i].startsWith("MEMBER:")) {
                                        String[] parts = lines[i].substring(7).split(":");
                                        UUID memberUuid = UUID.fromString(parts[0]);
                                        NationRole role = NationRole.valueOf(parts[1]);
                                        members.put(memberUuid, role);
                                    }
                                    i++;
                                }
                            }
                            i++;
                        }
                        
                        // Create nation
                        if (leader != null) {
                            Nation nation = new Nation(nationName, leader);
                            nation.setBalance(balance);
                            
                            // Add other members
                            for (Map.Entry<UUID, NationRole> memberEntry : members.entrySet()) {
                                if (!memberEntry.getKey().equals(leader)) {
                                    nation.addMember(memberEntry.getKey(), memberEntry.getValue());
                                }
                            }
                            
                            nations.put(nationName.toLowerCase(), nation);
                        }
                    }
                    i++;
                }
            } else if (lines[i].equals("PLAYER_MAPPINGS_START")) {
                i++;
                while (i < lines.length && !lines[i].equals("PLAYER_MAPPINGS_END")) {
                    if (lines[i].startsWith("MAPPING:")) {
                        String[] parts = lines[i].substring(8).split(":");
                        UUID uuid = UUID.fromString(parts[0]);
                        String nationName = parts[1];
                        playerToNation.put(uuid, nationName);
                    }
                    i++;
                }
            }
            i++;
        }
        
        return new NationData(nations, playerToNation);
    }
    
    /**
     * Data container for deserialized nation data
     */
    public static class NationData {
        public final Map<String, Nation> nations;
        public final Map<UUID, String> playerToNation;
        
        public NationData(Map<String, Nation> nations, Map<UUID, String> playerToNation) {
            this.nations = nations;
            this.playerToNation = playerToNation;
        }
    }
}