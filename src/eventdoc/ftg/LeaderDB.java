/*
 * LeaderDB.java
 *
 * Created on Mar 17, 2008, 10:29:37 PM
 */

package eventdoc.ftg;

import eug.parser.EUGScanner;
import eug.parser.TokenType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Michael
 */
public class LeaderDB {

    private static final Map<Integer, Leader> allLeaders =
            new HashMap<Integer, Leader>(5000);
    
//    private static final Map<String, Map<Integer, Monarch>> byCountry =
//            new HashMap<String, Map<Integer, Monarch>>(200);
    
    public static void init(String directory) {
        System.out.println("Parsing leaders from " + directory);
        for (File f : Main.resolver.listFiles(directory)) {
            //String tag = f.getPath().substring(f.getPath().lastIndexOf('.') + 1);
            String tag = f.getPath().substring(f.getPath().lastIndexOf('_') + 1, f.getPath().lastIndexOf('.'));
            if (Text.getText(tag).equalsIgnoreCase(tag)) {
                continue;   // not a country tag
            }
            
            parse(f, tag);
        }
        System.out.println("Finished leaders");
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
                        if (!scanner.lastStr().equalsIgnoreCase("historicalleader")) {
                            warn("Illegal object in " + tag + ": " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                            break;
                        }
                        Leader l = new Leader(scanner);
                        allLeaders.put(l.id, l);
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
     * Returns an HTML string representing the given leader.
     * <p>
     * The visible part of the returned string is the leader's type and name,
     * but it also has a tooltip with the leader's ID and skills.
     * @param leaderID the ID of the leader to create an HTML string for.
     * @return an HTML string representing the leader with the given ID.
     */
    static String format(int leaderID) {
        final Leader l = allLeaders.get(leaderID);
        if (l == null) {
            System.out.println("Cannot find leader " + leaderID);
            return "<font color=\"red\">"+leaderID+"</font>";
        }
        StringBuilder ret = new StringBuilder();
        
        ret.append("Leader").append(' ');
        
        ret.append("<span class=\"leader\" title=\"");
//        if (m.remark != null)
//            ret.append(m.remark);
        ret.append("Id:&nbsp;").append(leaderID);
        ret.append(",&nbsp;rank:&nbsp;").append(l.rank);
        ret.append("&nbsp;(").append(l.category).append(")");
        ret.append(",&nbsp;movement:&nbsp;").append(l.movement);
        ret.append(",&nbsp;fire:&nbsp;").append(l.fire);
        ret.append(",&nbsp;shock:&nbsp;").append(l.shock);
        if (l.siege != Integer.MIN_VALUE)
            ret.append(",&nbsp;siege:&nbsp;").append(l.siege);
        if (l.dormant)
            ret.append("&nbsp;(dormant)");
        ret.append("\">");
        ret.append(l.name);
        ret.append("</span>");
        
        return ret.toString();
    }
    
    static boolean isDormant(int leaderId) {
        return allLeaders.get(leaderId) != null && allLeaders.get(leaderId).dormant;
    }
    
    private static class Leader {
        private int id;
        private String category;
        private String name;
        private GregorianCalendar startdate;
        private GregorianCalendar deathdate;
        private int rank;
        private int movement;
        private int fire;
        private int shock;
        private int siege = Integer.MIN_VALUE;
        private boolean dormant;
        private String remark;  // not used
        
        private static final Pattern SPACE = Pattern.compile(" ");
        
        private Leader(EUGScanner scanner) {
            if (scanner.nextToken() != TokenType.LBRACE) {
                warn("No '{' after \"leader =\"", scanner.getLine(), scanner.getColumn());
            }

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
                        } else if (ident.equals("category")) {
                            scanner.nextToken();
                            category = capitalize(scanner.lastStr());
                        } else if (ident.equals("startdate")) {
                            startdate = ParseUtils.parseDate(scanner);
                        } else if (ident.equals("deathdate") || ident.equals("enddate")) {
                            deathdate = ParseUtils.parseDate(scanner);
                        } else if (ident.equals("rank")) {
                            scanner.nextToken();
                            rank = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("movement")) {
                            scanner.nextToken();
                            movement = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("fire")) {
                            scanner.nextToken();
                            fire = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("shock")) {
                            scanner.nextToken();
                            shock = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("siege")) {
                            scanner.nextToken();
                            siege = Integer.parseInt(scanner.lastStr());
                        } else if (ident.equals("dormant")) {
                            scanner.nextToken();
                            dormant = (scanner.lastStr().equalsIgnoreCase("yes"));
                        } else if (ident.equals("remark")) {
                            scanner.nextToken();
                            remark = scanner.lastStr();
                        } else if (ident.equals("location")) {
                            scanner.nextToken();    // skip
                        } else if (ident.equals("special")) {
                            scanner.nextToken();    // skip
                        } else {
                            warn("Unknown variable in leader: " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                        }
                        break;
                    case RBRACE:
                        break parseLoop;
                    default:
                        warn("Invalid leader part: " + scanner.lastToken(), scanner.getLine(), scanner.getColumn());
                        break;
                }
            }
        }

        private static final String capitalize(final String str) {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
    }

    private LeaderDB() {
    }
}
