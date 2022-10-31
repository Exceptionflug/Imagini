package de.exceptionflug.imagini.utils;

import com.google.common.collect.Ordering;
import org.checkerframework.checker.index.qual.PolyUpperBound;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public final class FileUtils {

    private FileUtils() {}

    private static final Ordering<File> FILE_ORDERING = Ordering.from((o1, o2) -> {
        try {
            BasicFileAttributes attributes1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
            BasicFileAttributes attributes2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
            return Long.compare(attributes2.creationTime().toMillis(), attributes1.creationTime().toMillis());
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    });

    public static Ordering<File> getFileOrdering() {
        return FILE_ORDERING;
    }

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