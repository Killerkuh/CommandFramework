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

import static de.steamwar.AssertionUtils.assertCMDFramework;

public class SubCMDSortingCommandTest {

    @Test
    public void testNoArgs() {
        SubCMDSortingCommand cmd = new SubCMDSortingCommand();
        try {
            cmd.execute("", "", new String[]{});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "Command with 0 parameters");
        }
    }

    @Test
    public void testOneArgs() {
        SubCMDSortingCommand cmd = new SubCMDSortingCommand();
        try {
            cmd.execute("", "", new String[]{"Hello"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "Command with 1 parameter");
        }
    }

    @Test
    public void testOneArgsVarArg() {
        SubCMDSortingCommand cmd = new SubCMDSortingCommand();
        try {
            cmd.execute("", "", new String[]{"Hello", "World"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "Command with n parameters");
        }
    }

    @Test
    public void testThreeArgsVarArg() {
        SubCMDSortingCommand cmd = new SubCMDSortingCommand();
        try {
            cmd.execute("", "", new String[]{"Hello", "World", "YoyoNow", "Hugo"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "Command with 3+n parameters");
        }
    }

    @Test
    public void testThreeArgsVarArg2() {
        SubCMDSortingCommand cmd = new SubCMDSortingCommand();
        try {
            cmd.execute("", "", new String[]{"Hello", "World", "YoyoNow"});
            assert false;
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "Command with 3+n parameters");
        }
    }
}
