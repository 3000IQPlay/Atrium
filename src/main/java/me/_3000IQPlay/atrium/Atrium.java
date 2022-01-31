package me._3000IQPlay.atrium;

import me._3000IQPlay.atrium.features.gui.custom.GuiCustomMainScreen;
import me._3000IQPlay.atrium.features.modules.client.IRC;
import me._3000IQPlay.atrium.features.modules.client.RPC;
import me._3000IQPlay.atrium.util.IconUtil;
import me._3000IQPlay.atrium.manager.*;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Mod(modid = "atrium", name = "Atrium", version = "1.5.6")
public class Atrium {
    public static final String MODID = "atrium";
    public static final String MODNAME = "Atrium";
    public static final String MODVER = "1.5.6";
    public static final Logger LOGGER = LogManager.getLogger("Atrium");
    public static ModuleManager moduleManager;
    public static SpeedManager speedManager;
    public static PositionManager positionManager;
    public static RotationManager rotationManager;
    public static CommandManager commandManager;
    public static EventManager eventManager;
    public static ConfigManager configManager;
    public static FileManager fileManager;
    public static FriendManager friendManager;
    public static TextManager textManager;
    public static ColorManager colorManager;
    public static ServerManager serverManager;
    public static PotionManager potionManager;
    public static InventoryManager inventoryManager;
    public static TimerManager timerManager;
    public static PacketManager packetManager;
    public static ReloadManager reloadManager;
    public static TotemPopManager totemPopManager;
    public static HoleManager holeManager;
    public static NotificationManager notificationManager;
    public static SafetyManager safetyManager;
    public static GuiCustomMainScreen customMainScreen;
    public static NoStopManager baritoneManager;
    public static WaypointManager waypointManager;
    @Mod.Instance
    public static Atrium INSTANCE;
    private static boolean unloaded;

    static {
        unloaded = false;
    }

    public static void load() {
        LOGGER.info("\n\nLoading Atrium");
        unloaded = false;
        if (reloadManager != null) {
            reloadManager.unload();
            reloadManager = null;
        }
        baritoneManager = new NoStopManager();
        totemPopManager = new TotemPopManager();
        timerManager = new TimerManager();
        packetManager = new PacketManager();
        serverManager = new ServerManager();
        colorManager = new ColorManager();
        textManager = new TextManager();
        moduleManager = new ModuleManager();
        speedManager = new SpeedManager();
        rotationManager = new RotationManager();
        positionManager = new PositionManager();
        commandManager = new CommandManager();
        eventManager = new EventManager();
        configManager = new ConfigManager();
        fileManager = new FileManager();
        friendManager = new FriendManager();
        potionManager = new PotionManager();
        inventoryManager = new InventoryManager();
        holeManager = new HoleManager();
        notificationManager = new NotificationManager();
        safetyManager = new SafetyManager();
        waypointManager = new WaypointManager();
        LOGGER.info("Initialized Managers");
        moduleManager.init();
        LOGGER.info("Modules loaded.");
        configManager.init();
        eventManager.init();
        LOGGER.info("EventManager loaded.");
        textManager.init(true);
        moduleManager.onLoad();
        totemPopManager.init();
        timerManager.init();
        if (moduleManager.getModuleByClass(RPC.class).isEnabled()) {
            DiscordPresence.start();
        }
        LOGGER.info("atrium initialized!\n");
    }

    public static void unload(boolean unload) {
        LOGGER.info("\n\nUnloading atrium");
        if (unload) {
            reloadManager = new ReloadManager();
            reloadManager.init(commandManager != null ? commandManager.getPrefix() : ".");
        }
        if (baritoneManager != null) {
            baritoneManager.stop();
        }
        Atrium.onUnload();
        eventManager = null;
        holeManager = null;
        timerManager = null;
        moduleManager = null;
        totemPopManager = null;
        serverManager = null;
        colorManager = null;
        textManager = null;
        speedManager = null;
        rotationManager = null;
        positionManager = null;
        commandManager = null;
        configManager = null;
        fileManager = null;
        friendManager = null;
        potionManager = null;
        inventoryManager = null;
        notificationManager = null;
        safetyManager = null;
        LOGGER.info("atrium unloaded!\n");
    }

    public static void reload() {
        Atrium.unload(false);
        Atrium.load();
    }

    public static void onUnload() {
        if (!unloaded) {
            try {
                IRC.INSTANCE.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            eventManager.onUnload();
            moduleManager.onUnload();
            configManager.saveConfig(Atrium.configManager.config.replaceFirst("atrium/", ""));
            moduleManager.onUnloadPost();
            timerManager.unload();
            unloaded = true;
        }
    }

    public static void setWindowIcon() {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            try (InputStream inputStream16x = Minecraft.class.getResourceAsStream("/assets/minecraft/textures/icons/icon-16x.png");
                 InputStream inputStream32x = Minecraft.class.getResourceAsStream("/assets/minecraft/textures/icons/icon-32x.png")) {
                ByteBuffer[] icons = new ByteBuffer[]{IconUtil.INSTANCE.readImageToBuffer(inputStream16x), IconUtil.INSTANCE.readImageToBuffer(inputStream32x)};
                Display.setIcon(icons);
            } catch (Exception e) {
                Atrium.LOGGER.error("Couldn't set Windows Icon", e);
            }
        }
    }

    private void setWindowsIcon() {
        Atrium.setWindowIcon();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        customMainScreen = new GuiCustomMainScreen();
        Display.setTitle("Atrium | v" + MODVER);
        setWindowsIcon();
        Atrium.load();
    }
}

