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
import org.junit.Test;

import java.util.List;

import static de.steamwar.AssertionUtils.assertCMDFramework;
import static de.steamwar.AssertionUtils.assertTabCompletes;

public class SimpleCommandTest {

    @Test
    public void testSimpleParsing() {
        SimpleCommand cmd = new SimpleCommand();
        try {
            cmd.execute("test", "", new String[0]);
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunSimple with noArgs");
        }
    }

    @Test
    public void testVarArgs() {
        SimpleCommand cmd = new SimpleCommand();
        try {
            cmd.execute("test", "", new String[]{"a", "b", "c"});
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunSimple with Varargs");
        }
    }

    @Test
    public void testSimpleParsingNoResult() {
        SimpleCommand cmd = new SimpleCommand();
        try {
            cmd.execute("test", "", new String[]{"Hello"});
        } catch (CommandFrameworkException e) {
            throw new AssertionError("No exception expected");
        }
    }

    @Test
    public void testSimpleTabComplete() {
        SimpleCommand cmd = new SimpleCommand();
        List<String> strings = cmd.tabComplete("test", "", new String[]{""});
        assertTabCompletes(strings);
    }
}
