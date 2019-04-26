package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TutorialManager {
    private static TutorialManager tutorialManager = null;

    private FileConfiguration tutorialConfig;

    public TutorialManager() {
        tutorialManager = this;

        loadTutorialFile();
    }

    public static TutorialManager getInstance() {
        if (tutorialManager == null) {
            new TutorialManager();
        }
        return tutorialManager;
    }

    private void loadTutorialFile() {
        File dataFolder = Civs.getInstance().getDataFolder();
        File tutorialFile = new File(dataFolder, "tutorial.yml");
        tutorialConfig = new YamlConfiguration();

        try {
            tutorialConfig.load(tutorialFile);
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.severe("Unable to load tutorial.yml");
        }
    }

    public void completeStep(Civilian civilian, TutorialType type, String param) {
        if (civilian.getTutorialIndex() == -1) {
            return;
        }

        ConfigurationSection path = tutorialConfig.getConfigurationSection(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        List<Map<?,?>> steps = path.getMapList("steps");
        if (steps.size() <= civilian.getTutorialIndex()) {
            return;
        }
        Map<?,?> step = steps.get(civilian.getTutorialIndex());
        if (step == null) {
            return;
        }

        if (!((String) step.get("type")).equalsIgnoreCase(type.toString())) {
            return;
        }

        if ((type.equals(TutorialType.BUILD) || type.equals(TutorialType.UPKEEP)) &&
                !param.equalsIgnoreCase(((String) step.get("region")))) {
            return;
        }
        if (type.equals(TutorialType.KILL) && !param.equals(step.get("kill-type"))) {
            return;
        }

        int progress = civilian.getTutorialProgress();
        Integer times = (Integer) step.get("times");
        int maxProgress = times == null ? 0 : times;
        if (progress < maxProgress) {
            civilian.setTutorialProgress(progress + 1);
            CivilianManager.getInstance().saveCivilian(civilian);
            // TODO send message of progress made?
            return;
        }
        Player player = Bukkit.getPlayer(civilian.getUuid());

        LinkedHashMap<?,?> rewards = (LinkedHashMap<?,?>) step.get("rewards");
        if (rewards != null) {
            List<String> itemList = (List<String>) rewards.get("items");
            if (itemList != null && !itemList.isEmpty() && player.isOnline()) {
                giveItemsToPlayer(player, itemList);
            }

            Object moneyDouble = rewards.get("money");
            double money = 0;
            if (moneyDouble != null) {
                if (moneyDouble instanceof Double) {
                    money = (Double) moneyDouble;
                } else if (moneyDouble instanceof Integer) {
                    money = (Integer) moneyDouble;
                }

                if (money > 0 && Civs.econ != null) {
                    Civs.econ.depositPlayer(player, money);
                }
            }
        }

        civilian.setTutorialProgress(0);
        civilian.setTutorialIndex(civilian.getTutorialIndex() + 1);
        CivilianManager.getInstance().saveCivilian(civilian);

        Util.spawnRandomFirework(player);

        sendMessageForCurrentTutorialStep(civilian);
    }

    public void sendMessageForCurrentTutorialStep(Civilian civilian) {
        if (civilian.getTutorialIndex() == -1) {
            return;
        }
        ConfigurationSection path = tutorialConfig.getConfigurationSection(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        Map<?,?> step = path.getMapList("steps").get(civilian.getTutorialIndex());
        if (step == null) {
            return;
        }


        String rawMessage = (String) ((LinkedHashMap<?,?>) step.get("messages")).get(civilian.getLocale());
        if (rawMessage == null) {
            rawMessage = (String) ((LinkedHashMap<?,?>) step.get("messages")).get("en");
        }
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (rawMessage != null && player.isOnline()) {
            List<String> messages = Util.parseColors(Util.textWrap("", rawMessage));
            for (String message : messages) {
                player.sendMessage(Civs.getPrefix() + message);
            }
        }

        // TODO open menu if type == CHOOSE
    }


    private void giveItemsToPlayer(Player player, List<String> itemList) {
        for (String item : itemList) {
            CVItem cvItem = CVItem.createCVItemFromString(item);
            int firstEmpty = player.getInventory().firstEmpty();
            if (firstEmpty > -1) {
                player.getInventory().addItem(cvItem.createItemStack());
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), cvItem.createItemStack());
            }
        }
    }


    public enum TutorialType {
        BUILD,
        UPKEEP,
        CHOOSE,
        KILL
    }
}