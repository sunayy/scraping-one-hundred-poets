import net.arnx.jsonic.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scraping {

    private final static String URL = "https://ja.wikipedia.org/wiki/百人一首";
    private final static String WAKA_TABLE_SELECT = ".wikitable";

    private final static int SKIP_HEADER_NUM = 1;

    public static void main(String[] args) {
        final Stream<Element> elementStream;
        try {
            elementStream = Jsoup.connect(URL).get()
                        .select(WAKA_TABLE_SELECT)
                        .select("tr")
                        .stream();
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
            return;
        }
        final List<Waka> wakaList = elementStream
                     .skip(SKIP_HEADER_NUM)
                     .map(e -> mapToWakaObject(selectBytd(e)))
                     .collect(Collectors.toList());

        System.out.println(JSON.encode(wakaList));
    }

    private static Elements selectBytd(Element element) {
        return element.select("td");
    }

    private static Waka mapToWakaObject(Elements elements) {
        final Element numberElement = elements.get(WAKA_PROPERTY.NUMBER.number);
        final int number = Integer.parseInt(WAKA_PROPERTY.NUMBER.mapper.apply(numberElement));

        final Element wakaElement = elements.get(WAKA_PROPERTY.AUTHOR.number);
        final String waka = WAKA_PROPERTY.WAKA.mapper.apply(wakaElement);

        final Element authorElement = elements.get(WAKA_PROPERTY.WAKA.number);
        final String author = WAKA_PROPERTY.AUTHOR.mapper.apply(authorElement);

        return new Waka(number, author, waka);
    }

    private enum WAKA_PROPERTY {
        NUMBER  (0, (e) -> e.text().replaceAll("\\..*", "")),
        AUTHOR  (2, (e) -> e.text()),
        WAKA    (1, (e) -> e.text());

        final int number;
        final Function<Element, String> mapper;

        WAKA_PROPERTY(int number, Function<Element, String> mapper) {
            this.number = number;
            this.mapper = mapper;
        }
    }
}
