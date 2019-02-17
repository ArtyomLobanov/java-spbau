package ru.spbau.lobanov.certification.logic;

import ru.spbau.lobanov.certification.primitives.ArgumentInfo;
import ru.spbau.lobanov.certification.primitives.CommandInfo;
import ru.spbau.lobanov.certification.primitives.Tip;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Parser, which parse information about commands
 * <p>
 * Command description format:
 * "@Command [name] [description] (Tip|Argument)*"
 * Argument description format:
 * "@Argument [name] [description]"
 * Tip description format:
 * "@Tip [message]"
 */
public class CertificationParser {
    static final String LINE_SEPARATOR = "#";
    private int lineLengthLimit;

    CertificationParser(int lineLengthLimit) {
        this.lineLengthLimit = lineLengthLimit;
    }

    /**
     * Parse information about commands from input stream
     *
     * @param input source of information
     * @return List of CommandInfo, described commands
     * @throws CertificationFormatException if data had wrong format
     */
    List<CommandInfo> parse(InputStream input) throws CertificationFormatException {
        List<CommandInfo> commands = new ArrayList<>();
        try (Scanner scanner = new Scanner(input, "utf-8")) {
            while (scanner.hasNext()) {
                String token = scanner.next();
                if ("@Command".equals(token)) {
                    commands.add(parseCommand(scanner));
                } else {
                    throw new CertificationFormatException("Wrong format: @Command expected, " + token + " found");
                }
            }
        }
        return commands;
    }

    /**
     * Parse information about commands from resource-file
     *
     * @param resource name of resource-file
     * @return List of CommandInfo, described commands
     * @throws CertificationFormatException if data had wrong format
     */
    List<CommandInfo> parse(String resource) throws CertificationFormatException {
        return parse(CertificationParser.class.getResourceAsStream(resource));
    }

    private CommandInfo parseCommand(Scanner scanner) throws CertificationFormatException {
        if (!scanner.hasNext("[^@].*")) {
            throw new CertificationFormatException("Command name missed");
        }
        String name = scanner.next();
        String message = parseMessage(scanner);
        List<Tip> tips = new ArrayList<>();
        List<ArgumentInfo> arguments = new ArrayList<>();
        while (scanner.hasNext("@((Tip)|(Argument))")) {
            String token = scanner.next();
            switch (token) {
                case "@Tip":
                    tips.add(parseTip(scanner));
                    break;
                case "@Argument":
                    arguments.add(parseArgument(scanner));
                    break;
            }
        }
        return new CommandInfo(name, message, arguments, tips);
    }

    private Tip parseTip(Scanner scanner) {
        return new Tip(parseMessage(scanner));
    }

    private ArgumentInfo parseArgument(Scanner scanner) {
        String argumentName = scanner.next();
        String message = parseMessage(scanner);
        return new ArgumentInfo(argumentName, message);
    }

    private String parseMessage(Scanner scanner) {
        StringBuilder message = new StringBuilder();
        int currentLength = 0;
        while (scanner.hasNext("[^@].*")) {
            String token = scanner.next();
            if (currentLength + token.length() + (currentLength > 0 ? 1 : 0) > lineLengthLimit) {
                message.append(LINE_SEPARATOR);
                currentLength = 0;
            } else if (currentLength > 0) {
                message.append(' ');
            }
            currentLength += token.length();
            message.append(token);
        }
        return message.toString();
    }

    public static class CertificationFormatException extends Exception {
        private CertificationFormatException(String message) {
            super(message);
        }
    }
}
