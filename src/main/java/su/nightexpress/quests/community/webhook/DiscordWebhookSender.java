package su.nightexpress.quests.community.webhook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import su.nightexpress.quests.community.data.CommunityActiveEvent;
import su.nightexpress.quests.community.definition.CommunityQuest;
import su.nightexpress.quests.config.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhookSender {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static CompletableFuture<HttpResponse<String>> sendEventStart(CommunityQuest quest, CommunityActiveEvent event) {
        String json = buildStartJson(quest, event);
        return sendWebhook(json);
    }

    public static CompletableFuture<HttpResponse<String>> sendEventVictory(CommunityQuest quest, CommunityActiveEvent event, List<Map.Entry<String, Double>> topContributors) {
        String json = buildVictoryJson(quest, event, topContributors);
        return sendWebhook(json);
    }

    public static CompletableFuture<HttpResponse<String>> sendEventFailure(CommunityQuest quest, CommunityActiveEvent event, List<Map.Entry<String, Double>> topContributors) {
        String json = buildFailureJson(quest, event, topContributors);
        return sendWebhook(json);
    }

    public static String buildStartJson(CommunityQuest quest, CommunityActiveEvent event) {
        String title = "🌐 Nouvel Événement Communautaire : " + (quest != null ? quest.getDisplayName() : event.getQuestId());
        String description = quest != null && quest.getDescription() != null ? String.join("\n", quest.getDescription()) : "Un nouvel événement communautaire vient de démarrer !";
        int color = Config.DISCORD_WEBHOOK_COLOR_START.get();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", stripColors(title));
        embed.addProperty("description", stripColors(description));
        embed.addProperty("color", color);

        JsonArray fields = new JsonArray();

        JsonObject targetField = new JsonObject();
        targetField.addProperty("name", "🎯 Objectif Global");
        targetField.addProperty("value", String.format("%.0f", event.getTargetAmount()));
        targetField.addProperty("inline", true);
        fields.add(targetField);

        JsonObject typeField = new JsonObject();
        typeField.addProperty("name", "📋 Type de Quête");
        typeField.addProperty("value", quest != null ? quest.getType() : "COMMUNITY");
        typeField.addProperty("inline", true);
        fields.add(typeField);

        long durationHours = (event.getEndTimestamp() - event.getStartTimestamp()) / (1000 * 60 * 60);
        JsonObject durationField = new JsonObject();
        durationField.addProperty("name", "⏳ Durée");
        durationField.addProperty("value", durationHours + " heures");
        durationField.addProperty("inline", true);
        fields.add(durationField);

        embed.add("fields", fields);

        return buildPayload(embed);
    }

    public static String buildVictoryJson(CommunityQuest quest, CommunityActiveEvent event, List<Map.Entry<String, Double>> topContributors) {
        String questName = quest != null ? quest.getDisplayName() : event.getQuestId();
        String title = "🏆 RÉSULTATS : VICTOIRE - " + questName;
        String description = "Félicitations ! La communauté a atteint 100% de l'objectif !";
        int color = Config.DISCORD_WEBHOOK_COLOR_SUCCESS.get();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", stripColors(title));
        embed.addProperty("description", stripColors(description));
        embed.addProperty("color", color);

        JsonArray fields = new JsonArray();

        JsonObject progressField = new JsonObject();
        progressField.addProperty("name", "📊 Progression Finale");
        progressField.addProperty("value", String.format("%.0f / %.0f (100%%)", event.getCurrentAmount(), event.getTargetAmount()));
        progressField.addProperty("inline", true);
        fields.add(progressField);

        JsonObject participantsField = new JsonObject();
        participantsField.addProperty("name", "👥 Participants");
        participantsField.addProperty("value", String.valueOf(event.getContributions().size()));
        participantsField.addProperty("inline", true);
        fields.add(participantsField);

        StringBuilder rankingSb = new StringBuilder();
        String[] medals = new String[]{"🥇", "🥈", "🥉"};
        if (topContributors != null && !topContributors.isEmpty()) {
            for (int i = 0; i < topContributors.size() && i < 3; i++) {
                Map.Entry<String, Double> entry = topContributors.get(i);
                rankingSb.append(medals[i]).append(" Top ").append(i + 1).append(" : **")
                    .append(entry.getKey()).append("** - ").append(String.format("%.0f", entry.getValue())).append("\n");
            }
        } else {
            rankingSb.append("Aucun contributeur.");
        }

        JsonObject rankingField = new JsonObject();
        rankingField.addProperty("name", "🏅 Classement Top 3");
        rankingField.addProperty("value", rankingSb.toString().trim());
        rankingField.addProperty("inline", false);
        fields.add(rankingField);

        embed.add("fields", fields);

        return buildPayload(embed);
    }

    public static String buildFailureJson(CommunityQuest quest, CommunityActiveEvent event, List<Map.Entry<String, Double>> topContributors) {
        String questName = quest != null ? quest.getDisplayName() : event.getQuestId();
        String title = "❌ RÉSULTATS : ÉCHEC - " + questName;
        String description = "Le temps est écoulé et l'objectif n'a pas été atteint.";
        int color = Config.DISCORD_WEBHOOK_COLOR_FAILED.get();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", stripColors(title));
        embed.addProperty("description", stripColors(description));
        embed.addProperty("color", color);

        JsonArray fields = new JsonArray();

        double percent = event.getTargetAmount() > 0 ? (event.getCurrentAmount() * 100.0 / event.getTargetAmount()) : 0.0;
        JsonObject progressField = new JsonObject();
        progressField.addProperty("name", "📊 Progression Finale");
        progressField.addProperty("value", String.format("%.0f / %.0f (%.1f%%)", event.getCurrentAmount(), event.getTargetAmount(), percent));
        progressField.addProperty("inline", true);
        fields.add(progressField);

        JsonObject participantsField = new JsonObject();
        participantsField.addProperty("name", "👥 Participants");
        participantsField.addProperty("value", String.valueOf(event.getContributions().size()));
        participantsField.addProperty("inline", true);
        fields.add(participantsField);

        StringBuilder rankingSb = new StringBuilder();
        String[] medals = new String[]{"🥇", "🥈", "🥉"};
        if (topContributors != null && !topContributors.isEmpty()) {
            for (int i = 0; i < topContributors.size() && i < 3; i++) {
                Map.Entry<String, Double> entry = topContributors.get(i);
                rankingSb.append(medals[i]).append(" Top ").append(i + 1).append(" : **")
                    .append(entry.getKey()).append("** - ").append(String.format("%.0f", entry.getValue())).append("\n");
            }
        } else {
            rankingSb.append("Aucun contributeur.");
        }

        JsonObject rankingField = new JsonObject();
        rankingField.addProperty("name", "🏅 Classement Top 3");
        rankingField.addProperty("value", rankingSb.toString().trim());
        rankingField.addProperty("inline", false);
        fields.add(rankingField);

        embed.add("fields", fields);

        return buildPayload(embed);
    }

    private static String buildPayload(JsonObject embed) {
        JsonObject root = new JsonObject();
        root.addProperty("username", Config.DISCORD_WEBHOOK_USERNAME.get());
        root.addProperty("avatar_url", Config.DISCORD_WEBHOOK_AVATAR_URL.get());

        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        root.add("embeds", embeds);

        return root.toString();
    }

    public static CompletableFuture<HttpResponse<String>> sendWebhook(String jsonPayload) {
        if (!Config.DISCORD_WEBHOOK_ENABLED.get()) {
            return CompletableFuture.completedFuture(null);
        }

        String url = Config.DISCORD_WEBHOOK_URL.get();
        if (url == null || url.trim().isEmpty() || url.contains("YOUR_WEBHOOK_URL")) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

            return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            CompletableFuture<HttpResponse<String>> cf = new CompletableFuture<>();
            cf.completeExceptionally(e);
            return cf;
        }
    }

    private static String stripColors(String input) {
        if (input == null) return "";
        return input.replaceAll("(?i)&[0-9a-fk-or]", "").replaceAll("(?i)§[0-9a-fk-or]", "");
    }
}
