package de.exceptionflug.imagini.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class FileUtils {

    private FileUtils() {}

    public static String getFileContents(final InputStream resourceAsStream) {
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
            final StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
                line = bufferedReader.readLine();
            }
            return stringBuilder.toString();
        } catch (final Exception e) {
            final StringBuilder sb = new StringBuilder(e.getClass().getName()+": "+(e.getMessage() != null ? e.getMessage() : "")+"\n");
            for(final StackTraceElement ee : e.getStackTrace()) {
                sb.append("    ").append(ee.toString()).append("\n");
            }
            return sb.toString();
        }
    }
}