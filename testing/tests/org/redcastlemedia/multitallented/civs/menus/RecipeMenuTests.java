package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecipeMenuTests {

    private ArrayList<CVItem> subItems;
    private HashMap<Integer, List<CVItem>> cycleItems;
    private HashMap<Integer, CVItem> proxyInv;

    @Before
    public void setup() {
        this.subItems = new ArrayList<>();
        this.cycleItems = new HashMap<>();
        this.proxyInv = new HashMap<>();
    }

    @Test
    public void cycleItemsShouldHaveLengthOf2() {
        subItems.add(new CVItem(Material.STONE, 4));
        subItems.add(new CVItem(Material.CHEST, 2));


        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);

        assertEquals(2, cycleItems.get(0).size());
    }

    @Test
    public void cycleItemsShouldAddAirIfQtyGreaterThanMaxStackSize() {
        subItems.add(new CVItem(Material.STONE, 4));
        subItems.add(new CVItem(Material.WOODEN_PICKAXE, 4));

        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);
        assertEquals(Material.AIR, cycleItems.get(1).get(0).getMat());
    }

    @Test
    public void cycleItemsShouldBreakDownItemStacksIntoMaxStackSize() {
        subItems.add(new CVItem(Material.STONE, 4));
        subItems.add(new CVItem(Material.WOODEN_PICKAXE, 4));

        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);
        assertTrue(cycleItems.get(0).get(1).getMat() == Material.WOODEN_PICKAXE ||
                cycleItems.get(0).get(0).getMat() == Material.WOODEN_PICKAXE);
        assertEquals(1, cycleItems.get(0).get(1).getQty());
    }
}
