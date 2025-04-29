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
import java.util.Calendar;
import java.util.Collections;
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
    
    static final Map<Integer, Monarch> allMonarchs = new HashMap<>(5000);
    
    private static final Map<Integer, List<EventDecision>> allWakes = new HashMap<>();
    private static final Map<Integer, List<EventDecision>> allSleeps = new HashMap<>();
    
//    private static final Map<String, Map<Integer, Monarch>> byCountry =
//            new HashMap<String, Map<Integer, Monarch>>(200);
    
    public static void init(String directory) {
        System.out.println("Parsing monarchs from " + directory);
        for (File f : Main.resolver.listFiles(directory)) {
            String tag = f.getPath().substring(f.getPath().lastIndexOf('_') + 1, f.getPath().lastIndexOf('.'));
            if (Text.getText(tag).equalsIgnoreCase(tag)) {
                continue;   // not a country tag
            }
            
            parse(f, tag);
        }
//        try {
//            Files.write(Paths.get("monarchs.txt"), (Iterable<String>)allMonarchs.entrySet().stream()
//                    .sorted(Map.Entry.comparingByKey())
//                    .map(e -> { return e.getKey() + "\t" + e.getValue().tag.toUpperCase() + "\t" + e.getValue().name.replace("&nbsp;", " "); })
//                    .map(String::valueOf)::iterator);
//        } catch (IOException ex) {
//        }
        System.out.println("Finished monarchs");
    }

    private static void parse(File f, String tag) {
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
                        Monarch m = new Monarch(scanner, tag);
                        if (allMonarchs.get(m.id) != null) {
                            System.out.println("Monarch ID conflict: " + m.name + " (" + m.tag + ") and " + allMonarchs.get(m.id).name + " (" + allMonarchs.get(m.id).tag + ") both have ID " + m.id);
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
    
    static Monarch getMonarch(int monarchId) {
        return allMonarchs.get(monarchId);
    }
    
    static void addSleep(int monarchId, EventDecision evtDec) {
        List<EventDecision> evtSleeps = allSleeps.get(monarchId);
        if (evtSleeps == null) {
            evtSleeps = new ArrayList<>();
            allSleeps.put(monarchId, evtSleeps);
        }
        
        if (!evtSleeps.contains(evtDec))
            evtSleeps.add(evtDec);
    }
    
    static void addWake(int monarchId, EventDecision evtDec) {
        List<EventDecision> evtWakes = allWakes.get(monarchId);
        if (evtWakes == null) {
            evtWakes = new ArrayList<>();
            allWakes.put(monarchId, evtWakes);
        }
        
        if (!evtWakes.contains(evtDec))
            evtWakes.add(evtDec);
    }
    
    public static String getTableHtml() {
        StringBuilder sb = new StringBuilder(allMonarchs.size() * 50);
        sb.append("<div class=\"table-wrapper\">\n");
        sb.append("<table>\n<tr><th>ID</th><th>Name</th><th>Country</th><th>Start</th><th>End</th><th>DIP</th><th>ADM</th><th>MIL</th><th>Total</th><th>Dormant</th><th>Woken by</th><th>Slept by</th><th>May be emperor</th><th>May have royal marriages</th><th>Union</th><th>Remark</th></tr>\n");
        
        allMonarchs.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            sb.append("<tr>");
            appendTD(sb, e.getKey());
            Monarch m = e.getValue();
            appendTD(sb, m.name);
            appendTD(sb, EventDB.formatCountry(m.tag));
            appendTD(sb, m.startdate == null ? "(none)" : m.startdate.get(Calendar.YEAR));
            appendTD(sb, m.deathdate == null ? "(none)" : m.deathdate.get(Calendar.YEAR));
            appendTD(sb, m.DIP);
            appendTD(sb, m.ADM);
            appendTD(sb, m.MIL);
            appendTD(sb, m.DIP+m.ADM+m.MIL);
            appendTD(sb, m.dormant ? "Yes" : "");
            appendTD(sb, getEventLinkList(allWakes.get(m.id)));
            appendTD(sb, getEventLinkList(allSleeps.get(m.id)));
            appendTD(sb, m.emperor ? "" : "No");
            appendTD(sb, m.dynastic ? "" : "No");
            if (m.unions != null) {
                Union u = m.unions.get(0);
                String union;
                if (u.primary == m.id) {
                    Monarch other = allMonarchs.get(u.secondary);
                    union = "Leads union with " + other.name + " (" + EventDB.formatCountry(other.tag) + ")";
                } else {
                    Monarch other = allMonarchs.get(u.primary);
                    union = "Junior in union with " + other.name + " (" + EventDB.formatCountry(other.tag) + ")";
                }
                if (m.unions.size() > 1)
                    union += " (+" + (m.unions.size() -1) + ")";
                
                appendTD(sb, union);
            } else {
                appendTD(sb, "");
            }
            appendTD(sb, m.remark == null ? "" : m.remark);
            sb.append("</tr>\n");
        });
        
        sb.append("</table>\n");
        sb.append("</div>\n<br>");
        sb.append(allMonarchs.size()).append(" total monarchs\n");
        return sb.toString();
    }
    private static void appendTD(StringBuilder sb, Object value) {
        sb.append("<td>").append(value).append("</td>");
    }
    private static String getEventLinkList(List<EventDecision> evts) {
        if (evts == null)
            return "";
        
        StringBuilder ret = new StringBuilder(evts.size() * 80);
        
        Collections.sort(evts, EventDB.SORT_BY_DATE_DECISIONS_FIRST);
        for (EventDecision e : evts) {
            if (e instanceof Decision) {
                ret.append(EventDB.makeSubfolderDecisionLink(e.getId()));
            } else {
                ret.append(EventDB.makeSubfolderLink(e.getId()));
            }
            ret.append("<br />");
        }
        return ret.toString();
    }
    
    static class Monarch {
        private int id;
        String tag;
        String name;
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
        
        private Monarch(EUGScanner scanner, String tag) {
            this.tag = tag;
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
