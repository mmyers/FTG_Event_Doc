/*
 * EventParser.java
 *
 * Created on Mar 14, 2008, 12:23:12 AM
 */

package eventdoc.ftg;

import eug.parser.EUGScanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class EventParser {
    
    private EUGScanner scanner;

    public EventParser(String filename) {
        this(new File(filename));
    }
    
    public EventParser(File file) {
        try {
            FileReader reader = new FileReader(file);
            scanner = new EUGScanner(reader);
            scanner.setCommentsIgnored(true);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public List<Event> parse() {
        final List<Event> ret = new ArrayList<Event>();
        
        parseLoop: while (true) {
            switch (scanner.nextToken()) {
                case IDENT:
                    if (scanner.lastStr().equalsIgnoreCase("event")) {
                        ret.add(new Event(scanner));
                    } else {
                        warn("Invalid definition: " + scanner.lastStr());
                    }
                    break;
                case EOF:
                    break parseLoop;
                default:
                    warn("Invalid token: " + scanner.lastStr());
                    break;
            }
        }
        
        return ret;
    }
    
    public void close() {
        scanner.close();
    }
    
    private void warn(String msg) {
        warn(msg, scanner.getLine(), scanner.getColumn());
    }
    
    private static void warn(String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }
    
}
