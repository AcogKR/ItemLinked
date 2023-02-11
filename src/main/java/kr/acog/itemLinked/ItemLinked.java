package kr.acog.itemLinked;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.minecraft.core.IRegistry;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ItemLinked extends JavaPlugin implements Listener {
    public static String link;
    public static String message;
    public static String type;
    public static String itemColor;
    public static String npeColor;

    public static HoverEvent getItemHoverFrom(ItemStack bukkitItem) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(bukkitItem);
        String id = IRegistry.Y.b(item.c()).toString();
        int amount = item.K();
        String tag = item.u() != null ? item.u().g().toString() : "";
        return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(id, amount, ItemTag.ofNbt(tag)));
    }

    public static String getItemTranslationKey(ItemStack bukkitItem) {
        return CraftItemStack.asNMSCopy(bukkitItem).p();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            link = getConfig().getString("options.item-linked", "[item]");
            npeColor = getConfig().getString("option.npe-message-color", "#B02C3A");
            message = getConfig().getString("options.npe-item-message", "You have no items in your hand.");
            type = getConfig().getString("options.npe-message-type", "actionbar");
            itemColor = getConfig().getString("options.item-color", "#B5D1B6");
        } catch (NullPointerException npe) {
            getLogger().log(Level.SEVERE, "There is a problem with the form in Config.yml");
            saveDefaultConfig();
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

}
