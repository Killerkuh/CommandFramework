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

public class ValidatorCommandTest {

    @Test
    public void testValidator() {
        ValidatorCommand cmd = new ValidatorCommand();
        try {
            cmd.execute("test", "", new String[0]);
            assertThat(true, is(false));
        } catch (Exception e) {
            assertThat(e.getMessage(), is("RunValidator"));
        }
    }

    @Test
    public void testErrorMessage() {
        ValidatorCommand cmd = new ValidatorCommand();
        try {
            cmd.execute("test", "", new String[]{"Hello"});
            assertThat(true, is(false));
        } catch (Exception e) {
            assertThat(e.getMessage(), is("RunSendMessageWithHelloWorldParameter"));
        }
    }

    @Test
    public void testErrorNoMessage() {
        ValidatorCommand cmd = new ValidatorCommand();
        try {
            cmd.execute("test", "", new String[]{"0.0"});
            assertThat(true, is(false));
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunOnErrorDouble");
        }
    }

    @Test
    public void testInvert() {
        ValidatorInvertCommand cmd = new ValidatorInvertCommand();
        try {
            cmd.execute("test", "", new String[]{"false", "true"});
            assertThat(true, is(false));
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunTestInvert");
        }
    }

    @Test
    public void testInvertOther() {
        ValidatorInvertCommand cmd = new ValidatorInvertCommand();
        try {
            cmd.execute("test", "", new String[]{"Hello", "0"});
            assertThat(true, is(false));
        } catch (Exception e) {
            assertCMDFramework(e, ExecutionIdentifier.class, "RunTestInvert2");
        }
    }
}
