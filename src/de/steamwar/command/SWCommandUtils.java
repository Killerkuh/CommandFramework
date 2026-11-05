/*
 *   This file is a part of the SteamWar software.
 *
 *   Copyright (C) 2025 SteamWar.de-Serverteam
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.steamwar.command;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class SWCommandUtils {

    @Getter
    private final Map<String, AbstractTypeMapper<?, ?>> MAPPER_FUNCTIONS = new HashMap<>();

    @Getter
    private final Map<String, AbstractValidator<?, ?>> VALIDATOR_FUNCTIONS = new HashMap<>();

    private SWTypeMapperCreator swTypeMapperCreator = (mapper, tabCompleter) -> new AbstractTypeMapper<Object, Object>() {
        @Override
        public Object map(Object sender, PreviousArguments previousArguments, String s) {
            return mapper.apply(s);
        }

        @Override
        public Collection<String> tabCompletes(Object sender, PreviousArguments previousArguments, String s) {
            return ((BiFunction<Object, Object, Collection<String>>) tabCompleter).apply(sender, s);
        }
    };

    static final AbstractTypeMapper<Object, String> STRING_MAPPER = createMapper(s -> s, Collections::singletonList);

    static {
        addMapper(boolean.class, Boolean.class, createMapper(s -> {
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
            return null;
        }, s -> Arrays.asList("true", "false")));
        addMapper(float.class, Float.class, createMapper(numberMapper(Float::parseFloat), numberCompleter(Float::parseFloat, true)));
        addMapper(double.class, Double.class, createMapper(numberMapper(Double::parseDouble), numberCompleter(Double::parseDouble, true)));
        addMapper(int.class, Integer.class, createMapper(numberMapper(Integer::parseInt), numberCompleter(Integer::parseInt, false)));
        addMapper(long.class, Long.class, createMapper(numberMapper(Long::parseLong), numberCompleter(Long::parseLong, false)));
        MAPPER_FUNCTIONS.put(String.class.getTypeName(), STRING_MAPPER);
    }

    public static <T extends AbstractTypeMapper<K, V>, K, V> void init(SWTypeMapperCreator<T, K, V> swTypeMapperCreator) {
        SWCommandUtils.swTypeMapperCreator = swTypeMapperCreator;
    }

    private static void addMapper(Class<?> clazz, Class<?> alternativeClazz, AbstractTypeMapper<?, ?> mapper) {
        MAPPER_FUNCTIONS.put(clazz.getTypeName(), mapper);
        MAPPER_FUNCTIONS.put(alternativeClazz.getTypeName(), mapper);
    }

    public static <T> AbstractTypeMapper<T, ?> getTypeMapper(String name, Map<String, AbstractTypeMapper<T, ?>> localTypeMapper) {
        AbstractTypeMapper<T, ?> typeMapper = localTypeMapper.getOrDefault(name, (AbstractTypeMapper<T, ?>) MAPPER_FUNCTIONS.getOrDefault(name, null));
        if (typeMapper == null) {
            throw new IllegalArgumentException("No mapper found for " + name);
        }
        return typeMapper;
    }

    public static <T> AbstractTypeMapper<T, ?> getTypeMapper(Parameter parameter, Map<String, AbstractTypeMapper<T, ?>> localTypeMapper) {
        Class<?> clazz = parameter.getType();
        if (parameter.isVarArgs()) {
            clazz = clazz.getComponentType();
        }
        if (clazz.isEnum() && !MAPPER_FUNCTIONS.containsKey(clazz.getTypeName()) && !localTypeMapper.containsKey(clazz.getTypeName())) {
            return createEnumMapper((Class<Enum<?>>) clazz);
        }
        return getTypeMapper(clazz.getTypeName(), localTypeMapper);
    }

    public static <T> AbstractValidator<T, ?> getValidator(AbstractSWCommand.Validator validator, Class<?> type, Map<String, AbstractValidator<T, ?>> localValidator) {
        String s = validator.value() != null && !validator.value().isEmpty() ? validator.value() : type.getTypeName();
        AbstractValidator<T, ?> concreteValidator = localValidator.getOrDefault(s, (AbstractValidator<T, ?>) VALIDATOR_FUNCTIONS.getOrDefault(s, null));
        if (concreteValidator == null) {
            throw new IllegalArgumentException("No validator found for " + s);
        }
        return concreteValidator;
    }

    public static <K, T> void addMapper(Class<T> clazz, AbstractTypeMapper<K, T> mapper) {
        addMapper(clazz.getTypeName(), mapper);
    }

    public static <T> void addMapper(String name, AbstractTypeMapper<T, ?> mapper) {
        MAPPER_FUNCTIONS.putIfAbsent(name, mapper);
    }

    public static <T> void addValidator(Class<T> clazz, AbstractValidator<T, ?> validator) {
        addValidator(clazz.getTypeName(), validator);
    }

    public static <T> void addValidator(String name, AbstractValidator<T, ?> validator) {
        VALIDATOR_FUNCTIONS.putIfAbsent(name, validator);
    }

    public static <T extends AbstractTypeMapper<K, String>, K> T createMapper(String... values) {
        List<String> strings = Arrays.stream(values).map(String::toLowerCase).collect(Collectors.toList());
        List<String> tabCompletes = Arrays.asList(values);
        return createMapper(s -> strings.contains(s.toLowerCase()) ? s : null, s -> tabCompletes);
    }

    public static <T extends AbstractTypeMapper<K, V>, K, V> T createMapper(Function<String, V> mapper, Function<String, Collection<String>> tabCompleter) {
        return createMapper(mapper, (commandSender, s) -> tabCompleter.apply(s));
    }

    public static <T extends AbstractTypeMapper<K, V>, K, V> T createMapper(Function<String, V> mapper, BiFunction<K, String, Collection<String>> tabCompleter) {
        return (T) swTypeMapperCreator.createTypeMapper(mapper, tabCompleter);
    }

    public static <T extends AbstractTypeMapper<K, Enum<?>>, K> T createEnumMapper(Class<Enum<?>> enumClass) {
        Map<String, Enum<?>> enumMap = new HashMap<>();
        for (Enum<?> e : enumClass.getEnumConstants()) {
            enumMap.put(e.name().toLowerCase(), e);
        }
        return createMapper(s -> enumMap.get(s.toLowerCase()), (k, s) -> enumMap.keySet());
    }

    private static <T> Function<String, T> numberMapper(Function<String, T> mapper) {
        return s -> {
            if (s.equalsIgnoreCase("nan")) return null;
            try {
                return mapper.apply(s);
            } catch (NumberFormatException e) {
                // Ignored
            }
            try {
                return mapper.apply(s.replace(',', '.'));
            } catch (NumberFormatException e) {
                return null;
            }
        };
    }

    private static Function<String, Collection<String>> numberCompleter(Function<String, ?> mapper, boolean comma) {
        return s -> {
            if (numberMapper(mapper).apply(s) == null) {
                return Collections.emptyList();
            }
            List<String> strings = new ArrayList<>();
            if (s.length() == 0) {
                strings.add("-");
            } else {
                strings.add(s);
            }
            for (int i = 0; i < 10; i++) {
                strings.add(s + i);
            }
            if (comma && (!s.contains(".") || !s.contains(","))) {
                strings.add(s + ".");
            }
            return strings;
        };
    }

    static <T extends Annotation> T[] getAnnotation(Method method, Class<T> annotation) {
        if (method.getAnnotations().length == 0) return null;
        return method.getDeclaredAnnotationsByType(annotation);
    }
}
