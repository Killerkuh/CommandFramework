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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SubCommand<T> implements Comparable<SubCommand<T>> {

    private AbstractSWCommand<T> abstractSWCommand;
    Method method;
    String[] description;
    String[] subCommand;
    private Predicate<T> senderPredicate;
    private Function<T, ?> senderFunction;
    AbstractValidator<T, T> validator;
    boolean noTabComplete;

    private Parameter[] parameters;

    private CommandPart<T> commandPart;

    boolean isHelp = false;

    SubCommand(AbstractSWCommand<T> abstractSWCommand, Method method, String[] subCommand, Map<String, AbstractTypeMapper<T, ?>> localTypeMapper, Map<String, AbstractValidator<T, ?>> localValidator, String[] description, boolean noTabComplete) {
        this.abstractSWCommand = abstractSWCommand;
        this.method = method;
        try {
            this.method.setAccessible(true);
        } catch (SecurityException e) {
            throw new SecurityException(e.getMessage(), e);
        }
        this.subCommand = subCommand;
        this.description = description;
        this.noTabComplete = noTabComplete;

        parameters = method.getParameters();
        AbstractSWCommand.Validator validator = parameters[0].getAnnotation(AbstractSWCommand.Validator.class);
        if (validator != null) {
            this.validator = (AbstractValidator<T, T>) SWCommandUtils.getValidator(validator, parameters[0].getType(), localValidator);
            if (validator.invert()) {
                AbstractValidator<T, T> current = this.validator;
                this.validator = (sender, value, messageSender) -> !current.validate(sender, value, messageSender);
            }
        }

        commandPart = generateCommandPart(abstractSWCommand, subCommand, parameters, localTypeMapper, localValidator);
        if (commandPart != null) isHelp = commandPart.isHelp();

        senderPredicate = t -> parameters[0].getType().isAssignableFrom(t.getClass());
        senderFunction = t -> parameters[0].getType().cast(t);
    }

    @Override
    public int compareTo(SubCommand<T> o) {
        int tLength = parameters.length + subCommand.length;
        int oLength = o.parameters.length + o.subCommand.length;

        boolean tVarArgs = parameters[parameters.length - 1].isVarArgs();
        boolean oVarArgs = o.parameters[o.parameters.length - 1].isVarArgs();

        if (tVarArgs) tLength *= -1;
        if (oVarArgs) oLength *= -1;

        if (tVarArgs && oVarArgs) return Integer.compare(tLength, oLength);

        return -Integer.compare(tLength, oLength);
    }

    boolean invoke(Consumer<Runnable> errors, T sender, String alias, String[] args) {
        try {
            if (!senderPredicate.test(sender)) {
                return false;
            }

            if (commandPart == null) {
                if (args.length != 0) {
                    return false;
                }
                if (validator != null) {
                    if (!validator.validate(sender, sender, (s, objectArgs) -> {
                        abstractSWCommand.sendMessage(sender, s, objectArgs);
                    })) {
                        return false;
                    }
                }
                method.invoke(abstractSWCommand, senderFunction.apply(sender));
            } else {
                List<Object> objects = new ArrayList<>();
                commandPart.generateArgumentArray(errors, objects, sender, args, 0);
                if (validator != null) {
                    if (!validator.validate(sender, sender, (s, objectArgs) -> {
                        abstractSWCommand.sendMessage(sender, s, objectArgs);
                    })) {
                        return false;
                    }
                }
                objects.add(0, senderFunction.apply(sender));
                method.invoke(abstractSWCommand, objects.toArray());
            }
        } catch (CommandFrameworkException e) {
            throw e;
        } catch (CommandParseException e) {
            return false;
        } catch (InvocationTargetException e) {
            throw new CommandFrameworkException(e, alias, args);
        } catch (IllegalAccessException | RuntimeException e) {
            throw new SecurityException(e.getMessage(), e);
        }
        return true;
    }

    List<String> tabComplete(T sender, String[] args) {
        if (validator != null && !validator.validate(sender, sender, (s, objects) -> {
        })) {
            return null;
        }
        if (commandPart == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        commandPart.generateTabComplete(list, sender, args, new ArrayList<>(), 0);
        return list;
    }

    private static <T> CommandPart<T> generateCommandPart(AbstractSWCommand<T> command, String[] subCommand, Parameter[] parameters, Map<String, AbstractTypeMapper<T, ?>> localTypeMapper, Map<String, AbstractValidator<T, ?>> localValidator) {
        CommandPart<T> first = null;
        CommandPart<T> current = null;
        for (String s : subCommand) {
            CommandPart commandPart = new CommandPart(command, SWCommandUtils.createMapper(s), null, null, null, -1);
            commandPart.addValidator(NULL_FILTER);
            commandPart.setIgnoreAsArgument(true);
            if (current != null) {
                current.setNext(commandPart);
            }
            current = commandPart;
            if (first == null) {
                first = current;
            }
        }
        for (int i = 1; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            AbstractTypeMapper<T, ?> typeMapper = handleImplicitTypeMapper(parameter, localTypeMapper);
            Class<?> varArgType = parameter.isVarArgs() ? parameter.getType().getComponentType() : null;
            AbstractSWCommand.OptionalValue optionalValue = parameter.getAnnotation(AbstractSWCommand.OptionalValue.class);

            CommandPart<T> commandPart = new CommandPart<>(command, typeMapper, varArgType, optionalValue != null ? optionalValue.value() : null, parameter, i);
            commandPart.setOnlyUseIfNoneIsGiven(optionalValue != null && optionalValue.onlyUINIG());
            handleImplicitTypeValidator(parameter, commandPart, localValidator);
            if (parameter.getAnnotation(AbstractSWCommand.AllowNull.class) == null) {
                commandPart.addValidator((AbstractValidator<T, Object>) NULL_FILTER);
            }
            if (current != null) {
                current.setNext(commandPart);
            }
            current = commandPart;
            if (first == null) {
                first = current;
            }
        }
        return first;
    }

    private static <T> AbstractTypeMapper<T, Object> handleImplicitTypeMapper(Parameter parameter, Map<String, AbstractTypeMapper<T, ?>> localTypeMapper) {
        Class<?> type = parameter.getType();
        if (parameter.isVarArgs()) {
            type = type.getComponentType();
        }

        Annotation[] annotations = parameter.getAnnotations();
        Constructor<?> sourceConstructor = null;
        Annotation sourceAnnotation = null;
        List<Constructor<?>> parentConstructors = new ArrayList<>();
        List<Annotation> parentAnnotations = new ArrayList<>();
        for (Annotation annotation : annotations) {
            CommandMetaData.ImplicitTypeMapper implicitTypeMapper = annotation.annotationType().getAnnotation(CommandMetaData.ImplicitTypeMapper.class);
            if (implicitTypeMapper == null) continue;
            Class<?> clazz = implicitTypeMapper.handler();
            if (!AbstractTypeMapper.class.isAssignableFrom(clazz)) continue;
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length != 1) continue;
            Constructor<?> constructor = constructors[0];
            if (needsTypeMapper(constructor)) {
                parentConstructors.add(constructor);
                parentAnnotations.add(annotation);
            } else {
                if (sourceAnnotation != null) {
                    throw new IllegalArgumentException("Multiple source type mappers found for parameter " + parameter);
                }
                sourceConstructor = constructor;
                sourceAnnotation = annotation;
            }
        }

        AbstractTypeMapper<T, Object> current;
        if (sourceAnnotation != null) {
            current = createInstance(sourceConstructor, sourceAnnotation, type, localTypeMapper);
        } else {
            current = (AbstractTypeMapper<T, Object>) SWCommandUtils.getTypeMapper(parameter, localTypeMapper);
        }
        for (int i = 0; i < parentConstructors.size(); i++) {
            Constructor<?> constructor = parentConstructors.get(i);
            Annotation annotation = parentAnnotations.get(i);
            current = createInstance(constructor, annotation, type, localTypeMapper, current);
        }
        return current;
    }

    private static boolean needsTypeMapper(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            if (AbstractTypeMapper.class.isAssignableFrom(parameterType)) {
                return true;
            }
        }
        return false;
    }

    private static <T> void handleImplicitTypeValidator(Parameter parameter, CommandPart<T> commandPart, Map<String, AbstractValidator<T, ?>> localValidator) {
        Annotation[] annotations = parameter.getAnnotations();
        Map<Integer, List<AbstractValidator<T, Object>>> validators = new HashMap<>();
        for (Annotation annotation : annotations) {
            CommandMetaData.ImplicitValidator implicitValidator = annotation.annotationType().getAnnotation(CommandMetaData.ImplicitValidator.class);
            if (implicitValidator == null) continue;
            Class<?> clazz = implicitValidator.handler();
            if (!AbstractValidator.class.isAssignableFrom(clazz)) continue;
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length != 1) continue;
            AbstractValidator<T, Object> validator = createInstance(constructors[0], annotation, commandPart.getType(), localValidator);
            validators.computeIfAbsent(implicitValidator.order(), integer -> new ArrayList<>()).add(validator);
        }
        List<Integer> keys = new ArrayList<>(validators.keySet());
        keys.sort(Integer::compareTo);
        for (Integer key : keys) {
            List<AbstractValidator<T, Object>> list = validators.get(key);
            for (AbstractValidator<T, Object> validator : list) {
                commandPart.addValidator(validator);
            }
        }
    }

    private static <T> T createInstance(Constructor<?> constructor, Object... parameter) {
        Class<?>[] types = constructor.getParameterTypes();
        List<Object> objects = new ArrayList<>();
        for (Class<?> clazz : types) {
            boolean found = false;
            for (Object o : parameter) {
                if (clazz.isAssignableFrom(o.getClass())) {
                    objects.add(o);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Could not find type " + clazz + " for constructor " + constructor);
            }
        }
        try {
            return (T) constructor.newInstance(objects.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static final AbstractValidator<?, Object> NULL_FILTER = (sender, value, messageSender) -> value != null;
}
