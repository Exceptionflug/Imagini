package de.exceptionflug.imagini.application;

import com.sun.net.httpserver.HttpExchange;
import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.AbstractResponse;
import de.exceptionflug.moon.response.TextResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

@Log4j2
public class UploadHandler implements PageHandler<AbstractResponse> {

    private final ImaginiServer imaginiServer;

    public UploadHandler(ImaginiServer imaginiServer) {
        this.imaginiServer = imaginiServer;
    }

    public AbstractResponse handle(AbstractResponse response, Request request) throws Exception {
        if(!request.getHttpExchange().getRequestMethod().equalsIgnoreCase("put")) {
            return new TextResponse("Only PUT is allowed", "text/plain", 400);
        }

        String host = request.getHttpExchange().getRequestHeaders().getFirst("Host");
        String accField = request.getHttpExchange().getRequestHeaders().getFirst("Account");
        if(accField != null) {
            host = accField;
        }
        String accessToken = request.getHttpExchange().getRequestHeaders().getFirst("Access-Token");
        String fileName = request.getHttpExchange().getRequestHeaders().getFirst("File-Name");

        if(host == null) {
            return new TextResponse("Header fields 'Host' or 'Account' not present", "text/plain", 400);
        }

        Account account = imaginiServer.getAccountByAddress(host);
        if(account == null) {
            return new TextResponse("Account "+host+" not found", "text/plain", 404);
        }

        if(account.getAccessToken() != null) {
            if(accessToken == null) {
                return new TextResponse("Header field 'Access-Token' not present", "text/plain", 400);
            }
            if(!Objects.equals(accessToken, account.getAccessToken())) {
                log.warn(request.getHttpExchange().getRemoteAddress().getHostName()+" tried to upload file to account "+
                        account.getName()+" with incorrect access token!");
                return new TextResponse("Unauthorized", "text/plain", 403);
            }
        }

        if(fileName == null) {
            return new TextResponse("Header field 'File-Name' not present", "text/plain", 400);
        }

        String normalizedPath = Paths.get(fileName).normalize().toString();
        String base = new File("content/" + account.getName()).getCanonicalPath();
        File file = new File(base, normalizedPath);
        if (!file.getCanonicalPath().startsWith(base)) {
            return new TextResponse("Header field 'File-Name' not present", "text/plain", 400);
        }
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(request.getHttpExchange().getRequestBody())) {
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                byte[] data = new byte[2048];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(data)) != -1) {
                    bufferedOutputStream.write(data, 0, bytesRead);
                }
            }
        }

        account.setLastAccessAddress(imaginiServer.getRemoteAddress(request.getHttpExchange()));
        imaginiServer.saveConfig();
        rewriteLoc(account.getRedirectUrl()+fileName, request.getHttpExchange());
        log.info(account.getName()+" uploaded file: "+fileName);
        return new TextResponse(account.getRedirectUrl()+htmlEncode(fileName), "text/plain", 200);
    }

    private String htmlEncode(String fileName) {
        return fileName.replace(" ", "%20");
    }

    private void rewriteLoc(String target, HttpExchange httpExchange) {
        if (target.startsWith("/")) {
            try {
                URI uri = (new URIBuilder(httpExchange.getRequestURI())).setPath(target).build();
                target = uri.toASCIIString();
            } catch (URISyntaxException var3) {
                var3.printStackTrace();
            }
        }

        httpExchange.getResponseHeaders().add("Location", target);
    }

}
