package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.ShopTrade;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shopkeepers-style recipe editor.
 * Shop enable/disable is controlled only by the toggle (slot 0).
 */
public final class ShopEditorGui {

    public static final int TRADES_PER_PAGE = 4;

    private static final Set<UUID> SKIP_CLOSE_PREVIEW = ConcurrentHashMap.newKeySet();

    private static final int[][] ROW_ITEM_SLOTS = {
            {10, 11, 13},
            {19, 20, 22},
            {28, 29, 31},
            {37, 38, 40}
    };

    private static final int[] ROW_DELETE_SLOTS = {16, 25, 34, 43};
    private static final int[] ROW_USES_SLOTS = {15, 24, 33, 42};

    private final ExtraNPCPlugin plugin;
    private final Player player;
    private final NpcData data;
    private final int page;

    public ShopEditorGui(ExtraNPCPlugin plugin, Player player, NpcData data) {
        this(plugin, player, data, 0);
    }

    public ShopEditorGui(ExtraNPCPlugin plugin, Player player, NpcData data, int page) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
        this.page = Math.max(0, page);
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.SHOP, data.getId(), page);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.parse("<dark_green>Shop Editor</dark_green>"));
        holder.setInventory(inv);

        fillBackground(inv);
        drawControls(inv);
        drawTradeRows(inv);

        player.openInventory(inv);
    }

    private static void reopen(ExtraNPCPlugin plugin, Player player, NpcData data, int page) {
        SKIP_CLOSE_PREVIEW.add(player.getUniqueId());
        new ShopEditorGui(plugin, player, data, page).open();
    }

    private static void openPreview(ExtraNPCPlugin plugin, Player player, NpcData data) {
        if (data.getTrades().stream().noneMatch(ShopTrade::isValid)) {
            plugin.getMessages().send(player, "shop-empty");
            return;
        }
        // Preview only — never force-enable the shop
        SKIP_CLOSE_PREVIEW.add(player.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getNpcManager().openShop(player, data));
    }

    private void fillBackground(Inventory inv) {
        ItemStack pane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, pane);
        }
    }

    private void drawControls(Inventory inv) {
        inv.setItem(0, new ItemBuilder(data.isShopEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("<white>Shop: " + (data.isShopEnabled() ? "<green>ENABLED" : "<red>DISABLED") + "</white>")
                .loreMini(
                        "<gray>When ENABLED: right-click opens shop</gray>",
                        "<gray>When DISABLED: shop stays off</gray>",
                        "<yellow>Click to toggle</yellow>"
                ).build());

        inv.setItem(1, new ItemBuilder(data.isAdminShop() ? Material.DIAMOND : Material.GOLD_INGOT)
                .name("<white>Type: " + (data.isAdminShop() ? "<aqua>Admin Shop" : "<gold>Limited Stock") + "</white>")
                .loreMini(
                        "<gray>Admin: infinite stock</gray>",
                        "<gray>Limited: each recipe has max uses</gray>",
                        "<yellow>Click to toggle</yellow>"
                ).build());

        inv.setItem(3, new ItemBuilder(Material.BOOK)
                .name("<yellow>How to edit</yellow>")
                .loreMini(
                        "<gray>Each row is one trade recipe:</gray>",
                        "<white>Cost 1</white> <gray>+</gray> <white>Cost 2</white> <gray>→</gray> <green>Result</green>",
                        "",
                        "<gray>Place items, then close the menu</gray>",
                        "<gray>to open the shop preview.</gray>"
                ).build());

        inv.setItem(5, new ItemBuilder(Material.EMERALD)
                .name("<green>Open Shop (Preview)</green>")
                .loreMini(
                        "<gray>Test the villager trade UI</gray>",
                        "<dark_gray>Does not change Enabled/Disabled</dark_gray>"
                ).build());

        inv.setItem(7, new ItemBuilder(Material.ARROW)
                .name("<aqua>Previous Page</aqua>")
                .build());
        inv.setItem(8, new ItemBuilder(Material.PAPER)
                .name("<white>Page <yellow>" + (page + 1) + "</yellow></white>")
                .loreMini("<gray>Trades: <white>" + data.getTrades().size() + "</white></gray>")
                .build());

        inv.setItem(45, new ItemBuilder(Material.NETHER_STAR)
                .name("<green>Add Empty Recipe Row</green>")
                .loreMini("<gray>Jump to a free recipe row</gray>")
                .build());

        inv.setItem(49, new ItemBuilder(Material.BARRIER)
                .name("<red>Done</red>")
                .loreMini("<gray>Save and return to NPC editor</gray>")
                .build());

        inv.setItem(53, new ItemBuilder(Material.ARROW)
                .name("<aqua>Next Page</aqua>")
                .build());
    }

    private void drawTradeRows(Inventory inv) {
        ItemStack emptyHint1 = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                .name("<aqua>Cost 1</aqua>")
                .loreMini("<gray>Place payment item here</gray>")
                .build();
        ItemStack emptyHint2 = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                .name("<aqua>Cost 2 (optional)</aqua>")
                .loreMini("<gray>Optional second payment</gray>")
                .build();
        ItemStack emptyResult = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name("<green>Result</green>")
                .loreMini("<gray>Item the player receives</gray>")
                .build();
        ItemStack arrow = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE)
                .name("<yellow>→</yellow>")
                .build();

        for (int row = 0; row < TRADES_PER_PAGE; row++) {
            int index = page * TRADES_PER_PAGE + row;
            ShopTrade trade = index < data.getTrades().size() ? data.getTrades().get(index) : null;

            int in1 = ROW_ITEM_SLOTS[row][0];
            int in2 = ROW_ITEM_SLOTS[row][1];
            int result = ROW_ITEM_SLOTS[row][2];
            int arrowSlot = in2 + 1;

            inv.setItem(arrowSlot, arrow);

            if (trade != null && trade.isValid()) {
                inv.setItem(in1, trade.getIngredient1());
                inv.setItem(in2, trade.getIngredient2() != null ? trade.getIngredient2() : emptyHint2.clone());
                inv.setItem(result, trade.getResult());
                inv.setItem(ROW_USES_SLOTS[row], new ItemBuilder(Material.CLOCK)
                        .name("<yellow>Max uses: <white>" + trade.getMaxUses() + "</white></yellow>")
                        .loreMini(
                                data.isAdminShop()
                                        ? "<gray>Ignored while Admin Shop is ON</gray>"
                                        : "<green>Left: +1</green> <red>Right: -1</red> <gold>Shift: ±10</gold>"
                        ).build());
            } else {
                inv.setItem(in1, emptyHint1.clone());
                inv.setItem(in2, emptyHint2.clone());
                inv.setItem(result, emptyResult.clone());
                inv.setItem(ROW_USES_SLOTS[row], new ItemBuilder(Material.CLOCK)
                        .name("<yellow>Max uses: <white>9999</white></yellow>")
                        .loreMini("<gray>Set after placing a recipe</gray>")
                        .build());
            }

            inv.setItem(ROW_DELETE_SLOTS[row], new ItemBuilder(Material.TNT)
                    .name("<red>Clear Row</red>")
                    .loreMini("<gray>Removes this recipe</gray>")
                    .build());
        }
    }

    public static boolean isEditableSlot(int slot) {
        for (int[] row : ROW_ITEM_SLOTS) {
            for (int s : row) {
                if (s == slot) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isPlaceholder(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }
        Material type = item.getType();
        return type == Material.LIGHT_BLUE_STAINED_GLASS_PANE
                || type == Material.LIME_STAINED_GLASS_PANE
                || type == Material.YELLOW_STAINED_GLASS_PANE
                || type == Material.GRAY_STAINED_GLASS_PANE;
    }

    public static ItemStack realItemOrNull(ItemStack item) {
        if (item == null || item.getType().isAir() || isPlaceholder(item)) {
            return null;
        }
        return item.clone();
    }

    public static void savePage(NpcData data, Inventory inv, int page) {
        int base = page * TRADES_PER_PAGE;
        List<ShopTrade> existing = new ArrayList<>(data.getTrades());
        List<ShopTrade> before = new ArrayList<>();
        List<ShopTrade> after = new ArrayList<>();

        for (int i = 0; i < existing.size(); i++) {
            if (i < base) {
                before.add(existing.get(i));
            } else if (i >= base + TRADES_PER_PAGE) {
                after.add(existing.get(i));
            }
        }

        List<ShopTrade> pageTrades = new ArrayList<>();
        for (int row = 0; row < TRADES_PER_PAGE; row++) {
            ItemStack in1 = realItemOrNull(inv.getItem(ROW_ITEM_SLOTS[row][0]));
            ItemStack in2 = realItemOrNull(inv.getItem(ROW_ITEM_SLOTS[row][1]));
            ItemStack result = realItemOrNull(inv.getItem(ROW_ITEM_SLOTS[row][2]));
            if (in1 == null || result == null) {
                continue;
            }
            int maxUses = 9999;
            int oldIndex = base + row;
            if (oldIndex < existing.size() && existing.get(oldIndex) != null) {
                maxUses = existing.get(oldIndex).getMaxUses();
            }
            pageTrades.add(new ShopTrade(result, in1, in2, maxUses));
        }

        data.getTrades().clear();
        data.getTrades().addAll(before);
        data.getTrades().addAll(pageTrades);
        data.getTrades().addAll(after);
    }

    public static void handle(ExtraNPCPlugin plugin, Player player, NpcData data, Inventory inv,
                              GuiHolder holder, int slot, boolean left, boolean shift) {
        int page = holder.getPage();

        if (isEditableSlot(slot)) {
            return;
        }

        switch (slot) {
            case 0 -> {
                savePage(data, inv, page);
                data.setShopEnabled(!data.isShopEnabled());
                plugin.getNpcManager().save(data);
                if (data.isShopEnabled()) {
                    plugin.getMessages().send(player, "shop-enabled");
                } else {
                    plugin.getMessages().send(player, "shop-disabled");
                }
                reopen(plugin, player, data, page);
            }
            case 1 -> {
                savePage(data, inv, page);
                data.setAdminShop(!data.isAdminShop());
                plugin.getNpcManager().save(data);
                reopen(plugin, player, data, page);
            }
            case 5 -> {
                savePage(data, inv, page);
                plugin.getNpcManager().save(data);
                openPreview(plugin, player, data);
            }
            case 7 -> {
                if (page > 0) {
                    savePage(data, inv, page);
                    plugin.getNpcManager().save(data);
                    reopen(plugin, player, data, page - 1);
                }
            }
            case 53 -> {
                savePage(data, inv, page);
                plugin.getNpcManager().save(data);
                reopen(plugin, player, data, page + 1);
            }
            case 45 -> {
                savePage(data, inv, page);
                plugin.getNpcManager().save(data);
                int targetPage = data.getTrades().size() / TRADES_PER_PAGE;
                reopen(plugin, player, data, targetPage);
            }
            case 49 -> {
                savePage(data, inv, page);
                plugin.getNpcManager().save(data);
                SKIP_CLOSE_PREVIEW.add(player.getUniqueId());
                new NpcEditGui(plugin, player, data).open();
            }
            default -> {
                for (int row = 0; row < TRADES_PER_PAGE; row++) {
                    if (slot == ROW_DELETE_SLOTS[row]) {
                        inv.setItem(ROW_ITEM_SLOTS[row][0], null);
                        inv.setItem(ROW_ITEM_SLOTS[row][1], null);
                        inv.setItem(ROW_ITEM_SLOTS[row][2], null);
                        savePage(data, inv, page);
                        plugin.getNpcManager().save(data);
                        reopen(plugin, player, data, page);
                        return;
                    }
                    if (slot == ROW_USES_SLOTS[row] && !data.isAdminShop()) {
                        savePage(data, inv, page);
                        int index = page * TRADES_PER_PAGE + row;
                        if (index < data.getTrades().size()) {
                            ShopTrade trade = data.getTrades().get(index);
                            int delta = shift ? 10 : 1;
                            if (!left) {
                                delta = -delta;
                            }
                            trade.setMaxUses(trade.getMaxUses() + delta);
                            plugin.getNpcManager().save(data);
                            reopen(plugin, player, data, page);
                        }
                        return;
                    }
                }
            }
        }
    }

    public static void handleClose(ExtraNPCPlugin plugin, Player player, NpcData data, Inventory inv, int page) {
        savePage(data, inv, page);
        plugin.getNpcManager().save(data);
        SKIP_CLOSE_PREVIEW.remove(player.getUniqueId());
        // Closing only saves — never force-enables or reopens the shop
    }
}
