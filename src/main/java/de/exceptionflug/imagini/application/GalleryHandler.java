package de.exceptionflug.imagini.application;

import com.google.common.collect.Ordering;
import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.imagini.elements.GalleryDomElement;
import de.exceptionflug.imagini.elements.PageBarDomElement;
import de.exceptionflug.imagini.utils.FileUtils;
import de.exceptionflug.moon.Request;
import de.exceptionflug.moon.handler.PageHandler;
import de.exceptionflug.moon.response.TextResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GalleryHandler implements PageHandler<TextResponse> {

    private static final int ITEMS_PER_PAGE = 20;

    private static final String GALLERY_TEMPLATE = FileUtils.getFileContents(
            ImaginiServer.class.getResourceAsStream("/html/gallery_template.html")
    );

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

        int page = getPageNumber(request);
        if(page < 1) {
            page = 1;
        }

        File dir = new File("content/"+account.getName());
        File[] filesInDir = dir.listFiles();
        filesInDir = filesInDir == null ? new File[0] : filesInDir;
        List<File> files = FILE_ORDERING.sortedCopy(Arrays.asList(filesInDir));

        int pageCount = (int) Math.ceil(files.size() / (double) ITEMS_PER_PAGE);
        pageCount = pageCount == 0 ? 1 : pageCount;
        if(page > pageCount) {
            page = pageCount;
        }
        int startIndex = (page-1)*ITEMS_PER_PAGE;
        int endIndex = Math.min(files.size(), startIndex + ITEMS_PER_PAGE);
        for (int i = startIndex; i < endIndex; i++) {
            File child = files.get(i);

            BasicFileAttributes basicFileAttributes = Files.readAttributes(child.toPath(), BasicFileAttributes.class);
            stringBuilder.append(new GalleryDomElement(account.getRedirectUrl()+child.getName(),
                    account.getRedirectUrl()+child.getName()+"?logging=false",
                    DateFormat.getDateTimeInstance().format(new Date(basicFileAttributes.creationTime().toMillis())),
                    child.getName()));
        }

        out.replace("%gallery%", stringBuilder.toString());
        out.replace("%pageBar%", new PageBarDomElement(account.getRedirectUrl()+"gallery?page=%page%",
                1, pageCount, page));
        return out;
    }

    private int getPageNumber(Request request) {
        String pageStr = request.getQueryParameter("page");
        if(pageStr == null) {
            return 1;
        }
        try {
            return Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

}
