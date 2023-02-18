package kr.acog.itemLinked;

import ch.njol.skript.variables.Variables;
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

        String[] messages = message.split(String.format("\\%s", itemLink));

        if (message.equals(itemLink)) {
            component.addExtra(componentItem);
        }
        for (int i = 0; i < messages.length; i++) {
            component.addExtra(messages[i]);
            if (i != messages.length - 1 || message.endsWith(itemLink)) {
                component.addExtra(componentItem);
            }
        }

        Object variable = Variables.getVariable(String.format("chat::%s", player.getUniqueId()), null, false);
        if (variable != null) {
            event.getRecipients().clear();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (event.getPlayer().getLocation().distanceSquared(onlinePlayer.getLocation()) <= 200 * 200) {
                    event.getRecipients().add(onlinePlayer);
                }
            }
        }
        getLogger().info(ChatColor.stripColor(component.toLegacyText()));
        for (Player players : event.getRecipients()) {
            players.spigot().sendMessage(component);
        }
    }

}
/*
on chat:
    if {cldgh1::%uuid of player%} is set:
        if {chat::%player's uuid%} is set:
            set chat recipients to all players in radius 200 around the player
            if {rank::%uuid of player%} is "½" or "⅓" or "ㅻ":
                add ops to chat recipients
            set chat format to "%{cldgh1::%uuid of player%}% %{color::%uuid of player%}%%player%&f%{rank::%uuid of player%}%: %message%"
        else:
            remove {chatall::*} from chat recipients
            set chat format to "&7[&6전체&7] &f%{cldgh1::%uuid of player%}% %{color::%uuid of player%}%%player%&f%{rank::%uuid of player%}%: %message%"
    if {cldgh1::%uuid of player%} is not set:
        if {chat::%player's uuid%} is set:
            set chat recipients to all players in radius 200 around the player
            if {rank::%uuid of player%} is "½" or "⅓" or "ㅻ":
                add ops to chat recipients
            set chat format to "%{color::%uuid of player%}%%player%&f%{rank::%uuid of player%}%: %message%"
        else:
            remove {chatall::*} from chat recipients
            set chat format to "&7[&6전체&7] &f%{color::%uuid of player%}%%player%&f%{rank::%uuid of player%}%: %message%"
 */
