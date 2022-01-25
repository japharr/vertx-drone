package com.jeliiadesina.drone.util;

import am.ik.yavi.core.CustomConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EnumeratedConstraint<T> implements CustomConstraint<String> {
    private final Set<String> values;

    public static <E extends Enum<E>> EnumeratedConstraint<E> enumValues(Class<E> type) {
        return new EnumeratedConstraint<>(EnumSet.allOf(type), Enum::name);
    }

    public static EnumeratedConstraint<String> codes(Collection<String> codes) {
        return new EnumeratedConstraint<>(codes, Function.identity());
    }

    public EnumeratedConstraint(Collection<T> values, Function<T, String> codeMapper) {
        this.values = Objects.requireNonNull(values).stream().map(codeMapper).collect(Collectors.toSet());
    }

    @Override
    public @NotNull String defaultMessageFormat() {
        return "\"{0}\" value must be one of: " + strValues() + ".";
    }

    @Override
    public @NotNull String messageKey() {
        return "string.enumerated";
    }

    @Override
    public boolean test(String s) {
        return values.contains(s);
    }

    private String strValues() {
        return String.join(", ", values);
    }
}