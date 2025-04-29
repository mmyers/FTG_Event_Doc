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
public class LeaderDB {

    private static final Map<Integer, Leader> allLeaders = new HashMap<>(5000);
    
    private static final Map<Integer, List<EventDecision>> allWakes = new HashMap<>();
    private static final Map<Integer, List<EventDecision>> allSleeps = new HashMap<>();
    
    public static void init(String directory) {
        System.out.println("Parsing leaders from " + directory);
        for (File f : Main.resolver.listFiles(directory)) {
            String tag = f.getPath().substring(f.getPath().lastIndexOf('_') + 1, f.getPath().lastIndexOf('.'));
            if (Text.getText(tag).equalsIgnoreCase(tag)) {
                continue;   // not a country tag
            }
            
            parse(f, tag);
        }
//        try {
//            Files.write(Paths.get("leaders.txt"), (Iterable<String>)allLeaders.entrySet().stream()
//                    .sorted(Map.Entry.comparingByKey())
//                    .map(e -> { return e.getKey() + "\t" + e.getValue().tag.toUpperCase() + "\t" + e.getValue().category + "\t" + e.getValue().name.replace("&nbsp;", " "); })
//                    .map(String::valueOf)::iterator);
//        } catch (IOException ex) {
//        }
        System.out.println("Finished leaders");
    }

    private static void parse(File f, String tag) {
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
                        Leader l = new Leader(scanner, tag);
                        if (allLeaders.get(l.id) != null) {
                            System.out.println("Leader ID conflict: " + l.name + " (" + l.tag + ") and " + allLeaders.get(l.id).name + " (" + allLeaders.get(l.id).tag + ") both have ID " + l.id);
                        }
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
    
    public static void checkForMonarchOverlap() {
        System.out.println("Checking for leader/monarch ID collisions...");
        for (Map.Entry<Integer, Leader> entry : allLeaders.entrySet()) {
            int leaderId = entry.getKey();
            MonarchDB.Monarch m = MonarchDB.getMonarch(leaderId);
            if (m != null) {
                Leader l = entry.getValue();
                String leaderName = l.name.replace("&nbsp;", " ");
                String monarchName = m.name.replace("&nbsp;", " ");
                System.out.println("Leader/monarch ID conflict: leader " + leaderName + " (" + l.tag.toUpperCase() + ") and " + monarchName + " (" + m.tag.toUpperCase() + ") both have ID " + leaderId);
            }
        }
        System.out.println("Done");
    }
    
    public static void dumpLeaderMonarchIdRanges() {
        System.out.println("Generating leader/monarch ID ranges...");
        java.util.Set<Integer> allIds = new java.util.HashSet<>(allLeaders.keySet());
        allIds.addAll(MonarchDB.allMonarchs.keySet());
        java.util.List<Integer> idsInOrder = new java.util.ArrayList<>(allIds);
        Collections.sort(idsInOrder);
        System.out.println(idsInOrder.size() + " total IDs used");
        
        int last = 0;
        int lastFree = 0;
        
        for (int i : idsInOrder) {
            if (last == 0) {
                lastFree = i - 1;
                last = i;
            } else if (i == (last+1)) { // in sequence
                last = i;
            } else {
                // Not in sequence. We've finished an ID range, and we also know a range of free IDs
                System.out.println("Used IDs: " + (lastFree+1) + " - " + last + " (" + (last - lastFree) + " IDs)");
                System.out.println((i - last - 1) + " free IDs");
                lastFree = i - 1;
                last = i;
            }
        }
        System.out.println("Used IDs: " + (lastFree+1) + " - " + last + " (" + (last - lastFree) + " IDs)");
        System.out.println("Free IDs: " + (last+1) + " and up");
        System.out.println("Done");
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
        if (l.siege > 0)
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
    
    
    static void addSleep(int leaderId, EventDecision evtDec) {
        List<EventDecision> evtSleeps = allSleeps.get(leaderId);
        if (evtSleeps == null) {
            evtSleeps = new ArrayList<>();
            allSleeps.put(leaderId, evtSleeps);
        }
        
        if (!evtSleeps.contains(evtDec))
            evtSleeps.add(evtDec);
    }
    
    static void addWake(int leaderId, EventDecision evtDec) {
        List<EventDecision> evtWakes = allWakes.get(leaderId);
        if (evtWakes == null) {
            evtWakes = new ArrayList<>();
            allWakes.put(leaderId, evtWakes);
        }
        
        if (!evtWakes.contains(evtDec))
            evtWakes.add(evtDec);
    }
    
    public static String getTableHtml() {
        StringBuilder sb = new StringBuilder(allLeaders.size() * 50);
        sb.append("<div class=\"table-wrapper\">\n");
        sb.append("<table>\n<tr><th>ID</th><th>Name</th><th>Country</th><th>Category</th><th>Rank</th><th>Start</th><th>End</th><th>Movement</th><th>Fire</th><th>Shock</th><th>Siege</th><th>Total</th><th>Total without siege</th><th>Dormant</th><th>Woken by</th><th>Slept by</th><th>Location</th><th>Remark</th></tr>\n");
        
        allLeaders.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            sb.append("<tr>");
            appendTD(sb, e.getKey());
            Leader l = e.getValue();
            appendTD(sb, l.name);
            appendTD(sb, EventDB.formatCountry(l.tag));
            appendTD(sb, l.category);
            appendTD(sb, l.rank);
            appendTD(sb, l.startdate == null ? "(none)" : l.startdate.get(Calendar.YEAR));
            appendTD(sb, l.deathdate == null ? "(none)" : l.deathdate.get(Calendar.YEAR));
            appendTD(sb, l.movement);
            appendTD(sb, l.fire);
            appendTD(sb, l.shock);
            appendTD(sb, l.siege);
            appendTD(sb, l.movement + l.fire + l.shock + l.siege);
            appendTD(sb, l.movement + l.fire + l.shock);
            appendTD(sb, l.dormant ? "Yes" : "");
            appendTD(sb, getEventLinkList(allWakes.get(l.id)));
            appendTD(sb, getEventLinkList(allSleeps.get(l.id)));
            appendTD(sb, l.location > 0 ? ProvinceDB.format(l.location) : "");
            appendTD(sb, l.remark == null ? "" : l.remark);
            sb.append("</tr>\n");
        });
        
        sb.append("</table>\n");
        sb.append("</div>\n<br>");
        sb.append(allLeaders.size()).append(" total leaders\n");
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
    
    private static class Leader {
        private int id;
        private String tag;
        private String category;
        private String name;
        private GregorianCalendar startdate;
        private GregorianCalendar deathdate;
        private int rank;
        private int movement;
        private int fire;
        private int shock;
        private int siege = 0;
        private boolean dormant;
        private int location = -1;
        private String remark;  // not used
        
        private static final Pattern SPACE = Pattern.compile(" ");
        
        private Leader(EUGScanner scanner, String tag) {
            this.tag = tag;
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
                            scanner.nextToken();
                            location = Integer.parseInt(scanner.lastStr());
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
