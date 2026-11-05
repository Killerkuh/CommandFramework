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

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CacheCommandTest {

    @Test
    public void testCaching() {
        CacheCommand cmd = new CacheCommand();
        List<String> tabCompletions1 = cmd.tabComplete("test", "", new String[]{""});
        List<String> tabCompletions2 = cmd.tabComplete("test", "", new String[]{""});
        assertThat(tabCompletions1, is(equalTo(tabCompletions2)));
    }

    @Test
    public void testCachingWithDifferentMessages() {
        CacheCommand cmd = new CacheCommand();
        List<String> tabCompletions1 = cmd.tabComplete("test", "", new String[]{""});
        List<String> tabCompletions2 = cmd.tabComplete("test", "", new String[]{"0"});
        assertThat(tabCompletions1, is(equalTo(tabCompletions2)));
    }

    @Test
    public void testCachingWithDifferentSenders() {
        CacheCommand cmd = new CacheCommand();
        List<String> tabCompletions1 = cmd.tabComplete("test", "", new String[]{""});
        List<String> tabCompletions2 = cmd.tabComplete("test2", "", new String[]{""});
        assertThat(tabCompletions1, is(not(equalTo(tabCompletions2))));
    }
}
