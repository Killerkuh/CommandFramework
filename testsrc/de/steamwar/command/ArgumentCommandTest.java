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

public class ArgumentCommandTest {

    @Test
    public void testNoArgs() {
        ArgumentCommand cmd = new ArgumentCommand();
        try {
            cmd.execute("test", "", new String[0]);
        } catch (Exception e) {
            throw new AssertionError("No exception expected");
        }
    }

    @Test
    public void testBoolean() {
        ArgumentCommand cmd = new ArgumentCommand();
        try {
            cmd.execute("test", "", new String[]{"true", "false"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunArgument with Boolean");
        }
    }

    @Test
    public void testFloat() {
        ArgumentCommand cmd = new ArgumentCommand();
        try {
            cmd.execute("test", "", new String[]{"0.0", "0.0", "0.0"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunArgument with Float");
        }
    }

    @Test
    public void testDouble() {
        ArgumentCommand cmd = new ArgumentCommand();
        try {
            cmd.execute("test", "", new String[]{"0.0", "0.0", "0.0", "0.0"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunArgument with Double");
        }
    }

    // @Test
    public void testInt() {
        ArgumentCommand cmd = new ArgumentCommand();
        try {
            cmd.execute("test", "", new String[]{"0"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunArgument with Integer");
        }
    }

    @Test
    public void testLong() {
        ArgumentCommand cmd = new ArgumentCommand();
        try {
            cmd.execute("test", "", new String[]{"0", "0"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunArgument with Long");
        }
    }

    @Test
    public void testTabComplete() {
        ArgumentCommand cmd = new ArgumentCommand();
        List<String> strings = cmd.tabComplete("test", "", new String[]{""});
        assertTabCompletes(strings, "true", "false", "hello", "wor");
    }

    @Test
    public void testPartialTabComplete() {
        ArgumentCommand cmd = new ArgumentCommand();
        List<String> strings = cmd.tabComplete("test", "", new String[]{"t"});
        assertTabCompletes(strings, "true", "t");

        strings = cmd.tabComplete("test", "", new String[]{"h"});
        assertTabCompletes(strings, "h", "hello");

        strings = cmd.tabComplete("test", "", new String[]{"hel"});
        assertTabCompletes(strings, "hel", "hello");

        strings = cmd.tabComplete("test", "", new String[]{"w"});
        assertTabCompletes(strings, "w", "wor");

        strings = cmd.tabComplete("test", "", new String[]{"wor"});
        assertTabCompletes(strings, "wor");

        strings = cmd.tabComplete("test", "", new String[]{"worl"});
        assertTabCompletes(strings, "wor", "worl");

        strings = cmd.tabComplete("test", "", new String[]{"one", "two", "three", "one"});
        assertTabCompletes(strings);
    }
}
