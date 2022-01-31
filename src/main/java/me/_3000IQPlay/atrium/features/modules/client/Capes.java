package me._3000IQPlay.atrium.features.modules.client;

import me._3000IQPlay.atrium.features.modules.Module;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class Capes
        extends Module {
    public static final ResourceLocation _3000IQPLAY_CAPE = new ResourceLocation("textures/atriumcape.png");
    public static Map<String, String[]> UUIDs = new HashMap<String, String[]>();
    private static Capes instance;

    public Capes() {
        super("Capes", "Renders the client's capes", Module.Category.CLIENT, false, false, false);
        UUIDs.put("_3000IQPlay", new String[]{"5d0d3c96-bd09-4490-9cf3-a718d250e4ca"});
        instance = this;
    }

    public static Capes getInstance() {
        if (instance == null) {
            instance = new Capes();
        }
        return instance;
    }

    public static ResourceLocation getCapeResource(AbstractClientPlayer player) {
        for (String name : UUIDs.keySet()) {
            for (String uuid : UUIDs.get(name)) {
                if (name.equalsIgnoreCase("_3000IQPlay") && player.getUniqueID().toString().equals(uuid)) {
                    return _3000IQPLAY_CAPE;
                }
            }
        }
        return null;
    }

    public static boolean hasCape(UUID uuid) {
        Iterator<String> iterator = UUIDs.keySet().iterator();
        if (iterator.hasNext()) {
            String name = iterator.next();
            return Arrays.asList((Object[]) UUIDs.get(name)).contains(uuid.toString());
        }
        return false;
    }
}
