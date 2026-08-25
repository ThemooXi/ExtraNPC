package me.themoo.extranpc;

import me.themoo.extranpc.command.NpcCommand;
import me.themoo.extranpc.command.NpcTabCompleter;
import me.themoo.extranpc.gui.GuiListener;
import me.themoo.extranpc.integration.NativePlayerNpcProvider;
import me.themoo.extranpc.integration.PlaceholderHook;
import me.themoo.extranpc.integration.PlayerNpcProvider;
import me.themoo.extranpc.listener.ChatInputListener;
import me.themoo.extranpc.listener.NpcListener;
import me.themoo.extranpc.listener.SupportReminderListener;
import me.themoo.extranpc.manager.NpcManager;
import me.themoo.extranpc.manager.SkinManager;
import me.themoo.extranpc.storage.MessageService;
import me.themoo.extranpc.storage.NpcStorage;
import me.themoo.extranpc.util.ConsoleBanner;
import me.themoo.extranpc.util.SimpleUpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExtraNPCPlugin extends JavaPlugin {

    private static ExtraNPCPlugin instance;
    private NpcManager npcManager;
    private SkinManager skinManager;
    private NpcStorage npcStorage;
    private MessageService messages;
    private PlaceholderHook placeholderHook;
    private ChatInputListener chatInputListener;
    private PlayerNpcProvider playerNpcProvider;
    private SupportReminderListener supportReminderListener;
    private SimpleUpdateChecker updateChecker;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.messages = new MessageService(this);
        this.skinManager = new SkinManager(this);
        this.npcStorage = new NpcStorage(this);
        this.placeholderHook = new PlaceholderHook(this);
        this.chatInputListener = new ChatInputListener(this);
        this.placeholderHook.hook();
        ConsoleBanner.printHook("PlaceholderAPI", placeholderHook.isEnabled());

        this.playerNpcProvider = new NativePlayerNpcProvider(this);
        ConsoleBanner.printHook("NativePlayerNPC", playerNpcProvider.isAvailable());

        this.npcManager = new NpcManager(this);

        PluginCommand command = getCommand("extranpc");
        if (command != null) {
            NpcCommand executor = new NpcCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(new NpcTabCompleter(this));
        }

        getServer().getPluginManager().registerEvents(new NpcListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputListener, this);
        this.supportReminderListener = new SupportReminderListener(this);
        getServer().getPluginManager().registerEvents(supportReminderListener, this);

        if (getConfig().getBoolean("update-checker.enabled", true)) {
            this.updateChecker = new SimpleUpdateChecker(this);
            if (getConfig().getBoolean("update-checker.check-on-startup", true)) {
                Bukkit.getScheduler().runTaskLater(this, () -> updateChecker.checkForUpdates(), 60L);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            npcManager.loadAll();
            if (getConfig().getBoolean("settings.console-banner", true)) {
                ConsoleBanner.printEnable(this, npcManager.getNpcs().size(), true);
            } else {
                getLogger().info("Enabled — " + npcManager.getNpcs().size() + " NPC(s) loaded.");
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
        if (npcManager != null) {
            npcManager.saveAll();
            npcManager.despawnAll();
        }
        if (getConfig().getBoolean("settings.console-banner", true)) {
            ConsoleBanner.printDisable(this);
        }
        instance = null;
    }

    public void reloadAll() {
        reloadConfig();
        messages.reload();
        npcManager.saveAll();
        npcManager.despawnAll();
        playerNpcProvider = new NativePlayerNpcProvider(this);
        npcManager.loadAll();
    }

    public static ExtraNPCPlugin getInstance() {
        return instance;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public NpcStorage getNpcStorage() {
        return npcStorage;
    }

    public MessageService getMessages() {
        return messages;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public ChatInputListener getChatInputListener() {
        return chatInputListener;
    }

    public PlayerNpcProvider getPlayerNpcProvider() {
        return playerNpcProvider;
    }

    public SimpleUpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
