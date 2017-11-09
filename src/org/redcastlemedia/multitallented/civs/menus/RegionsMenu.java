package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class RegionsMenu extends Menu {
    private static final String MENU_NAME = "CivsRegionStash";
    public RegionsMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        //Do nothing
    }

    //TODO make this more secure?
    @Override @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);
        if (!event.getInventory().getTitle().equals(MENU_NAME)) {
            return;
        }

        ItemManager itemManager = ItemManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        ArrayList<CivItem> stashItems = civilian.getStashItems();
        stashItems.clear();
        for (ItemStack is : event.getInventory()) {
            if (is == null || !CVItem.isCivsItem(is)) {
                continue;
            }
            CivItem civItem = itemManager.getItemType(is.getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase());
            civItem.setQty(is.getAmount());
            stashItems.add(civItem);
        }
        CivilianManager.getInstance().saveCivilian(civilian);
    }

    public static Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(civilian.getStashItems().size()), MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        int i=0;
        for (CivItem cvItem : civilian.getStashItems()) {
            boolean isTown = cvItem.getItemType().equals(CivItem.ItemType.TOWN);
            if (!cvItem.getItemType().equals(CivItem.ItemType.REGION) && !isTown) {
                continue;
            }
            List<String> lore = new ArrayList<>();
            lore.add(civilian.getUuid().toString());
            if (isTown) {
                lore.add(Util.parseColors(localeManager.getTranslation(civilian.getLocale(), "town-instructions")
                        .replace("$1", cvItem.getProcessedName())));
            } else {
                lore.addAll(Util.textWrap("", Util.parseColors(cvItem.getDescription(civilian.getLocale()))));
            }
            cvItem.setLore(lore);
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }

        return inventory;
    }
}