package de.exceptionflug.imagini.application;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.AbstractResponse;
import de.exceptionflug.moon.response.TextResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

        File file = new File("content/"+account.getName()+"/"+fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();
        byte[] data = ByteStreams.toByteArray(request.getHttpExchange().getRequestBody());
        try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            bufferedOutputStream.write(data);
        }

        account.setLastAccessAddress(imaginiServer.getRemoteAddress(request.getHttpExchange()));
        imaginiServer.saveConfig();
        rewriteLoc(account.getRedirectUrl()+fileName, request.getHttpExchange());
        log.info(account.getName()+" uploaded file: "+fileName);
        return new TextResponse(account.getRedirectUrl()+fileName, "text/plain", 200);
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
