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

package de.steamwar.command.dto;

import de.steamwar.command.AbstractSWCommand;
import de.steamwar.command.CommandFrameworkException;

import java.util.function.Supplier;

public class TestSWCommand extends AbstractSWCommand<String> {

    protected TestSWCommand(String command) {
        super(String.class, command);
    }

    protected TestSWCommand(String command, String[] aliases) {
        super(String.class, command, aliases);
    }

    @Override
    protected void createAndSafeCommand(String command, String[] aliases) {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void register() {

    }

    @Override
    protected void commandSystemError(String sender, CommandFrameworkException e) {
        System.out.println("CommandSystemError: " + e.getMessage());
    }

    @Override
    protected void commandSystemWarning(Supplier<String> message) {
        System.out.println("CommandSystemWarning: " + message.get());
    }
}
