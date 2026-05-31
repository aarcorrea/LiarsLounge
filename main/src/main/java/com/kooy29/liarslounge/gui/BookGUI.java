package com.kooy29.liarslounge.gui;

import cc.meteormc.bookapi.Book;
import cc.meteormc.bookapi.BookApi;
import com.kooy29.liarslounge.api.gui.IBookGUI;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class BookGUI implements IBookGUI {

    static Book book = null;

    public void initialize() {
        book = new Book();
        ConfigurationSection section = MsgUtil.bookGUISection();
        for (String key : section.getKeys(false)) {
            List<String> lines = MsgUtil.colorize(section.getStringList(key));
            book.addPage(String.join("", lines));
        }
    }

    public void open(Player player) {
        if (book == null) initialize();
        BookApi.openBook(player, book);
    }
}
