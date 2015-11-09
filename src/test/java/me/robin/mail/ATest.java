package me.robin.mail;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;

/**
 * Created by Administrator on 2015/10/27.
 */
public class ATest {
    public static void main(String[] args) {

        String json = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Pensons>\n" +
                "\t<Penson id=\"1\" city=\"zj\">\n" +
                "\t\t<name>name</name>\n" +
                "\t\t<sex>男</sex>\n" +
                "\t\t<age>30</age>\n" +
                "\t</Penson>\n" +
                "\t<Penson id=\"2\" city=\"zj\">\n" +
                "\t\t<name>name</name>\n" +
                "\t\t<sex>男</sex>\n" +
                "\t\t<age>30</age>\n" +
                "\t</Penson>\n" +
                "\t<Penson id=\"4\" city=\"zj\">\n" +
                "\t\t<name>name</name>\n" +
                "\t\t<sex>男</sex>\n" +
                "\t\t<age>30</age>\n" +
                "\t</Penson>\n" +
                "</Pensons>";

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>\n" +
                " <head id=\"common\">\n" +
                " <item key=\"Host\">www.nbcredit.net</item>\n" +
                " <item key=\"Connection\">Keep-Alive</item>\n" +
                " <item key=\"Accept\">text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8</item>\n" +
                " <item key=\"Proxy-Connection\">Keep-Alive</item>\n" +
                " <item key=\"Accept-Language\">zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3</item>\n" +
                " <item key=\"Accept-Encoding\">gzip,deflate</item>\n" +
                " </head>\n" +
                " <head id=\"json\">\n" +
                " <item key=\"Accept\">application/json, text/javascript, */*</item>\n" +
                " </head>\n" +
                "</root>";

        Document document = Jsoup.parse(json);


        Document xmlDoc =  Jsoup.parse(xml, "", new Parser(new XmlTreeBuilder()));

        System.out.println();
    }
}
