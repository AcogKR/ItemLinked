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
    private String link;
    private String message;
    private String type;
    private String color;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            link = getConfig().getString("options.item-linked", "[item]");
            message = getConfig().getString("options.npe-item-message", "You have no items in your hand.");
            type = getConfig().getString("options.npe-message-type", "actionbar");
            color = getConfig().getString("options.color", "#EC4D2B");
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
        if (!message.toUpperCase().contains(link.toUpperCase())) {
            return;
        }
        ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            if (type.equals("message")) player.sendMessage(getError(this.message));
            else player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(getError(this.message)));
            return;
        }
        event.setCancelled(true);
        String format = String.format("%s: ", event.getFormat().split(":")[0]);
        String display = handItem.getItemMeta().getDisplayName().equals("")
                ? handItem.getType().name()
                : handItem.getItemMeta().getDisplayName();

        TextComponent component = new TextComponent(format);
        TextComponent componentItem = new TextComponent("[");
        componentItem.addExtra(new TranslatableComponent(getItemTranslationKey(handItem))); // 번역 안돼면 [] 빼기
        componentItem.addExtra(new TextComponent("]"));
        componentItem.setColor(ChatColor.of(color));
        componentItem.setHoverEvent(getItemHoverFrom(handItem));

        if (message.equals(link)) {
            component.addExtra(componentItem);
        } else {
            String[] messages = message.split("\\[item]");
            for (int i = 0; i < messages.length; i++) {
                component.addExtra(messages[i]);
                if (i != messages.length - 1 || message.endsWith(link)) {
                    component.addExtra(componentItem);
                }
            }
        }

        player.spigot().sendMessage(component);
        player.spigot().sendMessage(new ComponentBuilder().create());
    }

    private String getError(String message) { return ChatColor.of("#EC4D2B") + message; }

    public HoverEvent getItemHoverFrom(ItemStack bukkitItem) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(bukkitItem);
        String id = IRegistry.Y.b(item.c()).toString();
        int amount = item.K();
        String tag = item.u() != null ? item.u().g().toString() : "";
        return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(id, amount, ItemTag.ofNbt(tag)));
    }

    private String getItemTranslationKey(ItemStack bukkitItem) {
        return CraftItemStack.asNMSCopy(bukkitItem).p();
    }
}
