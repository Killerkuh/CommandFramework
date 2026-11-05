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

public class StaticValueCommand extends TestSWCommand {

    public StaticValueCommand() {
        super("staticvalue");
    }

    @Register
    public void defaultStaticValue(String s, @StaticValue({"hello", "world"}) String staticValue) {
        throw new ExecutionIdentifier("RunStaticValue with " + staticValue);
    }

    @Register
    public void booleanStaticValue(String s, @StaticValue(value = {"-a", "-b", "-c"}, allowISE = true) boolean staticValue) {
        throw new ExecutionIdentifier("RunStaticValue with " + staticValue);
    }

    @Register
    public void booleanStaticValueOtherFalseValue(String s, @StaticValue(value = {"-d", "-e", "-f"}, allowISE = true, falseValues = {1}) boolean staticValue) {
        throw new ExecutionIdentifier("RunStaticValue with " + staticValue);
    }

    @Register
    public void intStaticValue(String s, @StaticValue(value = {"-g", "-h", "-i"}, allowISE = true) int staticValue) {
        throw new ExecutionIdentifier("RunStaticValue with int " + staticValue);
    }

    @Register
    public void longStaticValue(String s, @StaticValue(value = {"-j", "-k", "-l"}, allowISE = true) long staticValue) {
        throw new ExecutionIdentifier("RunStaticValue with long " + staticValue);
    }
}
