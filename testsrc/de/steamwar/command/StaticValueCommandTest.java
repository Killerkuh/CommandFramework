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

public class StaticValueCommandTest {

    @Test
    public void tabCompletionTest() {
        StaticValueCommand cmd = new StaticValueCommand();
        List<String> strings = cmd.tabComplete("", "", new String[]{""});
        assertTabCompletes(strings, "hello", "world", "-a", "-b", "-c", "-d", "-e", "-f", "-g", "-h", "-i", "-j", "-k", "-l");
    }

    @Test
    public void defaultTest() {
        StaticValueCommand cmd = new StaticValueCommand();
        try {
            cmd.execute("", "", new String[]{"hello"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with hello");
        }
        try {
            cmd.execute("", "", new String[]{"world"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with world");
        }
    }

    @Test
    public void booleanTest() {
        StaticValueCommand cmd = new StaticValueCommand();
        try {
            cmd.execute("", "", new String[]{"-a"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with false");
        }
        try {
            cmd.execute("", "", new String[]{"-b"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with true");
        }
        try {
            cmd.execute("", "", new String[]{"-c"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with true");
        }
    }

    @Test
    public void booleanOtherFalseTest() {
        StaticValueCommand cmd = new StaticValueCommand();
        try {
            cmd.execute("", "", new String[]{"-d"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with true");
        }
        try {
            cmd.execute("", "", new String[]{"-e"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with false");
        }
        try {
            cmd.execute("", "", new String[]{"-f"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with true");
        }
    }

    @Test
    public void intTest() {
        StaticValueCommand cmd = new StaticValueCommand();
        try {
            cmd.execute("", "", new String[]{"-g"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with int 0");
        }
        try {
            cmd.execute("", "", new String[]{"-h"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with int 1");
        }
        try {
            cmd.execute("", "", new String[]{"-i"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with int 2");
        }
    }

    @Test
    public void longTest() {
        StaticValueCommand cmd = new StaticValueCommand();
        try {
            cmd.execute("", "", new String[]{"-j"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with long 0");
        }
        try {
            cmd.execute("", "", new String[]{"-k"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with long 1");
        }
        try {
            cmd.execute("", "", new String[]{"-l"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunStaticValue with long 2");
        }
    }
}