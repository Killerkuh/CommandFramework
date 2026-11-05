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

import de.steamwar.command.dto.ExecutionIdentifier;
import de.steamwar.command.dto.TestSWCommand;
import de.steamwar.command.dto.TestTypeMapper;

import java.util.Arrays;
import java.util.Collection;

public class PreviousArgumentCommand extends TestSWCommand {

    public PreviousArgumentCommand() {
        super("previous");
    }

    @Register
    public void genericCommand(String s, int i, String l) {
        throw new ExecutionIdentifier(l);
    }

    @ClassMapper(value = String.class, local = true)
    public TestTypeMapper<String> typeMapper() {
        return new TestTypeMapper<String>() {
            @Override
            public String map(String sender, PreviousArguments previousArguments, String s) {
                return "RunTypeMapper_" + previousArguments.getMappedArg(0) + "_" + previousArguments.getMappedArg(0).getClass().getSimpleName();
            }

            @Override
            public Collection<String> tabCompletes(String sender, PreviousArguments previousArguments, String s) {
                return Arrays.asList(previousArguments.getMappedArg(0) + "");
            }
        };
    }
}
