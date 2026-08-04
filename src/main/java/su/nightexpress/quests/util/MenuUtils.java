package su.nightexpress.quests.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.island.definition.IslandResourceGroup;
import su.nightexpress.quests.quest.definition.QuestXPReward;
import su.nightexpress.quests.reward.Reward;
import su.nightexpress.quests.task.adapter.Adapter;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.milestone.data.MilestoneData;
import su.nightexpress.quests.quest.data.QuestData;
import su.nightexpress.quests.milestone.definition.Milestone;
import su.nightexpress.quests.quest.definition.Quest;
import su.nightexpress.quests.milestone.definition.MilestoneObjective;

import java.util.*;

import static su.nightexpress.quests.QuestsPlaceholders.*;

public class MenuUtils {

    @NotNull
    public static String buildProgressBar(double percent) {
        int length = Math.max(1, Config.PROGRESS_BAR_LENGTH.get());
        double clampedPercent = Math.clamp(percent, 0.0, 1.0);
        int filled = Math.clamp((int) Math.round(length * clampedPercent), 0, length);

        String symbol = Config.PROGRESS_BAR_SYMBOL.get();
        if (symbol == null || symbol.isEmpty()) symbol = "■";

        String colorFill = Config.PROGRESS_BAR_COLOR_FILLED.get();
        String colorEmpty = Config.PROGRESS_BAR_COLOR_EMPTY.get();
        boolean showPercent = Config.PROGRESS_BAR_SHOW_PERCENTAGE_INSIDE.get();

        if (showPercent) {
            String pctText = Math.round(clampedPercent * 100) + "%";
            int pctLen = pctText.length();
            if (pctLen <= length) {
                int startIdx = (length - pctLen) / 2;

                StringBuilder filledSb = new StringBuilder();
                StringBuilder emptySb = new StringBuilder();

                for (int index = 0; index < length; index++) {
                    String charAtIdx;
                    if (index >= startIdx && index < startIdx + pctLen) {
                        charAtIdx = String.valueOf(pctText.charAt(index - startIdx));
                    } else {
                        charAtIdx = symbol;
                    }

                    if (index < filled) {
                        filledSb.append(charAtIdx);
                    } else {
                        emptySb.append(charAtIdx);
                    }
                }

                return formatSection(filledSb.toString(), colorFill) + formatSection(emptySb.toString(), colorEmpty);
            }
        }

        StringBuilder filledSb = new StringBuilder();
        for (int i = 0; i < filled; i++) {
            filledSb.append(symbol);
        }
        StringBuilder emptySb = new StringBuilder();
        for (int i = filled; i < length; i++) {
            emptySb.append(symbol);
        }

        return formatSection(filledSb.toString(), colorFill) + formatSection(emptySb.toString(), colorEmpty);
    }

    @NotNull
    private static String formatSection(@NotNull String text, String color) {
        if (text.isEmpty()) return "";
        if (color == null || color.isEmpty()) return text;

        String trimmedColor = color.trim();
        if (!trimmedColor.startsWith("<")) {
            return "<" + trimmedColor + ">" + text + "</" + trimmedColor.split(":")[0] + ">";
        }

        String tagName = trimmedColor.substring(1);
        if (tagName.endsWith(">")) {
            tagName = tagName.substring(0, tagName.length() - 1);
        }
        int colonIndex = tagName.indexOf(':');
        String baseTagName = colonIndex != -1 ? tagName.substring(0, colonIndex) : tagName;

        return trimmedColor + text + "</" + baseTagName + ">";
    }

    @NotNull
    public static List<String> formatObjectives(@NotNull Milestone milestone, @NotNull MilestoneData data, int level) {
        List<String> list = new ArrayList<>();
        TaskType<?, ?> type = milestone.getType();

        milestone.getObjectiveTable().getEntryMap().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String fullName = entry.getKey();
            MilestoneObjective objective = entry.getValue();

            int required = objective.getAmount(level);
            int current = data.isLevelCompleted(level) ? required : data.getObjectiveProgress(fullName);

            list.add(formatObjective(type, fullName, current, required));
        });

        return list;
    }

    @NotNull
    public static List<String> formatObjectives(@NotNull Quest quest, @NotNull QuestData data) {
        List<String> list = new ArrayList<>();
        TaskType<?, ?> type = quest.getType();

        data.getObjectiveCounterMap().forEach((fullName, counter) -> {
            int required = counter.getRequired();
            int current = counter.getCompleted();

            list.add(formatObjective(type, fullName, current, required));
        });

        return list;
    }

    @NotNull
    private static String formatObjective(@NotNull TaskType<?, ?> type, @NotNull String fullName, int current, int required) {
        Adapter<?, ?> adapter = type.getAdapterFamily().getAdapterForName(fullName);
        String name = adapter == null ? fullName : adapter.getLocalizedName(fullName);

        return Lang.UI_MILESTONES_MILESTONE_OBJECTIVE.text()
            .replace(GENERIC_NAME, String.valueOf(name))
            .replace(GENERIC_CURRENT, NumberUtil.format(current))
            .replace(GENERIC_REQUIRED, NumberUtil.format(required));
    }

    @NotNull
    public static List<String> formatRewards(@NotNull List<Reward> rewards, int units, int level, double scale) {
        return rewards.stream()
            .sorted(Comparator.comparing(reward -> NightMessage.stripTags(reward.getName(units, level, scale))))
            .map(reward -> Lang.UI_ENTRY_REWARD.text().replace(GENERIC_NAME, reward.getName(units, level, scale)))
            .toList();
    }

    @NotNull
    public static List<String> formatBattlePassRewards(@NotNull QuestXPReward reward, double unitsWorth) {
        return Lists.newList(
            Lang.UI_ENTRY_REWARD_BATTLE_PASS_XP.text().replace(GENERIC_XP, NumberUtil.format(reward.getXP(unitsWorth)))
        );
    }

    @NotNull
    public static String formatNumber(long number) {
        return NumberUtil.format(number).replace(',', ' ').replace('\u00A0', ' ').replace('\u202F', ' ');
    }

    @NotNull
    public static String formatNumber(double number) {
        if (number % 1 == 0) {
            return formatNumber((long) number);
        }
        return NumberUtil.format(number).replace(',', ' ').replace('\u00A0', ' ').replace('\u202F', ' ');
    }

    @NotNull
    public static String formatWeightLore(@NotNull IslandResourceGroup group, String format) {
        if (format == null || format.trim().isEmpty()) {
            format = Config.ISLAND_WEIGHT_FORMAT.get();
        }
        String lineFormat = format;
        StringBuilder sb = new StringBuilder();
        group.getMaterials().forEach((mat, weight) -> {
            String weightStr = formatNumber(weight);
            String line = lineFormat
                .replace("%material%", mat.name())
                .replace("%material_name%", mat.name())
                .replace("%weight%", weightStr);
            sb.append(line).append("\n");
        });
        return sb.toString().trim();
    }

    public static int[] parseSlots(String slotsStr) {
        if (slotsStr == null || slotsStr.trim().isEmpty()) {
            return new int[0];
        }
        try {
            List<Integer> slots = new ArrayList<>();
            String[] split = slotsStr.split(",");
            for (String s : split) {
                s = s.trim();
                if (s.contains("-")) {
                    String[] range = s.split("-");
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    for (int i = start; i <= end; i++) {
                        slots.add(i);
                    }
                } else {
                    slots.add(Integer.parseInt(s));
                }
            }
            return slots.stream().mapToInt(Integer::intValue).toArray();
        } catch (Exception e) {
            return new int[0];
        }
    }

    @NotNull
    public static Map<Integer, int[]> loadSlotsByCount(@NotNull FileConfig config, @NotNull String path) {
        Map<Integer, int[]> map = new HashMap<>();
        String sectionPath = path.endsWith(".SlotsByCount") ? path : path + ".SlotsByCount";
        if (config.contains(sectionPath)) {
            for (String countKey : config.getSection(sectionPath)) {
                try {
                    int count = Integer.parseInt(countKey);
                    String slotsStr = config.getString(sectionPath + "." + countKey, "");
                    map.put(count, parseSlots(slotsStr));
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }
}
