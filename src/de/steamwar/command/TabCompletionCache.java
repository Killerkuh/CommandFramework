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
import lombok.EqualsAndHashCode;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@UtilityClass
public class TabCompletionCache {

    private Map<Key, TabCompletions> tabCompletionCache = new HashMap<>();
    Set<AbstractTypeMapper<?, ?>> cached = new HashSet<>();
    Set<AbstractTypeMapper<?, ?>> global = new HashSet<>();
    Map<AbstractTypeMapper<?, ?>, Long> cacheDuration = new HashMap<>();

    void add(AbstractTypeMapper<?, ?> typeMapper, AbstractSWCommand.Cached cached) {
        if (cached != null) {
            add(typeMapper, cached.global(), cached.cacheDuration(), cached.timeUnit());
        }
    }

    public void add(AbstractTypeMapper<?, ?> typeMapper, boolean global, long cacheDuration, TimeUnit timeUnit) {
        TabCompletionCache.cached.add(typeMapper);
        if (global) TabCompletionCache.global.add(typeMapper);
        TabCompletionCache.cacheDuration.put(typeMapper, timeUnit.toMillis(cacheDuration));
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Key {
        private Object sender;
        private String arg;
        private AbstractTypeMapper<?, ?> typeMapper;
    }

    @AllArgsConstructor
    private static class TabCompletions {
        private long timestamp;
        private Collection<String> tabCompletions;
    }

    Collection<String> tabComplete(Object sender, String arg, AbstractTypeMapper<Object, ?> typeMapper, Supplier<Collection<String>> tabCompleteSupplier) {
        if (!cached.contains(typeMapper)) return tabCompleteSupplier.get();

        String normalizedArg = typeMapper.normalize(sender, arg);
        if (normalizedArg == null) normalizedArg = "";
        Key key = new Key(global.contains(typeMapper) ? null : sender, normalizedArg, typeMapper);

        TabCompletions tabCompletions = tabCompletionCache.computeIfAbsent(key, ignore -> {
            return new TabCompletions(System.currentTimeMillis(), tabCompleteSupplier.get());
        });

        if (System.currentTimeMillis() - tabCompletions.timestamp > cacheDuration.get(typeMapper)) {
            tabCompletions.tabCompletions = tabCompleteSupplier.get();
        }
        tabCompletions.timestamp = System.currentTimeMillis();
        return tabCompletions.tabCompletions;
    }

    public void invalidateOldEntries() {
        Set<Key> toRemove = new HashSet<>();
        for (Map.Entry<Key, TabCompletions> tabCompletionsEntry : tabCompletionCache.entrySet()) {
            if (System.currentTimeMillis() - tabCompletionsEntry.getValue().timestamp > cacheDuration.get(tabCompletionsEntry.getKey().typeMapper)) {
                toRemove.add(tabCompletionsEntry.getKey());
            }
        }
        for (Key key : toRemove) {
            tabCompletionCache.remove(key);
        }
    }
}
