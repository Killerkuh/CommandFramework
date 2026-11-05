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

import java.util.Collection;

public interface AbstractTypeMapper<K, T> extends AbstractValidator<K, T> {
    /**
     * The CommandSender can be null!
     */
    @Deprecated
    default T map(K sender, String[] previousArguments, String s) {
        throw new IllegalArgumentException("This method is deprecated and should not be used anymore!");
    }

    /**
     * The CommandSender can be null!
     */
    default T map(K sender, PreviousArguments previousArguments, String s) {
        return map(sender, previousArguments.userArgs, s);
    }

    @Override
    default boolean validate(K sender, T value, MessageSender messageSender) {
        return true;
    }

    @Deprecated
    default Collection<String> tabCompletes(K sender, String[] previousArguments, String s) {
        throw new IllegalArgumentException("This method is deprecated and should not be used anymore!");
    }

    default Collection<String> tabCompletes(K sender, PreviousArguments previousArguments, String s) {
        return tabCompletes(sender, previousArguments.userArgs, s);
    }

    /**
     * Normalize the cache key by sender and user provided argument. <br>
     * Note: The CommandSender can be null! <br>
     * Returning null and the empty string are equivalent.
     */
    default String normalize(K sender, String s) {
        return null;
    }
}
