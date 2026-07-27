package su.nightexpress.quests.community.definition;

import java.util.ArrayList;
import java.util.List;

public class RankingRewards {

    private List<String> top1Commands;
    private List<String> top2Commands;
    private List<String> top3Commands;
    private double participationMinContribution;
    private List<String> participationCommands;

    public RankingRewards() {
        this.top1Commands = new ArrayList<>();
        this.top2Commands = new ArrayList<>();
        this.top3Commands = new ArrayList<>();
        this.participationCommands = new ArrayList<>();
    }

    public RankingRewards(List<String> top1Commands, List<String> top2Commands, List<String> top3Commands,
                          double participationMinContribution, List<String> participationCommands) {
        this.top1Commands = top1Commands != null ? top1Commands : new ArrayList<>();
        this.top2Commands = top2Commands != null ? top2Commands : new ArrayList<>();
        this.top3Commands = top3Commands != null ? top3Commands : new ArrayList<>();
        this.participationMinContribution = participationMinContribution;
        this.participationCommands = participationCommands != null ? participationCommands : new ArrayList<>();
    }

    public List<String> getTop1Commands() {
        return top1Commands;
    }

    public void setTop1Commands(List<String> top1Commands) {
        this.top1Commands = top1Commands != null ? top1Commands : new ArrayList<>();
    }

    public List<String> getTop2Commands() {
        return top2Commands;
    }

    public void setTop2Commands(List<String> top2Commands) {
        this.top2Commands = top2Commands != null ? top2Commands : new ArrayList<>();
    }

    public List<String> getTop3Commands() {
        return top3Commands;
    }

    public void setTop3Commands(List<String> top3Commands) {
        this.top3Commands = top3Commands != null ? top3Commands : new ArrayList<>();
    }

    public double getParticipationMinContribution() {
        return participationMinContribution;
    }

    public void setParticipationMinContribution(double participationMinContribution) {
        this.participationMinContribution = participationMinContribution;
    }

    public List<String> getParticipationCommands() {
        return participationCommands;
    }

    public void setParticipationCommands(List<String> participationCommands) {
        this.participationCommands = participationCommands != null ? participationCommands : new ArrayList<>();
    }
}
