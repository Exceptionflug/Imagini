package de.exceptionflug.imagini.application;

import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.AbstractResponse;
import de.exceptionflug.moon.response.NotFoundResponse;
import de.exceptionflug.moon.response.TextResponse;

import java.io.File;
import java.net.URI;

public class DeleteHandler implements PageHandler<AbstractResponse> {

    private final ImaginiServer imaginiServer;

    public DeleteHandler(ImaginiServer imaginiServer) {
        this.imaginiServer = imaginiServer;
    }

    @Override
    public AbstractResponse handle(AbstractResponse response, Request request) throws Exception {
        String host = request.getHttpExchange().getRequestHeaders().getFirst("Host");
        if(host == null) {
            return new TextResponse("Header fields 'Host' or 'Account' not present", "text/plain", 400);
        }
        Account account = imaginiServer.getAccountByAddress(host);
        if(account == null) {
            return new TextResponse("Account "+host+" not found", "text/plain", 404);
        }
        File file = new File("content/"+account.getName()+request.getHttpExchange().getRequestURI().getPath().substring(7));
        if(!file.exists()) {
            return new TextResponse("No such file: "+request.getHttpExchange().getRequestURI().getPath().substring(7), "text/plain", 404);
        }
        if(file.isDirectory()) {
            return new TextResponse("Target is a directory", "text/plain", 400);
        }
        if(file.delete()) {
            String redirectUrl = request.getQueryParameter("redirectUrl");
            if(redirectUrl != null) {
                request.rewriteLocation(redirectUrl);
            } else {
                return new TextResponse("OK", "text/plain", 200);
            }
        } else {
            return new TextResponse("Could not delete file", "text/plain", 500);
        }
        return response;
    }

}
