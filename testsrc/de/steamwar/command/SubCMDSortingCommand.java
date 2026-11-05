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

import java.util.Collections;

public class SubCMDSortingCommand extends TestSWCommand {

    public SubCMDSortingCommand() {
        super("subcmdsorting");
    }

    @Register
    public void test(String s) {
        throw new ExecutionIdentifier("Command with 0 parameters");
    }

    @Register
    public void test(String s, String args) {
        throw new ExecutionIdentifier("Command with 1 parameter");
    }

    @Register
    public void test(String s, String i1, String i2, String i3, String... args) {
        throw new ExecutionIdentifier("Command with 3+n parameters");
    }

    @Register
    public void test(String s, String... args) {
        throw new ExecutionIdentifier("Command with n parameters");
    }

    @ClassMapper(value = String.class, local = true)
    public AbstractTypeMapper<String, String> stringMapper() {
        return SWCommandUtils.createMapper(s -> s, Collections::singletonList);
    }
}
