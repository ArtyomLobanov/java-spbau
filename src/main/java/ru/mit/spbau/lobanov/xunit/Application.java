package ru.mit.spbau.lobanov.xunit;

import org.jetbrains.annotations.NotNull;
import ru.mit.spbau.lobanov.xunit.testing.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Application {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Expected format: test_pack_name path1 path2 path3");
        }
        String className = args[0];
        Tester tester;
        try {
            URL[] urls = Arrays.stream(args)
                    .sequential()
                    .skip(1)
                    .map(Paths::get)
                    .map(Path::toUri)
                    .map(Application::UriToURL)
                    .toArray(URL[]::new);
            Thread.currentThread().setContextClassLoader(new URLClassLoader(urls));
            tester = new Tester(className);
            tester.runTests();
        } catch (TestingException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        } catch (RuntimeException e) {
            System.out.println("Error: failed to build URL to target directory");
            return;
        }
        System.out.println("-------------------ERROR----LOGS------------------");
        System.out.println(tester.getErrorMessages().size() + " errors occurred during testing");
        for (ErrorMessage errorMessage : tester.getErrorMessages()) {
            System.out.print(errorMessage.getTest() != null ? errorMessage.getTest().getName() : "general");
            System.out.print("\t:\t");
            System.out.println(errorMessage.getMessage());
        }
        System.out.println("------------------WARNING----LOGS------------------");
        System.out.println(tester.getWarningMessages().size() + " warning occurred during testing");
        for (WarningMessage warningMessage : tester.getWarningMessages()) {
            System.out.print(warningMessage.getTest() != null ? warningMessage.getTest().getName() : "general");
            System.out.print("\t:\t");
            System.out.println(warningMessage.getMessage());
        }
        System.out.println("------------------SUCCESS----LOGS------------------");
        System.out.println(tester.getSuccessMessages().size() + " test successfully passed");
        for (SuccessMessage successMessage : tester.getSuccessMessages()) {
            System.out.print(successMessage.getTest().getName());
            System.out.print("\t:\t");
            System.out.print(successMessage.getMessage());
            System.out.print("\t:\t");
            System.out.println(successMessage.getTime() + " mills");
        }
    }

    @NotNull
    private static URL UriToURL(@NotNull URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
