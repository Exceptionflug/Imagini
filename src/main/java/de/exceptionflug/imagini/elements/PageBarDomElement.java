package de.exceptionflug.imagini.elements;

import de.exceptionflug.moon.DomElement;
import de.exceptionflug.moon.elements.simple.BaseElement;
import de.exceptionflug.moon.elements.simple.LinkElement;

import java.util.Set;
import java.util.TreeSet;

public class PageBarDomElement extends DomElement {

    private static final int MAX_PAGE_DISPLAY_AMOUNT = 10;

    private final String pageUrl;
    private final int firstPage, lastPage, currentPage;

    public PageBarDomElement(String pageUrl, int firstPage, int lastPage, int currentPage) {
        this.pageUrl = pageUrl;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
        this.currentPage = currentPage;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(new LinkElement(new BaseElement("&laquo;"), pageUrl.replace("%page%", Integer.toString(currentPage-1))));
        Set<Integer> pagesBetween = calculateIntegersBetween(firstPage, lastPage, MAX_PAGE_DISPLAY_AMOUNT);
        pagesBetween.add(firstPage);
        pagesBetween.add(lastPage);
        if(currentPage-1 > 0) {
            pagesBetween.add(currentPage-1);
        }
        if(currentPage+1 < lastPage) {
            pagesBetween.add(currentPage+1);
        }
        pagesBetween.add(currentPage);
        for(int i : pagesBetween) {
            if(i == currentPage) {
                builder.append(new BaseElement("<a href=\"#\" class=\"active\">"+currentPage+"</a>"));
            } else {
                builder.append(new LinkElement(new BaseElement(Integer.toString(i)),
                        pageUrl.replace("%page%", Integer.toString(i))));
            }
        }
        builder.append(new LinkElement(new BaseElement("&raquo;"), pageUrl.replace("%page%", Integer.toString(currentPage+1))));
        return builder.toString();
    }

    private Set<Integer> calculateIntegersBetween(int firstPage, int lastPage, int maxPageDisplayAmount) {
        int interval = (int) Math.ceil((lastPage - firstPage) / (double)maxPageDisplayAmount);
        Set<Integer> out = new TreeSet<>();
        for (int i = 0; i < maxPageDisplayAmount; i++) {
            firstPage += interval;
            if(firstPage >= lastPage)
                break;
            out.add(firstPage);
        }
        return out;
    }

}
