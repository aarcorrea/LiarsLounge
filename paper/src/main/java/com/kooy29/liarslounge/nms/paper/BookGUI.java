package com.kooy29.liarslounge.nms.paper;

import com.kooy29.liarslounge.api.gui.IBookGUI;
import com.kooy29.liarslounge.utils.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class BookGUI implements IBookGUI {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();
    private static ItemStack book;

    public void initialize() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();

        meta.title(Component.text("Guide"));
        meta.author(Component.text("LiarsLounge"));

        List<Component> pages = new ArrayList<>();

        ConfigurationSection section = MsgUtil.bookGUISection();
        for (String key : section.getKeys(false)) {
            List<String> lines = MsgUtil.colorize(section.getStringList(key));
            String joined = String.join("", lines);
            pages.add(LEGACY.deserialize(joined));
        }

        meta.pages(pages);
        item.setItemMeta(meta);

        book = item;
    }

    public void open(Player player) {
        if (book == null) initialize();
        player.openBook(book);
    }
}
