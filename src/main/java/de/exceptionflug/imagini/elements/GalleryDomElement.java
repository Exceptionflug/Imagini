package de.exceptionflug.imagini.elements;

import de.exceptionflug.moon.DomElement;

public final class GalleryDomElement extends DomElement {

    private static final String TEMPLATE = " <div class=\"img-box\">\n" +
            "        <a href='%href%'><img src=\"%src%\" alt=\"\" /></a>\n" +
            "        <div class=\"caption\">\n" +
            "             <p>%title%</p>\n" +
            "             <p class=\"opacity-low\">%description%</p>\n" +
            "        </div>\n" +
            "    </div>";

    private final String href;
    private final String src;
    private final String description;
    private final String title;

    public GalleryDomElement(String href, String src, String description, String title) {
        this.href = href;
        this.src = src;
        this.description = description;
        this.title = title;
    }

    @Override
    public String toString() {
        return TEMPLATE.replace("%href%", href)
                .replace("%src%", src)
                .replace("%title%", title)
                .replace("%description%", description);
    }

}
