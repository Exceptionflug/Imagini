package de.exceptionflug.imagini.application;

import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.imagini.utils.FileUtils;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.AbstractResponse;
import de.exceptionflug.moon.response.TextResponse;
import de.exceptionflug.moon.rest.response.JsonResponse;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GalleryApiHandler implements PageHandler<JsonResponse<List<String>>> {

    private final ImaginiServer imaginiServer;

    public GalleryApiHandler(ImaginiServer imaginiServer) {
        this.imaginiServer = imaginiServer;
    }

    @Override
    public AbstractResponse handle(JsonResponse<List<String>> listJsonResponse, Request request) throws Exception {
        String host = request.getHttpExchange().getRequestHeaders().getFirst("Host");
        if(host == null) {
            return new TextResponse("Header fields 'Host' or 'Account' not present", "text/plain", 400);
        }
        Account account = imaginiServer.getAccountByAddress(host);
        if(account == null) {
            return new TextResponse("Account "+host+" not found", "text/plain", 404);
        }
        File dir = new File("content/"+account.getName());
        File[] filesInDir = dir.listFiles();
        filesInDir = filesInDir == null ? new File[0] : filesInDir;
        List<File> files = FileUtils.getFileOrdering().sortedCopy(Arrays.asList(filesInDir));

        return new JsonResponse<>(files.stream()
                .map(File::getName)
                .collect(Collectors.toList()));
    }

}
