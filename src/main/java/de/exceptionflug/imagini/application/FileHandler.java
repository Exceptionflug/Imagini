package de.exceptionflug.imagini.application;

import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.elements.simple.MoonFooterElement;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.*;
import lombok.extern.log4j.Log4j2;
import org.apache.http.entity.ContentType;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

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
        File file = new File("content/"+account.getName()+request.getHttpExchange().getRequestURI());
        if(!file.exists()) {
            return new NotFoundResponse(request.getHttpExchange().getRequestURI());
        }
        if(file.isDirectory()) {
            return new TextResponse("<h2>Imagini File Server</h2><br>Current account: "+account.getName()+"<hr><i>&copy; Nico Britze 2020</i>", "text/html", 200);
        }
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            Account accessor = imaginiServer.getAccountByLastAccessAddress(request.getHttpExchange().getRemoteAddress().getHostString());
            if(accessor != null) {
                log.info(accessor.getName()+" ["+request.getHttpExchange().getRemoteAddress().getHostString()+"] accessed file "+file.getName()+" of account "+account.getName());
            } else {
                log.info("["+request.getHttpExchange().getRemoteAddress().getHostString()+"] accessed file "+file.getName()+" of account "+account.getName());
            }
            return new BinaryResponse(IOUtils.readFully(fileInputStream, -1, true), Files.probeContentType(file.toPath()));
        }
    }

}
