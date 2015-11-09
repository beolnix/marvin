package com.beolnix.marvin.im;

import java.util.Optional;

/**
 * Created by DAtmakin on 11/3/2015.
 */
public class IMSessionUtils {
    public boolean isCommand(String msg, String commandSymbol) {
        return msg!= null && commandSymbol != null && msg.startsWith(commandSymbol);
    }

    public Optional<String> parseCommand(String msg, String commandSymbol) {
        if (msg != null && commandSymbol != null && msg.startsWith(commandSymbol)) {
            String[] rawMsg = msg.split(" ");
            String command = rawMsg[0].replace(commandSymbol, "");
            return Optional.of(command);
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> parseCommandAttributes(String msg, String command, String commandSymbol) {
        if (msg != null && commandSymbol != null && command != null && msg.startsWith(commandSymbol + command)) {
            String commandAttributes = msg.substring(command.length() + 1).trim();
            return Optional.of(commandAttributes);
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> parseCommandAttributes(String msg, String commandSymbol) {
        Optional<String> commandOpt = parseCommand(msg, commandSymbol);
        if (!commandOpt.isPresent()) {
            return Optional.empty();
        }

        String command = commandOpt.get();

        if (msg != null && commandSymbol != null && command != null && msg.startsWith(commandSymbol + command)) {
            String commandAttributes = msg.substring(command.length() + 1).trim();
            return Optional.of(commandAttributes);
        } else {
            return Optional.empty();
        }
    }
}
