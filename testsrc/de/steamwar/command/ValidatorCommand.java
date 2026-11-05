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
import de.steamwar.command.dto.TestSWCommand;
import de.steamwar.command.dto.TestValidator;

public class ValidatorCommand extends TestSWCommand {

    public ValidatorCommand() {
        super("testvalidator");
    }

    @Register
    public void test(@Validator String sender) {
        throw new ExecutionIdentifier("RunTest");
    }

    @Override
    protected void sendMessage(String sender, String message, Object[] args) {
        if (message.equals("Hello World")) {
            throw new ExecutionIdentifier("RunSendMessageWithHelloWorldParameter");
        }
    }

    @Register
    public void onError(String sender, @ErrorMessage("Hello World") int error) {
        System.out.println("onError: " + sender + " " + error);
        throw new ExecutionIdentifier("RunOnError");
    }

    @Register
    public void onError(String sender, double error) {
        System.out.println("onError: " + sender + " " + error);
        throw new ExecutionIdentifier("RunOnErrorDouble");
    }

    @ClassValidator(value = String.class, local = true)
    public TestValidator<String> validator() {
        return (sender, value, messageSender) -> {
            throw new ExecutionIdentifier("RunValidator");
        };
    }
}
