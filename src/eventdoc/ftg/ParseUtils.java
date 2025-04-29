/*
 * ParseUtils.java
 *
 * Created on Mar 17, 2008, 7:56:10 PM
 */

package eventdoc.ftg;

import eug.parser.EUGScanner;
import eug.parser.TokenType;
import java.util.GregorianCalendar;

/**
 *
 * @author Michael
 */
final class ParseUtils {

    private enum Months {
        january, february, march, april, may, june,
        july, august, september, october, november, december
    }

    static GregorianCalendar parseDate(final EUGScanner scanner) {
        int year = 0;
        int month = 0;
        int day = 1;

        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Missing '{' in date", scanner.getLine(), scanner.getColumn());
        }
        parseLoop:
        while (true) {
            switch (scanner.nextToken()) {
                case IDENT:
                    String ident = scanner.lastStr().toLowerCase();
                    if (ident.equals("year")) {
                        scanner.nextToken();
                        year = tryParseInt(scanner);
                    } else if (ident.equals("month")) {
                        scanner.nextToken();
                        month = tryParseMonth(scanner);
                    } else if (ident.equals("day")) {
                        scanner.nextToken();
                        day = tryParseInt(scanner) + 1;  // correct range (0-29) to (1-30)
                    } else {
                        warn("Unexpected variable in date: " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                    }
                    break;
                case RBRACE:
                    break parseLoop;
                default:
                    warn("Unexpected token in date: " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                    break;
            }
        }
        return new GregorianCalendar(year, month, day);
    }

    static int tryParseInt(EUGScanner scanner) {
        try {
            return Integer.parseInt(scanner.lastStr());
        } catch (NumberFormatException ex) {
            warn("Expected integer but got '" + scanner.lastStr() + "'", scanner.getLine(), scanner.getColumn());
            return 0;
        }
    }

    static int tryParseMonth(EUGScanner scanner) {
        try {
            String monthStr = scanner.lastStr().toLowerCase();
            return Months.valueOf(monthStr).ordinal();
        } catch (IllegalArgumentException ex) {
            warn("Expected month but got '" + scanner.lastStr() + "'", scanner.getLine(), scanner.getColumn());
            return 0;
        }
    }
    
    static int readId(final EUGScanner scanner) {
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Missing '{' in id", scanner.getLine(), scanner.getColumn());
        }
        
        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"type =\"", scanner.getLine(), scanner.getColumn());
        }
        
        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected \"type = <value>\"", scanner.getLine(), scanner.getColumn());
        }
        
        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"id =\"", scanner.getLine(), scanner.getColumn());
        }
        
        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected \"id = <value>\"", scanner.getLine(), scanner.getColumn());
        }
        
        int ret = Integer.parseInt(scanner.lastStr());
        
        while (scanner.nextToken() != TokenType.RBRACE) {
            warn("Expected '}", scanner.getLine(), scanner.getColumn());
        }
        
        return ret;
    }
    
    static void warn(final String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }
}
