package dev.spawnportals.models;

import org.bukkit.World;

public enum PortalType {

    OVERWORLD("overworld", "\u00a7a\u00a7lOVERWORLD", "\u00a77Teletrasporto casuale", World.Environment.NORMAL),
    NETHER   ("nether",    "\u00a7c\u00a7lNETHER",    "\u00a77Teletrasporto casuale", World.Environment.NETHER),
    END      ("end",       "\u00a7d\u00a7lEND",        "\u00a77Teletrasporto casuale", World.Environment.THE_END);

    private final String id;
    private final String holoLine1;
    private final String holoLine2;
    private final World.Environment targetEnvironment;

    PortalType(String id, String holoLine1, String holoLine2, World.Environment env) {
        this.id = id;
        this.holoLine1 = holoLine1;
        this.holoLine2 = holoLine2;
        this.targetEnvironment = env;
    }

    public String getId()                           { return id; }
    public String getHoloLine1()                    { return holoLine1; }
    public String getHoloLine2()                    { return holoLine2; }
    public World.Environment getTargetEnvironment() { return targetEnvironment; }

    public static PortalType fromString(String s) {
        for (PortalType t : values())
            if (t.id.equalsIgnoreCase(s)) return t;
        return null;
    }
}
