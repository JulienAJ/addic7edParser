package julienaj.addictedParser;

import java.util.List;

class Episode
{
    private final String name;
    private final String pageLink;
    private final List<Entry> subs;

    public Episode(String name, String pageLink, List<Entry> subs)
    {
        this.name = name;
        this.pageLink = pageLink;
        this.subs = subs;
    }

    @Override
    public String toString()
    {
        return "Episode{" +
                "name='" + name + '\'' +
                ", pageLink='" + pageLink + '\'' +
                ", subs=" + subs +
                '}';
    }

    public String getName()
    {
        return name;
    }

    public String getPageLink()
    {
        return pageLink;
    }

    public List<Entry> getSubs()
    {
        return subs;
    }
}
