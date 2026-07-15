package su.nightexpress.quests.task.adapter.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quests.task.adapter.type.AbstractAdapter;

public class StringAdapter extends AbstractAdapter<String, String> {

    public StringAdapter(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean canHandle(@NotNull String entity) {
        return true;
    }

    @Override
    @Nullable
    public String getTypeByName(@NotNull String name) {
        return name;
    }

    @Override
    @Nullable
    public String getType(@NotNull String entity) {
        return entity;
    }

    @Override
    @NotNull
    public String getTypeName(@NotNull String type) {
        return type;
    }

    @Override
    @Nullable
    public String getLocalizedName(@NotNull String type) {
        return type;
    }

    @Override
    @NotNull
    public String toFullNameOfType(@NotNull String type) {
        return type;
    }
}
