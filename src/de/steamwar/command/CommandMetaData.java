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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface CommandMetaData {

    /**
     * This annotation is only for internal use.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Method {
        Class<?>[] value();

        int minParameterCount() default 0;

        int maxParameterCount() default Integer.MAX_VALUE;
    }

    /**
     * This annotation denotes what types are allowed as parameter types the annotation annotated with can use.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Parameter {
        Class<?>[] value() default {};

        Class<?> handler() default void.class;
    }

    /**
     * This annotation can be used in conjunction with a class that implements {@link AbstractTypeMapper} to
     * create a custom type mapper for a parameter. The class must have one of two constructors with the following
     * types:
     * <ul>
     *     <li>Annotation this annotation annotates</li>
     *     <li>{@link Class}</li>
     *     <li>{@link AbstractTypeMapper}, optional, if not present only one per parameter</li>
     *     <li>{@link java.util.Map} with types {@link String} and {@link AbstractValidator}</li>
     * </ul>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface ImplicitTypeMapper {
        /**
         * The validator class that should be used.
         */
        Class<?> handler();
    }

    /**
     * This annotation can be used in conjunction with a class that implements {@link AbstractValidator} to
     * create a custom validator short hands for commands. The validator class must have one constructor with
     * one of the following types:
     * <ul>
     *     <li>Annotation this annotation annotates</li>
     *     <li>{@link Class}</li>
     *     <li>{@link java.util.Map} with types {@link String} and {@link AbstractValidator}</li>
     * </ul>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface ImplicitValidator {
        /**
         * The validator class that should be used.
         */
        Class<?> handler();

        /**
         * Defines when this validator should be processed. Negative numbers denote that this will be
         * processed before {@link AbstractSWCommand.Validator} and positive numbers
         * denote that this will be processed after {@link AbstractSWCommand.Validator}.
         */
        int order();
    }
}
