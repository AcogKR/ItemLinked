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

public class ItemLinked extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        String link = getConfig().getString("option.item-linked", "[item]"),
                itemColor = getConfig().getString("options.item-color", "#B5D1B6"),
                error = getConfig().getString("options.npe-item-message", "You have no items in your hand."),
                errorColor = getConfig().getString("option.npe-message-color", "#B02C3A"),
                errorType = getConfig().getString("options.npe-message-type", "actionbar");


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
