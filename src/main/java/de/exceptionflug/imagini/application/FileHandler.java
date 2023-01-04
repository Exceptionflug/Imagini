package de.exceptionflug.imagini.application;

import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.AbstractResponse;
import de.exceptionflug.moon.response.BinaryResponse;
import de.exceptionflug.moon.response.NotFoundResponse;
import de.exceptionflug.moon.response.TextResponse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.extern.log4j.Log4j2;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

@Log4j2
public class FileHandler implements PageHandler<AbstractResponse> {

    private static final int PREVIEW_HEIGHT = 500;
    private static final int PREVIEW_WIDTH = 600;

    private final ImaginiServer imaginiServer;

    public FileHandler(ImaginiServer imaginiServer) {
        this.imaginiServer = imaginiServer;
    }

    @Override
    public AbstractResponse handle(AbstractResponse abstractResponse, Request request) throws Exception {
        String host = request.getHttpExchange().getRequestHeaders().getFirst("Host");
        if (host == null) {
            return new TextResponse("Header fields 'Host' or 'Account' not present", "text/plain", 400);
        }
        Account account = imaginiServer.getAccountByAddress(host);
        if (account == null) {
            return new TextResponse("Account " + host + " not found", "text/plain", 404);
        }
        String normalizedPath = Paths.get(request.getHttpExchange().getRequestURI().getPath()).normalize().toString();
        String base = new File("content/" + account.getName()).getCanonicalPath();
        File file = new File(base, normalizedPath);
        if (!file.exists() || !file.getCanonicalPath().startsWith(base)) {
            return new NotFoundResponse(request.getHttpExchange().getRequestURI());
        }
        if (file.isDirectory()) {
            return new TextResponse("<h2>Imagini File Server</h2><br>Current account: " + account.getName() + "<hr><i>&copy; Nico Britze 2020 - 2023</i>", "text/html", 200);
        }
        boolean logging = (request.getQueryParameter("logging") == null || !request.getQueryParameter("logging").equalsIgnoreCase("false")) || account.isDisallowLogDisable();
        if (request.getQueryParameter("preview") != null && request.getQueryParameter("preview").equalsIgnoreCase("true")) {
            return createPreview(file);
        }
        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file))) {
            if (logging) {
                Account accessor = imaginiServer.getAccountByLastAccessAddress(imaginiServer.getRemoteAddress(request.getHttpExchange()));
                if (accessor != null) {
                    log.info(accessor.getName() + " [" + request.getHttpExchange().getRemoteAddress().getHostString() + "] accessed file " + file.getName() + " of account " + account.getName());
                } else {
                    log.info("[" + imaginiServer.getRemoteAddress(request.getHttpExchange()) + "] accessed file " + file.getName() + " of account " + account.getName());
                }
            }
            return new BinaryResponse(fileInputStream.readAllBytes(), Files.probeContentType(file.toPath()));
        }
    }

    private AbstractResponse createPreview(File file) throws IOException {
        var mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null || !mimeType.startsWith("image")) {
            return new BinaryResponse(ImaginiServer.class.getResourceAsStream("/file.svg").readAllBytes(), "image/svg+xml");
        }
        if (!mimeType.equals("image/png")) {
            return new BinaryResponse(Files.readAllBytes(file.toPath()), mimeType);
        }
        try {
            var image = ImageIO.read(file);
            if (image.getHeight() < PREVIEW_HEIGHT && image.getWidth() < PREVIEW_WIDTH) {
                return new BinaryResponse(Files.readAllBytes(file.toPath()), mimeType);
            }
            double ratio = (double) image.getWidth() / image.getHeight();
            image = resizeImage(image, (int) (PREVIEW_HEIGHT * ratio), PREVIEW_HEIGHT);
            var out = new ByteArrayOutputStream();
            ImageIO.write(image, file.getName().substring(file.getName().lastIndexOf(".") + 1), out);
            return new BinaryResponse(out.toByteArray(), mimeType);
        } catch (IIOException exception) {
            return new BinaryResponse(Files.readAllBytes(file.toPath()), mimeType);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        var resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        var outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

}
