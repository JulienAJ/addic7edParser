package julienaj.addictedParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

//import javafx.util.Pair;
import org.apache.commons.cli.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main
{
    private static final String DOWNLOAD_BASE_URL = "http://www.addic7ed.com";
    private static final String EPISODE_BASE_URL = "http://www.addic7ed.com/serie/";
    private static final String PROGRAM_NAME = "java -jar AddictedParser.jar";
    private static final String FIREFOX_USER_AGENT =
            "Mozilla/5.0 (X11; U; Linux i686; pl-PL; rv:1.9.0.2) Gecko/20121223 Ubuntu/9.25 (jaunty) Firefox/3.8";

    private static Document getPage(String url) {
        try {
            return Jsoup.connect(url)
                    .referrer(DOWNLOAD_BASE_URL)
                    .userAgent(FIREFOX_USER_AGENT)
                    .followRedirects(true)
                    .get();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static Episode getEpisode(String seriesName, String season, String episode)
    {
        try
        {
            String url = EPISODE_BASE_URL + URLEncoder.encode(seriesName.replaceAll(" ", "_"), "UTF-8") + '/' + season + "/" + episode + "/x";
            System.out.println(url);
            Document page = getPage(url);
            if(page == null)
                return null;

            // TODO manage getPage errors
            // TODO manage redirect to login page

            List<Entry> results= new ArrayList<>();

            Element body = page.body();

            if(body.select("span[class*=titulo]").size() == 0)
            {
                // Episode Page doesn't exist
                System.err.println("No episode page yet");
                return null;
            }
            String title = body.select("span[class*=titulo]").first().ownText();
            Elements releases = body.select("div[id*=container95m").select("table[class*=tabel95]").select("table[border*=0]").select("table[align*=center]");

            for(Element element : releases)
            {
                Element header = element.select("td[class*=NewsTitle]").first();
                String temp[] = header.ownText().split(",");
                String version = temp[0].replace("Version ", "");

                header = element.select("td[colspan*=2]").first();
                String uploader = header.select("a[href*=/user/]").first().ownText();

                String comment = element.select("td[class*=newsDate]").first().ownText();

                Elements content = element.select("td[class*=language");

                for(Element translation : content)
                {
                    Element line = translation.parent();
                    String lang = translation.ownText();

                    String status = line.select("td[width*=19%]").select("b").first().ownText();

                    Elements buttons = line.select("a[class*=buttonDownload]");
                    String link = buttons.first().attr("href");

                    line = line.nextElementSibling();
                    boolean HI = false;
                    if(line.select("img[title*=Hearing Impaired]").size() != 0)
                        HI = true;

                    results.add(new Entry(version, lang, status, uploader, comment, DOWNLOAD_BASE_URL + link, HI));

                    // Checking for most updated
                    if(buttons.size() != 1)
                    {
                        link =  buttons.last().attr("href");
                        results.add(new Entry(version, lang, status, uploader, comment, DOWNLOAD_BASE_URL + link, HI));

                    }
                }

            }
            return new Episode(title, url, results);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static String trimRelease(String original)
    {
        // TODO manage .colored, 720p in versions name
        return original.toLowerCase().replace(".colored", "").replace("(720p)", "")
                .replace("hdtv.x264-", "").replace("1080p.", "");
    }

    private static List<Pair<String>> getEquivalence(String comments)
    {
        List<Pair<String>> equivalence= new ArrayList<>();
        equivalence.add(new Pair<>("lol", "dimension"));
        equivalence.add(new Pair<>("xii", "immerse"));
        equivalence.add(new Pair<>("asap", "immerse"));

        // TODO manage "works with"/"doesn't work with" in comments

        return equivalence;
    }

    private static boolean matchRelease(String episodeVersion, String subtitleVersion, String comment)
    {
        if(episodeVersion == null || subtitleVersion == null || episodeVersion.equals("") || subtitleVersion.equals(""))
            return false;

        episodeVersion = episodeVersion.toLowerCase();
        subtitleVersion = trimRelease(subtitleVersion);

        if(episodeVersion.equals(subtitleVersion))
            return true;

        List<Pair<String>> equivalence= getEquivalence(comment);

        for(Pair<String> pair : equivalence)
        {
            if((episodeVersion.equals(pair.getKey()) && subtitleVersion.equals(pair.getValue()))
                || (episodeVersion.equals(pair.getValue()) && subtitleVersion.equals(pair.getKey())))
                return true;
        }

        return false;
    }

    private static boolean matchCompletion(int minimum, String status)
    {
        status = status.toLowerCase();

        if(status.equals("completed"))
            return true;

        String[] temp = status.split("%");
        double percentage = Double.parseDouble(temp[0]);

        return (percentage >= (double) minimum);

    }

    private static CommandLine getCommandLine(String args[])
    {
        Options options = new Options();

        options.addOption("h", "help", false, "display help");
        options.addOption("l", "language", true, "language of the subtitle");
        options.addOption("r", "release", true, "release name for the episode");
        options.addOption("hi", "hearing-impaired", false, "hearing impaired subtitles");
        options.addOption("S", "series", true, "name of the series");
        options.addOption("s", "season", true, "season of the episode");
        options.addOption("e", "episode", true, "episode of the season");
        // TODO manage incomplete translations
        //options.addOption("c", "completion", true, "minimum percentage of completion");
        options.addOption("fd", "force-download", true, "force download in <arg> repertory");
        options.addOption("list", false, "list all subtitles available");

        CommandLineParser parser = new BasicParser();
        try
        {
            CommandLine commandLine = parser.parse(options, args);
            if(commandLine.hasOption("h"))
            {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(PROGRAM_NAME, options);
                return null;
            }
            return commandLine;
        }
        catch(ParseException e)
        {
            System.err.println("Error while parsing command line arguments");
            System.err.println(e.getMessage());
            return null;
        }
    }

    private static String download(String link, String referer, String epName, String repertory)
    {
        // TODO manage incomplete translations
        try
        {
            String filename = repertory + File.separator + epName + ".srt";
            byte[] bytes = Jsoup.connect(link).referrer(referer).ignoreContentType(true).execute().bodyAsBytes();
            if(bytes != null && bytes.length != 0)
            {
                FileOutputStream stream = new FileOutputStream(filename);
                stream.write(bytes);
                stream.close();
                return filename;
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args)
    {
        CommandLine cmd = getCommandLine(args);

        if(cmd == null)
            return;

        if(!cmd.hasOption("S") || !cmd.hasOption("s") || !cmd.hasOption("e") || !cmd.hasOption("r")
                || cmd.getOptionValue("S") == null || cmd.getOptionValue("s") == null
                || cmd.getOptionValue("e") == null || cmd.getOptionValue("r") == null)
        {
            // TODO remove release from mandatory options when there is a list
            System.err.println("Series name (S), season (s), episode (e) and release (r) are mandatory");
            System.err.println("See " + PROGRAM_NAME + " -h " + "for help");
            return;
        }

        String seriesName = cmd.getOptionValue("S");
        String season = cmd.getOptionValue("s");
        String episode = cmd.getOptionValue("e");

        String language = cmd.getOptionValue("l", "english");
        boolean list = cmd.hasOption("list");
        boolean HI = cmd.hasOption("hi");
        String release = cmd.getOptionValue("r");
        int completion = Integer.parseInt(cmd.getOptionValue("c", "100"));
        boolean forceDownload = cmd.hasOption("fd");
        String downloadDirectory = cmd.getOptionValue("fd");

        Episode result = Main.getEpisode(seriesName, season, episode);

        if(result == null)
        {
            System.out.println("No Results");
            return;
        }

        if(list)
        {
            System.out.println(result.getName() + " (" + result.getPageLink() + ")");
            List<Entry> results = result.getSubs();
            results.forEach(System.out::println);
            return;
        }

        List<Entry> results = result.getSubs();
        List<Entry> selected = new ArrayList<>();
        //noinspection Convert2streamapi
        for(Entry e : results)
        {
            if(language.toLowerCase().equals(e.getLanguage().toLowerCase())
                    && matchRelease(release, e.getVersion(), e.getComment()))
            {
                if((HI == e.isHI() || e.isHI()) && matchCompletion(completion, e.getStatus()))
                    selected.add(e);
            }
        }

        if(!forceDownload)
            System.out.println(result.getName() + " (" + result.getPageLink() + ")");

        if(selected.size() == 0)
        {
            System.out.println("No matching results");
            return;
        }

        if(forceDownload)
        {
            Entry bestEntry = selected.get(0);
            if(selected.size() != 1)
            {
                if(bestEntry.getLink().contains("original") && selected.get(1).getLink().contains("updated"))
                    bestEntry = selected.get(1);

                // TODO manage "colored"
            }

            String output = download(bestEntry.getLink(), result.getPageLink(), result.getName(), downloadDirectory);
            System.out.println(output);
            return;
        }

        for(Entry e : selected)
        {
            System.out.println(e);
        }
    }
}
