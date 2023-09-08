package eventdoc.ftg;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Michael
 */
public interface HtmlObject {
    void generateHTML(BufferedWriter out) throws IOException;
}
