package agregator;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import one.util.streamex.StreamEx;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    /**
     * Получение списка url rss лент
     * @return возвращает список List<String> состоящий из url по которым расположены rss ленты
     */
    public static List<String> getListURL(){

        FileInputStream fis;
        Properties property = new Properties();
        List<String> listOfUrl = new ArrayList<>();
        FileReader fr = null;
        try {
            fis = new FileInputStream("application.properties");
            property.load(fis);
            File fileWithURL = new File(property.getProperty("LIST_URL_PATH"));
            fr = new FileReader(fileWithURL);
            BufferedReader reader = new BufferedReader(fr);
            String line = null;
            while ((line = reader.readLine()) != null) {
                listOfUrl.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return listOfUrl;
    }


    /**
     * Формирование html контента и записаь
     */
    public static void createHtmlFile(){


        FileInputStream fis;
        Properties property = new Properties();
        try {
            fis = new FileInputStream("application.properties");
            property.load(fis);
            List<Item> items = getContent(Integer.valueOf(property.getProperty("COUNT_ITEMS")));
            StringBuilder htmlStringBuilder=new StringBuilder();

            htmlStringBuilder.append("<html>\n" +
                    "  <head>\n" +
                    "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n" +
                    "    <title>Ссылки на 50 последних тем</title>\n" +
                    "  </head>\n");
            htmlStringBuilder.append("  <body>\n");
            for(Item item : items){
                htmlStringBuilder.append(String.format("    <p><a href=\"%s\">%s</a></p>\n",item.getLink().get(),item.getTitle().get()));
            }
            htmlStringBuilder.append("  </body>\n" +
                    "</html>");

            WriteToFile(htmlStringBuilder.toString(),property.getProperty("RESULT_HTML_PATH"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод получения данных из rss ленты
     * @param count количество записей, тип int
     * @return возвращает список первых 50 объектов rss ленты, тип: List<Item>
     */
    public static List<Item> getContent(int count){
        List<Item> result = new ArrayList<>();
        RssReader reader = new RssReader();
        Stream<Item> rssFeed = null;
        try {

            List<String> listOfUrl = getListURL();

            if(listOfUrl.size()!=0){

                if(listOfUrl.size()>1){
                    rssFeed = StreamEx.of(reader.read(listOfUrl.get(0)));
                    for(int i=1;i<listOfUrl.size();i++){
                        rssFeed = ((StreamEx<Item>) rssFeed).append(reader.read(listOfUrl.get(i)));
                    }

                    result = rssFeed
                            .sorted(Comparator
                                    .comparing((Item o) ->
                                            o.getPubDate().isPresent() ? new Date(o.getPubDate().get()) : new Date(0)).reversed())
                            .limit(count).collect(Collectors.toList());
                }else{

                    rssFeed = reader.read(listOfUrl.get(0));

                    result = rssFeed
                            .sorted(Comparator
                                    .comparing((Item o) ->
                                            o.getPubDate().isPresent() ? new Date(o.getPubDate().get()) : new Date(0)).reversed())
                            .limit(count).collect(Collectors.toList());
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
    }

    /**
     * Метод создания или перезаписи файла
     * @param fileContent String контент файла
     * @param fileName String путь до файла(если указано просто имя файла, файл будет создан в директории проекта)
     * @throws IOException
     */
    public static void WriteToFile(String fileContent, String fileName) throws IOException {

        File file = new File(fileName);
        OutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
        Writer writer=new OutputStreamWriter(outputStream);
        writer.write(fileContent);
        writer.close();

    }

}
