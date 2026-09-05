package me.themoo.extranpc.util;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.compat.ServerCompat;
import me.themoo.extranpc.compat.ServerVersionParser;
import me.themoo.extranpc.integration.PlayerNpcProvider;

import java.util.logging.Logger;

/**
 * Startup / shutdown ASCII banner for the server console.
 */
public final class ConsoleBanner {

    private ConsoleBanner() {
    }

    public static void printEnable(ExtraNPCPlugin plugin, int npcCount, PlayerNpcProvider playerNpcs) {
        Logger log = plugin.getLogger();
        String version = plugin.getDescription().getVersion();
        String line = "════════════════════════════════════════════════════════";
        String backend = playerNpcs != null && playerNpcs.isAvailable()
                ? playerNpcs.backendName()
                : "UNAVAILABLE";
        String server = ServerVersionParser.describeFamily(ServerCompat.version())
                + " · " + ServerCompat.rawVersion();

        log.info("");
        log.info(line);
        log.info("  _____      _              _   _ ____   ____ ");
        log.info(" | ____|_  _| |_ _ __ __ _ | \\ | |  _ \\ / ___|");
        log.info(" |  _| \\ \\/ / __| '__/ _` ||  \\| | |_) | |    ");
        log.info(" | |___ >  <| |_| | | (_| || |\\  |  __/| |___ ");
        log.info(" |_____/_/\\_\\\\__|_|  \\__,_||_| \\_|_|    \\____|");
        log.info("");
        log.info("  Advanced NPC Framework  ·  v" + version);
        log.info(line);
        log.info("  Status      : ENABLED");
        log.info("  Server      : " + server);
        log.info("  Support     : Paper 1.21.x / 26.1 / 26.2+");
        log.info("  NPCs        : " + npcCount + " loaded");
        log.info("  Player NPCs : " + backend);
        log.info("  Optional    : PlaceholderAPI (soft)");
        log.info(line);
        log.info("  Author      : ThemoO");
        log.info("  Discord     : " + SupportLinks.DISCORD_USER);
        log.info("  Support     : " + SupportLinks.DISCORD_INVITE);
        log.info(line);
        log.info("  Join Discord for help, updates & previews (Extra Flux)");
        log.info("  Tip: /extranpc gui  |  /extranpc help  |  /extranpc about  |  /extranpc update");
        log.info(line);
        log.info("");
    }

    public static void printDisable(ExtraNPCPlugin plugin) {
        Logger log = plugin.getLogger();
        String line = "════════════════════════════════════════════════════════";
        log.info("");
        log.info(line);
        log.info("  ExtraNPC v" + plugin.getDescription().getVersion() + " disabled");
        log.info("  All NPCs saved & despawned safely");
        log.info("  Support: " + SupportLinks.DISCORD_INVITE);
        log.info(line);
        log.info("");
    }

    public static void printHook(String name, boolean ok) {
        pluginLogger().info("Hook " + name + " → " + (ok ? "OK" : "SKIPPED"));
    }

    private static Logger pluginLogger() {
        ExtraNPCPlugin plugin = ExtraNPCPlugin.getInstance();
        return plugin != null ? plugin.getLogger() : Logger.getLogger("ExtraNPC");
    }
}
