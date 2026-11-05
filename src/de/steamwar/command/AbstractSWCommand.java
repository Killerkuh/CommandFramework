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

import java.lang.annotation.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSWCommand<T> {

    private static final Map<Class<AbstractSWCommand<?>>, List<AbstractSWCommand<?>>> dependencyMap = new HashMap<>();

    private Class<?> clazz; // This is used in createMappings()

    private boolean initialized = false;
    protected final List<SubCommand<T>> commandList = new ArrayList<>();
    protected final List<SubCommand<T>> helpCommandList = new ArrayList<>();

    private final Map<String, AbstractTypeMapper<T, ?>> localTypeMapper = new HashMap<>();
    private final Map<String, AbstractValidator<T, ?>> localValidators = new HashMap<>();

    protected AbstractSWCommand(Class<T> clazz, String command) {
        this(clazz, command, new String[0]);
    }

    protected AbstractSWCommand(Class<T> clazz, String command, String... aliases) {
        this.clazz = clazz;

        PartOf partOf = this.getClass().getAnnotation(PartOf.class);
        if (partOf != null) {
            dependencyMap.computeIfAbsent((Class<AbstractSWCommand<?>>) partOf.value(), k -> new ArrayList<>()).add(this);
            return;
        }

        createAndSafeCommand(command, aliases);
        unregister();
        register();
    }

    protected abstract void createAndSafeCommand(String command, String[] aliases);

    public abstract void unregister();

    public abstract void register();

    protected void commandSystemError(T sender, CommandFrameworkException e) {
        e.printStackTrace();
    }

    protected void commandSystemWarning(Supplier<String> message) {
        System.out.println(message.get());
    }

    protected void sendMessage(T sender, String message, Object[] args) {
    }

    protected void initialisePartOf(AbstractSWCommand parent) {
    }

    protected final void execute(T sender, String alias, String[] args) {
        initialize();
        List<Runnable> errors = new ArrayList<>();
        try {
            if (commandList.stream().noneMatch(s -> s.invoke(errors::add, sender, alias, args))) {
                errors.forEach(Runnable::run);
            } else return;
            if (!errors.isEmpty()) return;
            for (SubCommand<T> help : helpCommandList) {
                // Generic help must not hide the error of a more specific command
                if (help.noTabComplete && !errors.isEmpty()) break;
                if (help.invoke(errors::add, sender, alias, args)) return;
            }
            errors.forEach(Runnable::run);
        } catch (CommandFrameworkException e) {
            commandSystemError(sender, e);
            throw e;
        }
    }

    protected final List<String> tabComplete(T sender, String alias, String[] args) throws IllegalArgumentException {
        initialize();
        String string = args[args.length - 1].toLowerCase();
        return Stream.concat(commandList.stream(), helpCommandList.stream())
                .filter(s -> !s.noTabComplete)
                .map(s -> s.tabComplete(sender, args))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(s -> !s.isEmpty())
                .filter(s -> s.toLowerCase().startsWith(string) || string.startsWith(s.toLowerCase()))
                .distinct()
                .collect(Collectors.toList());
    }

    private synchronized void initialize() {
        if (initialized) return;
        List<Method> methods = methods().stream()
                .filter(this::validateMethod)
                .collect(Collectors.toList());
        for (Method method : methods) {
            Cached cached = method.getAnnotation(Cached.class);
            this.<Mapper, AbstractTypeMapper<?, ?>>add(Mapper.class, method, (anno, typeMapper) -> {
                TabCompletionCache.add(typeMapper, cached);
                (anno.local() ? ((Map) localTypeMapper) : SWCommandUtils.getMAPPER_FUNCTIONS()).put(anno.value(), typeMapper);
            });
            this.<ClassMapper, AbstractTypeMapper<?, ?>>add(ClassMapper.class, method, (anno, typeMapper) -> {
                TabCompletionCache.add(typeMapper, cached);
                (anno.local() ? ((Map) localTypeMapper) : SWCommandUtils.getMAPPER_FUNCTIONS()).put(anno.value().getName(), typeMapper);
            });
            this.<Validator, AbstractValidator<?, ?>>add(Validator.class, method, (anno, validator) -> {
                (anno.local() ? ((Map) localValidators) : SWCommandUtils.getVALIDATOR_FUNCTIONS()).put(anno.value(), validator);
            });
            this.<ClassValidator, AbstractValidator<?, ?>>add(ClassValidator.class, method, (anno, validator) -> {
                (anno.local() ? ((Map) localValidators) : SWCommandUtils.getVALIDATOR_FUNCTIONS()).put(anno.value().getName(), validator);
            });
        }
        for (Method method : methods) {
            add(Register.class, method, true, (anno, parameters) -> {
                for (int i = 1; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    Class<?> clazz = parameter.getType();
                    if (parameter.isVarArgs()) clazz = clazz.getComponentType();
                    Mapper mapper = parameter.getAnnotation(Mapper.class);
                    if (clazz.isEnum() && mapper == null && !SWCommandUtils.getMAPPER_FUNCTIONS().containsKey(clazz.getTypeName())) {
                        continue;
                    }
                    String name = mapper != null ? mapper.value() : clazz.getTypeName();
                    if (!SWCommandUtils.getMAPPER_FUNCTIONS().containsKey(name) && !localTypeMapper.containsKey(name)) {
                        commandSystemWarning(() -> "The parameter '" + parameter.toString() + "' is using an unsupported Mapper of type '" + name + "'");
                        return;
                    }
                }
                commandList.add(new SubCommand<>(this, method, anno.value(), localTypeMapper, localValidators, anno.description(), anno.noTabComplete()));
            });
        }

        if (dependencyMap.containsKey(this.getClass())) {
            dependencyMap.get(this.getClass()).forEach(abstractSWCommand -> {
                abstractSWCommand.localTypeMapper.putAll((Map) localTypeMapper);
                abstractSWCommand.localValidators.putAll((Map) localValidators);
                abstractSWCommand.initialisePartOf(this);
                abstractSWCommand.initialize();
                commandList.addAll((Collection) abstractSWCommand.commandList);
            });
        }

        Collections.sort(commandList);
        commandList.removeIf(subCommand -> {
            if (subCommand.isHelp) {
                helpCommandList.add(subCommand);
                return true;
            } else {
                return false;
            }
        });
        initialized = true;
    }

    private boolean validateMethod(Method method) {
        if (!checkType(method.getAnnotations(), method.getReturnType(), false, annotation -> {
            CommandMetaData.Method methodMetaData = annotation.annotationType().getAnnotation(CommandMetaData.Method.class);
            if (methodMetaData == null) return (aClass, varArg) -> true;
            if (method.getParameterCount() > methodMetaData.maxParameterCount() || method.getParameterCount() < methodMetaData.minParameterCount()) {
                return (aClass, varArg) -> false;
            }
            return (aClass, varArg) -> {
                Class<?>[] types = methodMetaData.value();
                if (types == null) return true;
                for (Class<?> type : types) {
                    if (type.isAssignableFrom(aClass)) return true;
                }
                return false;
            };
        }, "The method '" + method + "'")) return false;
        boolean valid = true;
        for (Parameter parameter : method.getParameters()) {
            if (!checkType(parameter.getAnnotations(), parameter.getType(), parameter.isVarArgs(), annotation -> {
                CommandMetaData.Parameter parameterMetaData = annotation.annotationType().getAnnotation(CommandMetaData.Parameter.class);
                if (parameterMetaData == null) return (aClass, varArg) -> true;
                Class<?> handler = parameterMetaData.handler();
                if (BiPredicate.class.isAssignableFrom(handler)) {
                    try {
                        return (BiPredicate<Class<?>, Boolean>) handler.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                    }
                }
                return (aClass, varArg) -> {
                    if (varArg) aClass = aClass.getComponentType();
                    Class<?>[] types = parameterMetaData.value();
                    if (types == null) return true;
                    for (Class<?> current : types) {
                        if (current.isAssignableFrom(aClass)) return true;
                    }
                    return false;
                };
            }, "The parameter '" + parameter + "'")) valid = false;
        }
        return valid;
    }

    private boolean checkType(Annotation[] annotations, Class<?> clazz, boolean varArg, Function<Annotation, BiPredicate<Class<?>, Boolean>> toApplicableTypes, String warning) {
        boolean valid = true;
        for (Annotation annotation : annotations) {
            BiPredicate<Class<?>, Boolean> predicate = toApplicableTypes.apply(annotation);
            if (!predicate.test(clazz, varArg)) {
                commandSystemWarning(() -> warning + " is using an unsupported annotation of type '" + annotation.annotationType().getName() + "'");
                valid = false;
            }
        }
        return valid;
    }

    private <T extends Annotation> void add(Class<T> annotation, Method method, boolean firstParameter, BiConsumer<T, Parameter[]> consumer) {
        T[] anno = SWCommandUtils.getAnnotation(method, annotation);
        if (anno == null || anno.length == 0) return;

        Parameter[] parameters = method.getParameters();
        if (firstParameter && !clazz.isAssignableFrom(parameters[0].getType())) {
            commandSystemWarning(() -> "The method '" + method.toString() + "' is lacking the first parameter of type '" + clazz.getTypeName() + "'");
            return;
        }
        Arrays.stream(anno).forEach(t -> consumer.accept(t, parameters));
    }

    private <T extends Annotation, K> void add(Class<T> annotation, Method method, BiConsumer<T, K> consumer) {
        add(annotation, method, false, (anno, parameters) -> {
            try {
                method.setAccessible(true);
                consumer.accept(anno, (K) method.invoke(this));
            } catch (Exception e) {
                throw new SecurityException(e.getMessage(), e);
            }
        });
    }

    // TODO: Implement this when Message System is ready
    /*
    public void addDefaultHelpMessage(String message) {
        defaultHelpMessages.add(message);
    }
     */

    private List<Method> methods() {
        List<Method> methods = new ArrayList<>();
        Class<?> current = getClass();
        while (current != AbstractSWCommand.class) {
            methods.addAll(Arrays.asList(current.getDeclaredMethods()));
            current = current.getSuperclass();
        }
        return methods;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface PartOf {
        Class<?> value();
    }

    // --- Annotation for the command ---

    /**
     * Annotation for registering a method as a command
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Repeatable(Register.Registeres.class)
    @CommandMetaData.Method(value = void.class, minParameterCount = 1)
    protected @interface Register {

        /**
         * Identifier of subcommand
         */
        String[] value() default {};

        @Deprecated
        boolean help() default false;

        String[] description() default {};

        boolean noTabComplete() default false;

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.METHOD})
        @CommandMetaData.Method(value = void.class, minParameterCount = 1)
        @interface Registeres {
            Register[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @CommandMetaData.Method(value = AbstractTypeMapper.class, maxParameterCount = 0)
    @CommandMetaData.ImplicitTypeMapper(handler = Mapper.Handler.class)
    protected @interface Mapper {
        String value();

        boolean local() default false;

        class Handler<T> implements AbstractTypeMapper<T, Object> {

            private AbstractTypeMapper<T, Object> inner;

            public Handler(AbstractSWCommand.Mapper mapper, Map<String, AbstractTypeMapper<T, ?>> localTypeMapper) {
                inner = (AbstractTypeMapper<T, Object>) SWCommandUtils.getTypeMapper(mapper.value(), localTypeMapper);
            }

            @Override
            public Object map(T sender, PreviousArguments previousArguments, String s) {
                return inner.map(sender, previousArguments, s);
            }

            @Override
            public boolean validate(T sender, Object value, MessageSender messageSender) {
                return inner.validate(sender, value, messageSender);
            }

            @Override
            public Collection<String> tabCompletes(T sender, PreviousArguments previousArguments, String s) {
                return inner.tabCompletes(sender, previousArguments, s);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @CommandMetaData.Method(value = AbstractTypeMapper.class, maxParameterCount = 0)
    protected @interface ClassMapper {
        Class<?> value();

        boolean local() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @CommandMetaData.Method(value = AbstractTypeMapper.class, maxParameterCount = 0)
    protected @interface Cached {
        long cacheDuration() default 5;

        TimeUnit timeUnit() default TimeUnit.SECONDS;

        boolean global() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @CommandMetaData.Method(value = AbstractValidator.class, maxParameterCount = 0)
    @CommandMetaData.ImplicitValidator(handler = Validator.Handler.class, order = 0)
    protected @interface Validator {
        String value() default "";

        boolean local() default false;

        boolean invert() default false;

        class Handler<T> implements AbstractValidator<T, Object> {

            private AbstractValidator<T, Object> inner;
            private boolean invert;

            public Handler(AbstractSWCommand.Validator validator, Class<?> clazz, Map<String, AbstractValidator<T, ?>> localValidator) {
                inner = (AbstractValidator<T, Object>) SWCommandUtils.getValidator(validator, clazz, localValidator);
                invert = validator.invert();
            }

            @Override
            public boolean validate(T sender, Object value, MessageSender messageSender) {
                return inner.validate(sender, value, messageSender) ^ invert;
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @CommandMetaData.Method(value = AbstractValidator.class, maxParameterCount = 0)
    protected @interface ClassValidator {
        Class<?> value();

        boolean local() default false;
    }

    // --- Implicit TypeMapper ---

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @CommandMetaData.Parameter({String.class, int.class, Integer.class, long.class, Long.class, boolean.class, Boolean.class})
    @CommandMetaData.ImplicitTypeMapper(handler = StaticValue.Handler.class)
    protected @interface StaticValue {
        String[] value();

        /**
         * This is the short form for 'allowImplicitSwitchExpressions'
         * and can be set to true if you want to allow int as well as boolean as annotated parameter types.
         * The value array needs to be at least 2 long for this flag to be considered.
         * While using an int, the value will represent the index into the value array.
         * While using a boolean, the {@link #falseValues()} defines which indices are
         * considered {@code false} or {@code true}.
         */
        boolean allowISE() default false;

        int[] falseValues() default {0};

        class Handler<T> implements AbstractTypeMapper<T, Object> {

            private AbstractTypeMapper inner;

            public Handler(StaticValue staticValue, Class<?> clazz) {
                if (clazz == String.class) {
                    inner = SWCommandUtils.createMapper(staticValue.value());
                    return;
                }
                if (!staticValue.allowISE()) {
                    throw new IllegalArgumentException("The parameter type '" + clazz.getTypeName() + "' is not supported by the StaticValue annotation");
                }
                if (clazz == boolean.class || clazz == Boolean.class) {
                    List<String> tabCompletes = new ArrayList<>(Arrays.asList(staticValue.value()));
                    Set<Integer> falseValues = new HashSet<>();
                    for (int i : staticValue.falseValues()) falseValues.add(i);
                    inner = SWCommandUtils.createMapper(s -> {
                        int index = tabCompletes.indexOf(s);
                        return index == -1 ? null : !falseValues.contains(index);
                    }, (commandSender, s) -> tabCompletes);
                } else if (clazz == int.class || clazz == Integer.class || clazz == long.class || clazz == Long.class) {
                    List<String> tabCompletes = new ArrayList<>(Arrays.asList(staticValue.value()));
                    inner = SWCommandUtils.createMapper(s -> {
                        Number index = tabCompletes.indexOf(s);
                        return index.longValue() == -1 ? null : index;
                    }, (commandSender, s) -> tabCompletes);
                } else {
                    throw new IllegalArgumentException("The parameter type '" + clazz.getTypeName() + "' is not supported by the StaticValue annotation");
                }
            }

            @Override
            public Object map(T sender, PreviousArguments previousArguments, String s) {
                return inner.map(sender, previousArguments, s);
            }

            @Override
            public Collection<String> tabCompletes(T sender, PreviousArguments previousArguments, String s) {
                return inner.tabCompletes(sender, previousArguments, s);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    protected @interface OptionalValue {
        /**
         * Will pe parsed against the TypeMapper specified by the parameter or annotation.
         */
        String value();

        /**
         * The method name stands for 'onlyUseIfNoneIsGiven'.
         */
        boolean onlyUINIG() default false;
    }

    // --- Implicit Validator ---

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @CommandMetaData.ImplicitValidator(handler = ErrorMessage.Handler.class, order = Integer.MAX_VALUE)
    protected @interface ErrorMessage {
        /**
         * Error message to be displayed when the parameter is invalid.
         */
        String value();

        /**
         * This is the short form for 'allowEmptyArrays'.
         */
        boolean allowEAs() default true;

        class Handler<T> implements AbstractValidator<T, Object> {

            private AbstractSWCommand.ErrorMessage errorMessage;

            public Handler(AbstractSWCommand.ErrorMessage errorMessage) {
                this.errorMessage = errorMessage;
            }

            @Override
            public boolean validate(T sender, Object value, MessageSender messageSender) {
                if (value == null) messageSender.send(errorMessage.value());
                if (!errorMessage.allowEAs() && value != null && value.getClass().isArray() && Array.getLength(value) == 0) {
                    messageSender.send(errorMessage.value());
                    return false;
                }
                return value != null;
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    protected @interface AllowNull {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @CommandMetaData.Parameter({int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class})
    @CommandMetaData.ImplicitValidator(handler = Min.Handler.class, order = 2)
    protected @interface Min {
        int intValue() default Integer.MIN_VALUE;

        long longValue() default Long.MIN_VALUE;

        float floatValue() default Float.MIN_VALUE;

        double doubleValue() default Double.MIN_VALUE;

        boolean inclusive() default true;

        class Handler<T> implements AbstractValidator<T, Number> {

            private int value;
            private Function<Number, Number> comparator;

            public Handler(AbstractSWCommand.Min min, Class<?> clazz) {
                this.value = min.inclusive() ? 0 : 1;
                this.comparator = createComparator("Min", clazz, min.intValue(), min.longValue(), min.floatValue(), min.doubleValue());
            }

            @Override
            public boolean validate(T sender, Number value, MessageSender messageSender) {
                if (value == null) return true;
                return (comparator.apply(value).intValue()) >= this.value;
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @CommandMetaData.Parameter({int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class})
    @CommandMetaData.ImplicitValidator(handler = Max.Handler.class, order = 2)
    protected @interface Max {
        int intValue() default Integer.MAX_VALUE;

        long longValue() default Long.MAX_VALUE;

        float floatValue() default Float.MAX_VALUE;

        double doubleValue() default Double.MAX_VALUE;

        boolean inclusive() default true;

        class Handler<T> implements AbstractValidator<T, Number> {

            private int value;
            private Function<Number, Number> comparator;

            public Handler(AbstractSWCommand.Max max, Class<?> clazz) {
                this.value = max.inclusive() ? 0 : -1;
                this.comparator = createComparator("Max", clazz, max.intValue(), max.longValue(), max.floatValue(), max.doubleValue());
            }

            @Override
            public boolean validate(T sender, Number value, MessageSender messageSender) {
                if (value == null) return true;
                return (comparator.apply(value).intValue()) <= this.value;
            }
        }
    }

    private static Function<Number, Number> createComparator(String type, Class<?> clazz, int iValue, long lValue, float fValue, double dValue) {
        if (clazz == int.class || clazz == Integer.class) {
            return number -> Integer.compare(number.intValue(), iValue);
        } else if (clazz == long.class || clazz == Long.class) {
            return number -> Long.compare(number.longValue(), lValue);
        } else if (clazz == float.class || clazz == Float.class) {
            return number -> Float.compare(number.floatValue(), fValue);
        } else if (clazz == double.class || clazz == Double.class) {
            return number -> Double.compare(number.doubleValue(), dValue);
        } else {
            throw new IllegalArgumentException(type + " annotation is not supported for " + clazz);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @CommandMetaData.ImplicitTypeMapper(handler = Length.Handler.class)
    protected @interface Length {
        int min() default 0;

        int max() default Integer.MAX_VALUE;

        class Handler<T> implements AbstractTypeMapper<T, Object> {

            private int min;
            private int max;
            private AbstractTypeMapper<T, Object> inner;

            public Handler(Length length, AbstractTypeMapper<T, Object> inner) {
                this.min = length.min();
                this.max = length.max();
                this.inner = inner;
            }

            @Override
            public Object map(T sender, PreviousArguments previousArguments, String s) {
                if (s.length() < min || s.length() > max) return null;
                return inner.map(sender, previousArguments, s);
            }

            @Override
            public Collection<String> tabCompletes(T sender, PreviousArguments previousArguments, String s) {
                List<String> tabCompletes = inner.tabCompletes(sender, previousArguments, s)
                        .stream()
                        .filter(str -> str.length() >= min)
                        .map(str -> str.substring(0, Math.min(str.length(), max)))
                        .collect(Collectors.toList());
                if (s.length() < min) {
                    tabCompletes.add(0, s);
                }
                return tabCompletes;
            }

            @Override
            public String normalize(T sender, String s) {
                return inner.normalize(sender, s);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @CommandMetaData.Parameter(handler = ArrayLength.Type.class)
    @CommandMetaData.ImplicitTypeMapper(handler = ArrayLength.HandlerTypeMapper.class)
    @CommandMetaData.ImplicitValidator(handler = ArrayLength.HandlerValidator.class, order = 1)
    protected @interface ArrayLength {
        int min() default 0;

        int max() default Integer.MAX_VALUE;

        class Type implements BiPredicate<Class<?>, Boolean> {
            @Override
            public boolean test(Class<?> clazz, Boolean isVarArgs) {
                return clazz.isArray();
            }
        }

        class HandlerTypeMapper<T> implements AbstractTypeMapper<T, Object> {

            private int max;
            private AbstractTypeMapper<T, Object> inner;

            public HandlerTypeMapper(ArrayLength arrayLength, AbstractTypeMapper<T, Object> inner) {
                this.max = arrayLength.max();
                this.inner = inner;
            }

            @Override
            public Object map(T sender, PreviousArguments previousArguments, String s) {
                return inner.map(sender, previousArguments, s);
            }

            @Override
            public Collection<String> tabCompletes(T sender, PreviousArguments previousArguments, String s) {
                Object[] mapped = previousArguments.getMappedArg(0);
                if (mapped.length >= max) return Collections.emptyList();
                return inner.tabCompletes(sender, previousArguments, s);
            }

            @Override
            public String normalize(T sender, String s) {
                return inner.normalize(sender, s);
            }
        }

        class HandlerValidator<T> implements AbstractValidator<T, Object> {

            private int min;
            private int max;

            public HandlerValidator(ArrayLength arrayLength) {
                this.min = arrayLength.min();
                this.max = arrayLength.max();
            }

            @Override
            public boolean validate(T sender, Object value, MessageSender messageSender) {
                if (value == null) return true;
                int length = Array.getLength(value);
                return length >= min && length <= max;
            }
        }
    }
}
