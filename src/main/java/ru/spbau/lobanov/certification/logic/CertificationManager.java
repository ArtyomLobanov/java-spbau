package ru.spbau.lobanov.certification.logic;

import ru.spbau.lobanov.certification.primitives.ArgumentInfo;
import ru.spbau.lobanov.certification.primitives.CommandInfo;
import ru.spbau.lobanov.certification.primitives.Tip;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.spbau.lobanov.certification.logic.CertificationParser.LINE_SEPARATOR;

/**
 * Special class, which contains information about
 * available commands in some system
 */
public class CertificationManager {
    private static final int DEFAULT_LINE_LENGTH_LIMIT = 50;

    private final Map<String, CommandInfo> commands;

    /**
     * Create CertificationManager, which got information
     * from resource file
     *
     * @param resourceName resource file
     * @throws CertificationParser.CertificationFormatException if file had wrong format
     */
    public CertificationManager(String resourceName) throws CertificationParser.CertificationFormatException {
        CertificationParser parser = new CertificationParser(DEFAULT_LINE_LENGTH_LIMIT);
        List<CommandInfo> list = parser.parse(resourceName);
        commands = list.stream()
                .collect(Collectors.toMap(CommandInfo::getName, Function.identity()));
    }

    private static String formatCommand(CommandInfo command) {
        String arguments = command.getArguments()
                .stream()
                .map(ArgumentInfo::getArgumentName)
                .map(s -> "[" + s + "]")
                .collect(Collectors.joining(" "));
        return command.getName() + " " + arguments;
    }

    /**
     * Return information about command
     *
     * @param command target command
     * @return description of command
     * @throws CertificateNotFountException if certificate for given command wasn't found
     */
    public String getCertificate(String command) throws CertificateNotFountException {
        CommandInfo commandInfo = commands.get(command);
        if (commandInfo == null) {
            throw new CertificateNotFountException("Certificate wasn't found for command: " + command);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Certificate for command: ")
                .append(commandInfo.getName())
                .append("\n\t\t");
        builder.append(commandInfo.getGeneralInfo().replaceAll(LINE_SEPARATOR, "\n\t\t"))
                .append('\n');
        if (!commandInfo.getArguments().isEmpty()) {
            builder.append("\tArguments:\n");
            for (ArgumentInfo argument : commandInfo.getArguments()) {
                builder.append("\t\t")
                        .append(argument.getArgumentName())
                        .append(" - ")
                        .append(argument.getDescription().replaceAll(LINE_SEPARATOR, "\n\t\t\t"))
                        .append('\n');
            }
        }
        if (!commandInfo.getArguments().isEmpty()) {
            builder.append("\tTips:\n");
            for (Tip tip : commandInfo.getTips()) {
                builder.append("\t\t- ")
                        .append(tip.getMessage().replaceAll(LINE_SEPARATOR, "\n\t\t"))
                        .append('\n');
            }
        }
        return builder.toString();
    }

    /**
     * Return List of name of available commands
     *
     * @return List of names of commands
     */
    public List<String> getCommands() {
        return commands.values().stream()
                .map(CertificationManager::formatCommand)
                .collect(Collectors.toList());
    }

    public static class CertificateNotFountException extends Exception {
        CertificateNotFountException(String message) {
            super(message);
        }
    }
}
