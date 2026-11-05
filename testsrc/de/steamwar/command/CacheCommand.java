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

import de.steamwar.command.dto.TestSWCommand;
import de.steamwar.command.dto.TestTypeMapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheCommand extends TestSWCommand {

    public CacheCommand() {
        super("typemapper");
    }

    @Register
    public void test(String sender, int tabCompleteTest) {
    }

    private AtomicInteger count = new AtomicInteger();

    @Cached
    @Mapper(value = "int", local = true)
    public AbstractTypeMapper<String, Integer> typeMapper() {
        return new TestTypeMapper<Integer>() {
            @Override
            public Integer map(String sender, PreviousArguments previousArguments, String s) {
                return Integer.parseInt(s);
            }

            @Override
            public Collection<String> tabCompletes(String sender, PreviousArguments previousArguments, String s) {
                return Arrays.asList(count.getAndIncrement() + "");
            }
        };
    }
}
