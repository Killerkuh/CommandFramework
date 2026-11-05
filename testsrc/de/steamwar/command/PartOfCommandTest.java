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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PartOfCommandTest {

    @Test
    public void testMerging() {
        PartOfCommand.ParentCommand command = new PartOfCommand.ParentCommand();
        new PartOfCommand.SubCommand();
        try {
            command.execute("test", "", new String[]{"0"});
            assertThat(true, is(false));
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "SubCommand with int -1");
        }
    }
}
