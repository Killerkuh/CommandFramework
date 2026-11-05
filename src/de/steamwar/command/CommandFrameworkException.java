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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public class CommandFrameworkException extends RuntimeException {

    private Function causeMessage;
    private Throwable cause;
    private Function stackTraceExtractor;
    private String extraStackTraces;

    private String message;

    static CommandFrameworkException commandPartExceptions(String type, Throwable cause, String current, Class<?> clazzType, Executable executable, int index) {
        return new CommandFrameworkException(e -> {
            return CommandFrameworkException.class.getTypeName() + ": Error while " + type + " (" + current + ") to type " + clazzType.getTypeName() + " with parameter index " + index;
        }, cause, exception -> {
            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            int last = 0;
            for (int i = 0; i < stackTraceElements.length; i++) {
                if (stackTraceElements[i].getClassName().equals(CommandPart.class.getTypeName())) {
                    last = i;
                    break;
                }
            }
            return Arrays.stream(stackTraceElements).limit(last - 1);
        }, executable.getDeclaringClass().getTypeName() + "." + executable.getName() + "(Unknown Source)");
    }

    CommandFrameworkException(InvocationTargetException invocationTargetException, String alias, String[] args) {
        this(e -> {
            StringBuilder st = new StringBuilder();
            st.append(e.getCause().getClass().getTypeName());
            if (e.getCause().getMessage() != null) {
                st.append(": ").append(e.getCause().getMessage());
            }
            if (alias != null && !alias.isEmpty()) {
                st.append("\n").append("Performed command: " + alias + " " + String.join(" ", args));
            }
            return st.toString();
        }, invocationTargetException, e -> {
            StackTraceElement[] stackTraceElements = e.getCause().getStackTrace();
            return Arrays.stream(stackTraceElements).limit(stackTraceElements.length - e.getStackTrace().length);
        }, null);
    }

    private <T extends Throwable> CommandFrameworkException(Function<T, String> causeMessage, T cause, Function<T, Stream<StackTraceElement>> stackTraceExtractor, String extraStackTraces) {
        super(causeMessage.apply(cause), cause);
        this.causeMessage = causeMessage;
        this.cause = cause;
        this.stackTraceExtractor = stackTraceExtractor;
        this.extraStackTraces = extraStackTraces;
    }

    public synchronized String getBuildStackTrace() {
        if (message != null) {
            return message;
        }
        StringBuilder st = new StringBuilder();
        st.append(causeMessage.apply(cause)).append("\n");
        ((Stream<StackTraceElement>) stackTraceExtractor.apply(cause)).forEach(stackTraceElement -> {
            st.append("\tat ").append(stackTraceElement.toString()).append("\n");
        });
        if (extraStackTraces != null) {
            st.append("\tat ").append(extraStackTraces).append("\n");
        }
        message = st.toString();
        return message;
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        s.print(getBuildStackTrace());
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        s.print(getBuildStackTrace());
    }
}
