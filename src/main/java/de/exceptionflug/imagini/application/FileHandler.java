package de.exceptionflug.imagini.application;

import com.google.common.io.ByteStreams;
import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.AbstractResponse;
import de.exceptionflug.moon.response.BinaryResponse;
import de.exceptionflug.moon.response.NotFoundResponse;
import de.exceptionflug.moon.response.TextResponse;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileHandler implements PageHandler<AbstractResponse> {

    private final ImaginiServer imaginiServer;

    public FileHandler(ImaginiServer imaginiServer) {
        this.imaginiServer = imaginiServer;
    }

    @Override
    public AbstractResponse handle(AbstractResponse abstractResponse, Request request) throws Exception {
        String host = request.getHttpExchange().getRequestHeaders().getFirst("Host");
        if(host == null) {
            return new TextResponse("Header fields 'Host' or 'Account' not present", "text/plain", 400);
        }
        Account account = imaginiServer.getAccountByAddress(host);
        if(account == null) {
            return new TextResponse("Account "+host+" not found", "text/plain", 404);
        }
        File file = new File("content/"+account.getName()+request.getHttpExchange().getRequestURI().getPath());
        if(!file.exists()) {
            return new NotFoundResponse(request.getHttpExchange().getRequestURI());
        }
        if(file.isDirectory()) {
            return new TextResponse("<h2>Imagini File Server</h2><br>Current account: "+account.getName()+"<hr><i>&copy; Nico Britze 2020</i>", "text/html", 200);
        }
        boolean logging = true;
        if(request.getQueryParameter("logging") != null && request.getQueryParameter("logging").equalsIgnoreCase("false")) {
            logging = false;
        }
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            if(logging) {
                Account accessor = imaginiServer.getAccountByLastAccessAddress(imaginiServer.getRemoteAddress(request.getHttpExchange()));
                if(accessor != null) {
                    log.info(accessor.getName()+" ["+request.getHttpExchange().getRemoteAddress().getHostString()+"] accessed file "+file.getName()+" of account "+account.getName());
                } else {
                    log.info("["+imaginiServer.getRemoteAddress(request.getHttpExchange())+"] accessed file "+file.getName()+" of account "+account.getName());
                }
            }
            return new BinaryResponse(ByteStreams.toByteArray(fileInputStream), Files.probeContentType(file.toPath()));
        }
    }

}
