package dev.spawnportals.models;

import org.bukkit.World;

public enum PortalType {

    OVERWORLD("overworld", "§a§lOVERWORLD", "§7Teletrasporto casuale", World.Environment.NORMAL),
    NETHER   ("nether",    "§c§lNETHER",    "§7Teletrasporto casuale", World.Environment.NETHER),
    END      ("end",       "§d§lEND",        "§7Teletrasporto casuale", World.Environment.THE_END);

    private final String id;
    private final String holoLine1;   // titolo colorato
    private final String holoLine2;   // sottotitolo
    private final World.Environment targetEnvironment;

    PortalType(String id, String holoLine1, String holoLine2, World.Environment env) {
        this.id = id;
        this.holoLine1 = holoLine1;
        this.holoLine2 = holoLine2;
        this.targetEnvironment = env;
    }

    public String getId()                          { return id; }
    public String getHoloLine1()                   { return holoLine1; }
    public String getHoloLine2()                   { return holoLine2; }
    public World.Environment getTargetEnvironment(){ return targetEnvironment; }

    public static PortalType fromString(String s) {
        for (PortalType t : values())
            if (t.id.equalsIgnoreCase(s)) return t;
        return null;
    }
}
