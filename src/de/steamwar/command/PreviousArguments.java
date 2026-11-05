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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreviousArguments {

    public final String[] userArgs;
    public final Object[] mappedArgs;

    public PreviousArguments(String[] userArgs, Object[] mappedArgs) {
        this.userArgs = userArgs;
        this.mappedArgs = mappedArgs;
    }

    public String getUserArg(int index) {
        return userArgs[userArgs.length - index - 1];
    }

    public <T> T getMappedArg(int index) {
        return (T) mappedArgs[mappedArgs.length - index - 1];
    }

    public <T> Optional<T> getFirst(Class<T> clazz) {
        for (Object o : mappedArgs) {
            if (clazz.isInstance(o)) {
                return Optional.of((T) o);
            }
        }
        return Optional.empty();
    }

    public <T> List<T> getAll(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (Object o : mappedArgs) {
            if (clazz.isInstance(o)) {
                list.add((T) o);
            }
        }
        return list;
    }
}
