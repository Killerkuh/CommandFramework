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

package de.steamwar;

import de.steamwar.command.CommandFrameworkException;
import lombok.experimental.UtilityClass;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@UtilityClass
public class AssertionUtils {

    public static void assertCMDFramework(Exception e, Class<?> clazz, String message) {
        assertThat(e, is(instanceOf(CommandFrameworkException.class)));
        assertThat(e.getCause().getCause(), is(instanceOf(clazz)));
        assertThat(e.getCause().getCause().getMessage(), is(message));
    }

    public static <T> void assertTabCompletes(List<T> list, T... elements) {
        assertThat(list.size(), is(elements.length));
        if (elements.length > 0) {
            assertThat(list, containsInAnyOrder(elements));
        }
    }
}
