package kr.acog.itemLinked;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.minecraft.core.IRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ItemLinked extends JavaPlugin implements Listener {
    public String itemLink;
    public String itemLinkMessageColor;
    public String npeMessage;
    public String npeMessageType;
    public String npeMessageColor;

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
            itemLink = getConfig().getString("options.item-linked", "[item]");
            npeMessageColor = getConfig().getString("option.npe-message-color", "#B02C3A");
            npeMessage = getConfig().getString("options.npe-item-message", "You have no items in your hand.");
            npeMessageType = getConfig().getString("options.npe-message-type", "actionbar");
            itemLinkMessageColor = getConfig().getString("options.item-color", "#B5D1B6");
        } catch (NullPointerException npe) {
            getLogger().log(Level.SEVERE, "There is a problem with the form in Config.yml");
            saveDefaultConfig();
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (!message.toUpperCase().contains(itemLink.toUpperCase())) {
            return;
        }

        ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            if (npeMessageType.equals("message")) {
                player.sendMessage(ChatColor.of(npeMessageColor) + npeMessage);
            } else {
                    player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColor.of(npeMessageColor) + npeMessage)
                    );
            }
            return;
        }

        event.setCancelled(true);
        String format = String.format("%s: ", event.getFormat().split(":")[0]);
        String display = handItem.getItemMeta().getDisplayName().equals("")
                ? getItemTranslationKey(handItem)
                : ChatColor.stripColor(handItem.getItemMeta().getDisplayName());

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(format));
        TextComponent componentItem = new TextComponent("[");

        componentItem.addExtra(new TranslatableComponent(display));
        componentItem.addExtra(new TextComponent("]"));
        componentItem.setColor(ChatColor.of(itemLinkMessageColor));
        componentItem.setHoverEvent(ItemLinked.getItemHoverFrom(handItem));


        if (message.equals(itemLink)) {
            component.addExtra(componentItem);
            player.spigot().sendMessage(component);
            return;
        }

        String[] messages = message.split(String.format("\\%s", itemLink));
        for (int i = 0; i < messages.length; i++) {
            component.addExtra(messages[i]);
            if (i != messages.length - 1 || message.endsWith(itemLink)) {
                component.addExtra(componentItem);
            }
        }

        getLogger().info(ChatColor.stripColor(component.toLegacyText()));
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.spigot().sendMessage(component);
        }
    }

}
