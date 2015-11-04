package julienaj.addictedParser;

class Entry
{
    private final String version;
    private final String uploader;
    private final String comment;

    private final String language;
    private final String status;
    private final String link;
    private final boolean HI;

    public Entry(String version, String language, String status, String uploader, String comment, String link, boolean HI)
    {
        this.version = version;
        this.language = language;
        this.status = status;
        this.uploader = uploader;
        this.comment = comment;
        this.link = link;
        this.HI = HI;
    }
    // TODO write better toString
    @Override
    public String toString() {
        return "Entry{" +
                "version='" + version + '\'' +
                ", language='" + language + '\'' +
                ", status='" + status + '\'' +
                ", uploader='" + uploader + '\'' +
                ", comment='" + comment + '\'' +
                ", link='" + link + '\'' +
                ", HI=" + HI +
                '}';
    }

    public String getVersion()
    {
        return version;
    }

// --Commented out by Inspection START (21/08/2015 13:42):
//    public String getUploader()
//    {
//        return uploader;
//    }
// --Commented out by Inspection STOP (21/08/2015 13:42)

    public String getComment()
    {
        return comment;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getStatus()
    {
        return status;
    }

    public String getLink()
    {
        return link;
    }

    public boolean isHI()
    {
        return HI;
    }
}
