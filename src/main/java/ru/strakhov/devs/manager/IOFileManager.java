package ru.strakhov.devs.manager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOFileManager {
    public static String readFile(String inputFileName) throws IOException {
        Path inputFileNamePath = Paths.get(inputFileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFileNamePath.toFile()));
        String line = "";
        while (bufferedReader.ready()) {
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return line;
    }

    public static void createFile(String fileName, String content) {
        try {
            Path jarDirectory = Paths.get(IOFileManager.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent();

            Path filePath = jarDirectory.resolve(fileName);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content);

        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать файл: " + fileName, e);
        }
    }
}
