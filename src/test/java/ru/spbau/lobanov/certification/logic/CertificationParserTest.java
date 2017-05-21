package ru.spbau.lobanov.certification.logic;

import org.junit.Test;
import ru.spbau.lobanov.certification.logic.CertificationParser.CertificationFormatException;
import ru.spbau.lobanov.certification.primitives.ArgumentInfo;
import ru.spbau.lobanov.certification.primitives.CommandInfo;
import ru.spbau.lobanov.certification.primitives.Tip;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

public class CertificationParserTest {
    @Test
    public void oneCommandManyArguments() throws Exception {
        InputStream inputStream = CertificationParserTest.class.getResourceAsStream("/test1.info");
        List<CommandInfo> list = new CertificationParser(50).parse(inputStream);
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).getArguments().size());
        assertEquals(3, list.get(0).getTips().size());
        String[] arguments = list.get(0).getArguments().stream()
                .map(ArgumentInfo::getArgumentName)
                .sorted()
                .toArray(String[]::new);
        assertArrayEquals(new String[]{"argument", "argument2"}, arguments);
        String[] tips = list.get(0).getTips().stream()
                .map(Tip::getMessage)
                .sorted()
                .toArray(String[]::new);
        assertArrayEquals(new String[]{"tip", "tip 3", "tip2"}, tips);
    }

    @Test
    public void manyCommands() throws Exception {
        InputStream inputStream = CertificationParserTest.class.getResourceAsStream("/test2.info");
        List<CommandInfo> list = new CertificationParser(50).parse(inputStream);
        assertEquals(4, list.size());
        String[] tips = list.stream()
                .map(CommandInfo::getName)
                .sorted()
                .toArray(String[]::new);
        assertArrayEquals(new String[]{"command1", "command2", "command3", "command4"}, tips);
    }

    @Test(expected = CertificationFormatException.class)
    public void missedName() throws Exception {
        InputStream inputStream = CertificationParserTest.class.getResourceAsStream("/test3.info");
        List<CommandInfo> list = new CertificationParser(50).parse(inputStream);
    }

    @Test(expected = CertificationFormatException.class)
    public void wrongFormat() throws Exception {
        InputStream inputStream = CertificationParserTest.class.getResourceAsStream("/test4.info");
        List<CommandInfo> list = new CertificationParser(50).parse(inputStream);
    }
}