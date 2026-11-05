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

public class ValidatorInvertCommand extends TestSWCommand {

    public ValidatorInvertCommand() {
        super("testvalidator");
    }

    @Register
    public void test(@Validator(value = "Text", invert = true) String sender, boolean b, boolean b2) {
        System.out.println("test: " + sender + " " + b + " " + b2);
        throw new ExecutionIdentifier("RunTestInvert");
    }

    @Register
    public void test(String sender, @Validator(value = "Text", invert = true) String h, int i) {
        System.out.println("test: " + sender + " " + h + " " + i);
        throw new ExecutionIdentifier("RunTestInvert2");
    }

    @Validator(value = "Text", local = true)
    public TestValidator<String> testValidator() {
        return (sender, value, messageSender) -> {
            System.out.println("testValidator: " + sender + " " + value + " " + messageSender);
            return false;
        };
    }
}
