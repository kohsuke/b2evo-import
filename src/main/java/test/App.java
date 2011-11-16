package test;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    // tweaked atom feed and have it return all comments

    public static void main( String[] args ) throws Exception {
        Map<String,Integer> ids = buildIdMap();

        SAXReader r = new SAXReader();
        Document dom = r.read(new File("shiori.comment.rss"));
        List<Element> list = dom.getRootElement().elements("entry");
        for (Element e : list) {
            String title = e.elementText("title");
            title = title.substring(title.indexOf(':')+2);

            String name = e.element("author").elementText("name");
            name = name.substring(0,name.lastIndexOf('[')-1);
            String date = e.elementText("issued");
            date = date.replace('T',' ').replaceAll("Z","");
            String content = e.elementText("content");

            int id = ids.get(title);

            System.out.printf("INSERT INTO wp_comments (comment_post_ID,comment_author,comment_date,comment_date_gmt,comment_content) "
                    +"VALUES(%d,\"%s\",\"%s\",\"%s\",\"%s\");\n", id,
                    name, date, date, escape(content));
        }
    }

    private static String escape(String content) {
        StringBuilder b = new StringBuilder(content.length());
        for (int i=0; i<content.length(); i++) {
            char ch = content.charAt(i);
            int idx = "'\"\n\r\t\\".indexOf(ch);
            if (idx <0)
                b.append(ch);
            else
                b.append('\\').append("'\"nrt\\".charAt(idx));
        }
        return b.toString();
    }

    private static Map<String,Integer> buildIdMap() throws IOException {
        Map<String,Integer> postIds = new HashMap<String, Integer>();
        BufferedReader r = new BufferedReader(new FileReader("wp-posts.csv"));
        r.readLine();   // caption
        String s;
        while ((s=r.readLine())!=null) {
            int idx = s.indexOf(',');
            int id = Integer.parseInt(s.substring(0,idx));
            String post = s.substring(idx+1);
            post = post.substring(1,post.length()-1);
            postIds.put(post,id);
        }
        return postIds;
    }
}
