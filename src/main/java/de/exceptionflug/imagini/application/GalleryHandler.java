package de.exceptionflug.imagini.application;

import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.imagini.elements.GalleryDomElement;
import de.exceptionflug.imagini.utils.FileUtils;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.TextResponse;

import java.io.File;

public class GalleryHandler implements PageHandler<TextResponse> {

    private static final String GALLERY_TEMPLATE = FileUtils.getFileContents(
            ImaginiServer.class.getResourceAsStream("/html/gallery_template.html")
    );

    private final ImaginiServer imaginiServer;

    public GalleryHandler(ImaginiServer imaginiServer) {
        this.imaginiServer = imaginiServer;
    }

    @Override
    public TextResponse handle(TextResponse response, Request request) throws Exception {
        String host = request.getHttpExchange().getRequestHeaders().getFirst("Host");
        if(host == null) {
            return new TextResponse("Header fields 'Host' or 'Account' not present", "text/plain", 400);
        }
        Account account = imaginiServer.getAccountByAddress(host);
        if(account == null) {
            return new TextResponse("Account "+host+" not found", "text/plain", 404);
        }
        TextResponse out = new TextResponse(GALLERY_TEMPLATE, "text/html");
        StringBuilder stringBuilder = new StringBuilder();

        File dir = new File("content/"+account.getName());
        for(File child : dir.listFiles()) {
            stringBuilder.append(new GalleryDomElement(account.getRedirectUrl()+child.getName(),
                    account.getRedirectUrl()+child.getName(), "", child.getName()));
        }

        out.replace("%gallery%", stringBuilder.toString());
        return out;
    }

}
