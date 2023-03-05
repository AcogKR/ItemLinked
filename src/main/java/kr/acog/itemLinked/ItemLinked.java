package kr.acog.itemLinked;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.minecraft.core.IRegistry;
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
    public String link;
    public String itemColor;
    public String error;
    public String errorColor;
    public String errorType;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            link = getConfig().getString("options.item-linked", "[item]");
            itemColor = getConfig().getString("option.npe-message-color", "#B02C3A");
            error = getConfig().getString("options.npe-item-message", "You have no items in your hand.");
            errorType = getConfig().getString("options.npe-message-type", "actionbar");
            errorColor = getConfig().getString("options.item-color", "#B5D1B6");
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

        if (!message.equalsIgnoreCase(link)) {
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            if (errorType.equals("message")) {
                player.sendMessage(ChatColor.of(errorColor) + error);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.of(errorColor) + error));
            }
            return;
        }

        event.setCancelled(true);
        String format = String.format("%s: ", event.getFormat().split(":")[0]);
        String display = handItem.getItemMeta().getDisplayName().equals("")
                ? CraftItemStack.asNMSCopy(handItem).p()
                : ChatColor.stripColor(handItem.getItemMeta().getDisplayName());
        String translate = new TranslatableComponent(display).getTranslate();

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(format));
        TextComponent componentItem = new TextComponent(String.format("[%s]", translate));
        componentItem.setColor(ChatColor.of(itemColor));
        componentItem.setHoverEvent(ItemLinked.getItemHoverFrom(handItem));

        int startIndex = 0;
        int index;
        while ((index = message.indexOf(link, startIndex)) >= 0) {
            component.addExtra(message.substring(startIndex, index));
            component.addExtra(componentItem);
            startIndex = index + link.length();
        }

        if (startIndex < message.length()) {
            component.addExtra(new TextComponent(message.substring(startIndex)));
        }

        getLogger().info(ChatColor.stripColor(component.toLegacyText()));
        for (Player players : event.getRecipients()) {
            players.spigot().sendMessage(component);
        }
    }

    public static HoverEvent getItemHoverFrom(ItemStack bukkitItem) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(bukkitItem);
        String id = IRegistry.Y.b(item.c()).toString();
        int amount = item.K();
        String tag = item.u() != null ? item.u().g().toString() : "";
        return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(id, amount, ItemTag.ofNbt(tag)));
    }


}

