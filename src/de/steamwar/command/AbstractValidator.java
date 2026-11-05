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

import lombok.RequiredArgsConstructor;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface AbstractValidator<K, T> {

    /**
     * Validates the given value.
     *
     * @param sender        The sender of the command.
     * @param value         The value to validate or null if mapping returned null.
     * @param messageSender The message sender to send messages to the player. Never send messages directly to the player.
     * @return The result of the validation.
     */
    boolean validate(K sender, T value, MessageSender messageSender);

    @Deprecated
    default <C> Validator<C> validate(C value, MessageSender messageSender) {
        return new Validator<>(value, messageSender);
    }

    @Deprecated
    @RequiredArgsConstructor
    class Validator<C> {
        private final C value;
        private final MessageSender messageSender;

        private boolean valid = true;

        public <M> Validator<M> map(Function<C, M> mapper) {
            return new Validator<>(mapper.apply(value), messageSender).set(valid);
        }

        public Validator<C> set(boolean value) {
            this.valid = value;
            return this;
        }

        public Validator<C> and(Predicate<C> predicate) {
            valid &= predicate.test(value);
            return this;
        }

        public Validator<C> or(Predicate<C> predicate) {
            valid |= predicate.test(value);
            return this;
        }

        public Validator<C> errorMessage(String s, Object... args) {
            if (!valid) messageSender.send(s, args);
            return this;
        }

        public boolean result() {
            return valid;
        }
    }

    @FunctionalInterface
    interface MessageSender {
        void send(String s, Object... args);

        default boolean send(boolean condition, String s, Object... args) {
            if (condition) send(s, args);
            return condition;
        }

        default boolean send(BooleanSupplier condition, String s, Object... args) {
            return send(condition.getAsBoolean(), s, args);
        }
    }
}
