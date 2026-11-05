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

import de.steamwar.command.dto.TestSWCommand;
import de.steamwar.command.dto.TestTypeMapper;

import java.util.Collection;

public class BetterExceptionCommand extends TestSWCommand {

    public BetterExceptionCommand() {
        super("betterexception");
    }

    @Register
    public void exceptionOnTabComplete(String s, @Mapper("exception") @Validator("exception") String s1) {
    }

    @Mapper("exception")
    @Validator("exception")
    public TestTypeMapper<String> tabCompleteException() {
        return new TestTypeMapper<String>() {
            @Override
            public String map(String sender, PreviousArguments previousArguments, String s) {
                return null;
            }

            @Override
            public boolean validate(String sender, String value, MessageSender messageSender) {
                System.out.println("Validate: " + value);
                throw new SecurityException();
            }

            @Override
            public Collection<String> tabCompletes(String sender, PreviousArguments previousArguments, String s) {
                throw new SecurityException();
            }
        };
    }
}
