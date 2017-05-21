package ru.spbau.lobanov.certification.primitives;

import java.util.Collections;
import java.util.List;

/**
 * Data-class, which contains information about
 * some command. It contains name of command,
 * some comment about it command, and also
 * lists of tips and arguments descriptions
 */
public class CommandInfo {
    private final String name;
    private final String generalInfo;
    private final List<ArgumentInfo> arguments;
    private final List<Tip> tips;

    public CommandInfo(String name, String generalInfo, List<ArgumentInfo> arguments, List<Tip> tips) {
        this.name = name;
        this.generalInfo = generalInfo;
        this.arguments = arguments;
        this.tips = tips;
    }

    public String getName() {
        return name;
    }

    public String getGeneralInfo() {
        return generalInfo;
    }

    public List<ArgumentInfo> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public List<Tip> getTips() {
        return Collections.unmodifiableList(tips);
    }
}
