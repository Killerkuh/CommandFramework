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

public class ArgumentCommand extends TestSWCommand {

    public ArgumentCommand() {
        super("argument");
    }

    @Register
    public void argument(String sender, boolean b, boolean b2) {
        throw new ExecutionIdentifier("RunArgument with Boolean");
    }

    @Register
    public void argument(String sender, float f, float f2, float f3) {
        throw new ExecutionIdentifier("RunArgument with Float");
    }

    @Register
    public void argument(String sender, double d, double d2, double d3, double d4) {
        throw new ExecutionIdentifier("RunArgument with Double");
    }

    @Register
    public void argument(String sender, int i) {
        throw new ExecutionIdentifier("RunArgument with Integer");
    }

    @Register
    public void argument(String sender, long l, long l2) {
        throw new ExecutionIdentifier("RunArgument with Long");
    }

    @Register
    public void argument(String sender, String arg) {
        throw new ExecutionIdentifier("RunArgument with String");
    }

    @Register
    public void minLengthArgument(String sender, @Length(min = 3) @StaticValue({"he", "hello"}) String arg) {
        throw new ExecutionIdentifier("RunLengthArgument with String");
    }

    @Register
    public void minAndMaxLengthArgument(String sender, @Length(min = 3, max = 3) @StaticValue({"wo", "world"}) String arg) {
        throw new ExecutionIdentifier("RunLengthArgument with String");
    }

    public void arrayLengthArgument(String sender, @ArrayLength(max = 3) @StaticValue({"one", "two", "three"}) String... args) {
        throw new ExecutionIdentifier("RunArrayLengthArgument with String");
    }
}
