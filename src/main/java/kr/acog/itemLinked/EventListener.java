package kr.acog.itemLinked;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import static kr.acog.itemLinked.ItemLinked.*;

public class EventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String senderMessage = event.getMessage();
        if (!senderMessage.toUpperCase().contains(link.toUpperCase())) {
            return;
        }
        ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();

        if (handItem.getType() == Material.AIR) {
            if (type.equals("message")) {
                player.sendMessage(npeColor + message);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(npeColor + message));
            }
            return;
        }

        event.setCancelled(true);
        String format = String.format("%s: ", event.getFormat().split(":")[0]);
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(format));
        String display = handItem.getItemMeta().getDisplayName().equals("")
                ? getItemTranslationKey(handItem)
                : ChatColor.stripColor(handItem.getItemMeta().getDisplayName());

        TextComponent componentItem = new TextComponent("[");
        componentItem.addExtra(new TranslatableComponent(display));
        componentItem.addExtra(new TextComponent("]"));
        componentItem.setColor(ChatColor.of(itemColor));
        componentItem.setHoverEvent(ItemLinked.getItemHoverFrom(handItem));

        if (senderMessage.equals(link)) {
            component.addExtra(componentItem);
            player.spigot().sendMessage(component);
            return;
        }

        String[] messages = senderMessage.split(String.format("\\%s", link));
        for (int i = 0; i < messages.length; i++) {
            component.addExtra(messages[i]);
            if (i != messages.length - 1 || senderMessage.endsWith(link)) {
                component.addExtra(componentItem);
            }
        }

        player.spigot().sendMessage(component);
    }

}
