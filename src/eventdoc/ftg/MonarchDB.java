/*
 * MonarchDB.java
 *
 * Created on Mar 17, 2008, 7:34:15 PM
 */

package eventdoc.ftg;

import eug.parser.EUGScanner;
import eug.parser.TokenType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Michael
 */
public class MonarchDB {
    
    private static final Map<Integer, Monarch> allMonarchs =
            new HashMap<Integer, Monarch>(5000);
    
//    private static final Map<String, Map<Integer, Monarch>> byCountry =
//            new HashMap<String, Map<Integer, Monarch>>(200);
    
    public static void init(String directory) {
        System.out.println("Parsing monarchs from " + directory);
        for (File f : Main.resolver.listFiles(directory)) {
            //String tag = f.getPath().substring(f.getPath().lastIndexOf('.') + 1);
            String tag = f.getPath().substring(f.getPath().lastIndexOf('_') + 1, f.getPath().lastIndexOf('.'));
            if (Text.getText(tag).equalsIgnoreCase(tag)) {
                continue;   // not a country tag
            }
            
            parse(f, tag);
        }
        System.out.println("Finished monarchs");
    }

    private static void parse(File f, String tag) {
//        System.out.println(f);
        try {
            EUGScanner scanner = new EUGScanner(new FileReader(f));
            scanner.setCommentsIgnored(true);
            
            parseLoop:
            while (true) {
                switch (scanner.nextToken()) {
                    case IDENT:
                        if (!scanner.lastStr().equalsIgnoreCase("historicalmonarch")) {
                            warn("Illegal object in " + tag + ": " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                            break;
                        }
                        Monarch m = new Monarch(scanner);
                        if (allMonarchs.get(m.id) != null) {
                            System.out.println("Monarch ID conflict: " + m.name + " and " + allMonarchs.get(m.id) + " both have ID " + m.id);
                        }
                        allMonarchs.put(m.id, m);
                        break;
                    case EOF:
                        break parseLoop;
                    default:
                        warn("Illegal token in " + tag + ": " + scanner.lastToken(), scanner.getLine(), scanner.getColumn());
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error reading from " + f);
        }
    }
    
    private static void warn(String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }
    
    /**
     * Returns an HTML string representing the given monarch.
     * <p>
     * The visible part of the returned string is just the monarch's name, but
     * it also has a tooltip with the monarch's ID and skills.
     * @param monarchID the ID of the monarch to create an HTML string for.
     * @return an HTML string representing the monarch with the given ID.
     */
    static String format(int monarchID) {
        final Monarch m = allMonarchs.get(monarchID);
        if (m == null) {
            System.out.println("Cannot find monarch " + monarchID);
            return "<font color=\"red\">"+monarchID+"</font>";
        }
        StringBuilder ret = new StringBuilder();
        
        ret.append("<span class=\"monarch\" title=\"");
//        if (m.remark != null)
//            ret.append(m.remark);
        ret.append("Id:&nbsp;").append(monarchID);
        ret.append(",&nbsp;DIP:&nbsp;").append(m.DIP).append(",&nbsp;ADM:&nbsp;");
        ret.append(m.ADM).append(",&nbsp;MIL:&nbsp;").append(m.MIL);
        if (m.dormant)
            ret.append("&nbsp;(dormant)");
        if (!m.emperor)
            ret.append("&nbsp;(cannot be emperor)");
        ret.append("\">");
        ret.append(m.name);
        ret.append("</span>");
        
        return ret.toString();
    }
    
    static boolean isDormant(int monarchId) {
        return allMonarchs.get(monarchId) != null && allMonarchs.get(monarchId).dormant;
    }
    
    private static class Monarch {
        private int id;
        private String name;
        private GregorianCalendar startdate;
        private GregorianCalendar deathdate;
        private int DIP;
        private int ADM;
        private int MIL;
        private boolean dormant;
        private String remark;  // not used
        private boolean emperor; // FTG
        private boolean dynastic; // FTG
        private List<Union> unions = null;
        
        private static final Pattern SPACE = Pattern.compile(" ");
        
        private Monarch(EUGScanner scanner) {
            if (scanner.nextToken() != TokenType.LBRACE) {
                warn("No '{' after \"monarch =\"", scanner.getLine(), scanner.getColumn());
            }

            emperor = true;
            dynastic = true;

            parseLoop:
            while (true) {
                switch (scanner.nextToken()) {
                    case IDENT:
                        String ident = scanner.lastStr().toLowerCase();
                        if (ident.equals("id")) {
                            id = ParseUtils.readId(scanner);
                        } else if (ident.equals("name")) {
                            scanner.nextToken();
                            name = SPACE.matcher(scanner.lastStr()).replaceAll("&nbsp;");
                        } else if (ident.equals("startdate")) {
                            startdate = ParseUtils.parseDate(scanner);
                        } else if (ident.equals("deathdate") || ident.equals("enddate")) {
                            deathdate = ParseUtils.parseDate(scanner);
                        } else if (ident.equals("dip")) {
                            scanner.nextToken();
                            DIP = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("adm")) {
                            scanner.nextToken();
                            ADM = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("mil")) {
                            scanner.nextToken();
                            MIL = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("dormant")) {
                            scanner.nextToken();
                            dormant = (scanner.lastStr().equalsIgnoreCase("yes"));
                        } else if (ident.equals("remark")) {
                            scanner.nextToken();
                            remark = scanner.lastStr();
                        } else if (ident.equals("emperor")) {
                            scanner.nextToken();
                            emperor = (scanner.lastStr().equalsIgnoreCase("yes"));
                        } else if (ident.equals("dynastic")) {
                            scanner.nextToken();
                            dynastic = (scanner.lastStr().equalsIgnoreCase("yes"));
                        } else if (ident.equals("union")) {
                            if (unions == null)
                                unions = new ArrayList<Union>();
                            Union u = new Union(scanner, id);
                            unions.add(u);
                        } else {
                            warn("Unknown variable in monarch: " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                            scanner.skipNext();
                        }
                        break;
                    case RBRACE:
                        break parseLoop;
                    default:
                        warn("Invalid monarch part: " + scanner.lastToken(), scanner.getLine(), scanner.getColumn());
                        break;
                }
            }
        }
    }

    private static class Union {
        private int primary;
        private int secondary;

        private Union(EUGScanner scanner, int id) {
            if (scanner.nextToken() != TokenType.LBRACE) {
                warn("No '{' after \"union =\"", scanner.getLine(), scanner.getColumn());
            }

            primary = id;

            parseLoop:
            while (true) {
                switch (scanner.nextToken()) {
                    case IDENT:
                        String ident = scanner.lastStr().toLowerCase();
                        if (ident.equals("id")) {
                            scanner.nextToken();
                            secondary = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("leader")) {
                            scanner.nextToken();
                            if (scanner.lastStr().equalsIgnoreCase("no")) {
                                primary = secondary;
                                secondary = id;
                            }
                        } else {
                            warn("Unknown variable in union: " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                            scanner.skipNext();
                        }
                        break;
                    case RBRACE:
                        break parseLoop;
                    default:
                        warn("Invalid union part: " + scanner.lastToken(), scanner.getLine(), scanner.getColumn());
                        break;
                }
            }
        }
    }

    private MonarchDB() {
    }
}
