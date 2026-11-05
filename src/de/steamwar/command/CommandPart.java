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

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

class CommandPart<T> {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    @AllArgsConstructor
    private static class CheckArgumentResult {
        private final boolean success;
        private final Object value;
    }

    private AbstractSWCommand<T> command;
    private AbstractTypeMapper<T, ?> typeMapper;
    private List<AbstractValidator<T, Object>> validators = new ArrayList<>();
    private Class<?> varArgType;
    private String optional;

    private CommandPart<T> next = null;

    @Setter
    private boolean ignoreAsArgument = false;

    @Setter
    private boolean onlyUseIfNoneIsGiven = false;

    private Parameter parameter;
    private int parameterIndex;

    public CommandPart(AbstractSWCommand<T> command, AbstractTypeMapper<T, ?> typeMapper, Class<?> varArgType, String optional, Parameter parameter, int parameterIndex) {
        this.command = command;
        this.typeMapper = typeMapper;
        this.varArgType = varArgType;
        this.optional = optional;
        this.parameter = parameter;
        this.parameterIndex = parameterIndex;

        if (optional != null && varArgType != null) {
            throw new IllegalArgumentException("A vararg part can't have an optional part! In method " + parameter.getDeclaringExecutable() + " with parameter " + parameterIndex);
        }
    }

    void addValidator(AbstractValidator<T, Object> validator) {
        if (validator == null) return;
        validators.add(validator);
    }

    public void setNext(CommandPart<T> next) {
        if (varArgType != null) {
            throw new IllegalArgumentException("There can't be a next part if this is a vararg part! In method " + parameter.getDeclaringExecutable() + " with parameter " + parameterIndex);
        }
        this.next = next;
    }

    public boolean isHelp() {
        if (next == null) {
            if (varArgType == null) {
                return false;
            }
            if (varArgType != String.class) {
                return false;
            }
            return typeMapper == SWCommandUtils.STRING_MAPPER;
        } else {
            return next.isHelp();
        }
    }

    public void generateArgumentArray(Consumer<Runnable> errors, List<Object> current, T sender, String[] args, int startIndex) {
        if (varArgType != null) {
            Object array = Array.newInstance(varArgType, args.length - startIndex);
            for (int i = startIndex; i < args.length; i++) {
                CheckArgumentResult validArgument = checkArgument(null, sender, args, current, i);
                if (!validArgument.success) throw new CommandParseException();
                Array.set(array, i - startIndex, validArgument.value);
            }
            for (AbstractValidator<T, Object> validator : validators) {
                if (!validator.validate(sender, array, (s, objects) -> {
                    errors.accept(() -> command.sendMessage(sender, s, objects));
                })) throw new CommandParseException();
            }
            current.add(array);
            return;
        }

        CheckArgumentResult validArgument = checkArgument(errors, sender, args, current, startIndex);
        if (!validArgument.success && optional == null) {
            throw new CommandParseException();
        }
        if (!validArgument.success) {
            if (!ignoreAsArgument) {
                if (!onlyUseIfNoneIsGiven) {
                    current.add(typeMapper.map(sender, new PreviousArguments(EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY), optional));
                } else if (startIndex >= args.length) {
                    current.add(typeMapper.map(sender, new PreviousArguments(EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY), optional));
                } else {
                    throw new CommandParseException();
                }
            }
            if (next != null) {
                next.generateArgumentArray(errors, current, sender, args, startIndex);
            }
            return;
        }
        if (!ignoreAsArgument) {
            current.add(validArgument.value);
        }
        if (next != null) {
            next.generateArgumentArray(errors, current, sender, args, startIndex + 1);
        } else if (startIndex + 1 < args.length) {
            throw new CommandParseException();
        }
    }

    public void generateTabComplete(List<String> current, T sender, String[] args, List<Object> mappedArgs, int startIndex) {
        if (varArgType != null) {
            List<Object> currentArgs = new ArrayList<>(mappedArgs);
            List<Object> varArgs = new ArrayList<>();
            for (int i = startIndex; i < args.length - 1; i++) {
                CheckArgumentResult validArgument = checkArgument((ignore) -> {
                }, sender, args, mappedArgs, i);
                if (!validArgument.success) return;
                varArgs.add(validArgument.value);
            }

            currentArgs.add(varArgs.toArray());
            Collection<String> strings = tabCompletes(sender, args, currentArgs, args.length - 1);
            if (strings != null) {
                current.addAll(strings);
            }
            return;
        }

        if (args.length - 1 > startIndex) {
            CheckArgumentResult checkArgumentResult = checkArgument((ignore) -> {
            }, sender, args, mappedArgs, startIndex);
            if (checkArgumentResult.success && next != null) {
                if (!ignoreAsArgument) {
                    mappedArgs.add(checkArgumentResult.value);
                }
                next.generateTabComplete(current, sender, args, mappedArgs, startIndex + 1);
                return;
            }
            if (optional != null && next != null) {
                next.generateTabComplete(current, sender, args, mappedArgs, startIndex);
            }
            return;
        }

        Collection<String> strings = tabCompletes(sender, args, mappedArgs, startIndex);
        if (strings != null) {
            current.addAll(strings);
        }
        if (optional != null && next != null) {
            next.generateTabComplete(current, sender, args, mappedArgs, startIndex);
        }
    }

    private Collection<String> tabCompletes(T sender, String[] args, List<Object> mappedArgs, int startIndex) {
        return TabCompletionCache.tabComplete(sender, args[startIndex], (AbstractTypeMapper<Object, ?>) typeMapper, () -> {
            try {
                return typeMapper.tabCompletes(sender, new PreviousArguments(Arrays.copyOf(args, startIndex), mappedArgs.toArray()), args[startIndex]);
            } catch (Throwable e) {
                throw CommandFrameworkException.commandPartExceptions("tabcompleting", e, args[startIndex], (varArgType != null ? varArgType : parameter.getType()), parameter.getDeclaringExecutable(), parameterIndex);
            }
        });
    }

    private CheckArgumentResult checkArgument(Consumer<Runnable> errors, T sender, String[] args, List<Object> mappedArgs, int index) {
        Object value;
        try {
            value = typeMapper.map(sender, new PreviousArguments(Arrays.copyOf(args, index), mappedArgs.toArray()), args[index]);
        } catch (Exception e) {
            return new CheckArgumentResult(false, null);
        }
        boolean success = true;
        for (AbstractValidator<T, Object> validator : validators) {
            try {
                if (!validator.validate(sender, value, (s, objects) -> {
                    errors.accept(() -> {
                        command.sendMessage(sender, s, objects);
                    });
                })) {
                    success = false;
                    value = null;
                }
            } catch (Throwable e) {
                throw CommandFrameworkException.commandPartExceptions("validating", e, args[index], (varArgType != null ? varArgType : parameter.getType()), parameter.getDeclaringExecutable(), parameterIndex);
            }
        }
        return new CheckArgumentResult(success, value);
    }

    public Class<?> getType() {
        return varArgType != null ? varArgType : parameter.getType();
    }
}
