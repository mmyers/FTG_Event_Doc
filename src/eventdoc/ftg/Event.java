package eventdoc.ftg;

import eug.parser.EUGScanner;
import eug.parser.TokenType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Event {

    private int id;
    private Trigger trigger;
    private boolean random;
    private boolean global; // FTG
    private boolean hidden; // FTG
    private boolean persistent; // FTG
    private int totalChance = -1; // total of all ai_chances of actions
    private String tag = null;
    private int province = -1;
    private String name;
    private String desc;
    private GregorianCalendar date;
    private int offset = -1;
    private GregorianCalendar deathdate;
    private List<Action> actions;

    Event(EUGScanner scanner) {
        actions = new ArrayList<Action>();
        
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("No '{' after \"event =\"", scanner.getLine(), scanner.getColumn());
        }
        
        parseLoop: while (true) {
            switch (scanner.nextToken()) {
                case IDENT:
                    String ident = scanner.lastStr().toLowerCase();
                    if (ident.equals("id")) {
                        scanner.nextToken();
                        id = Integer.parseInt(scanner.lastStr());
                    } else if (ident.equals("trigger")) {
                        trigger = Trigger.parseTrigger(scanner);
                    } else if (ident.equals("random")) {
                        scanner.nextToken();
                        random = (scanner.lastStr().equalsIgnoreCase("yes"));
                    } else if (ident.equals("global")) {
                        scanner.nextToken();
                        global = (scanner.lastStr().equalsIgnoreCase("yes"));
                    } else if (ident.equals("hidden")) {
                        scanner.nextToken();
                        hidden = (scanner.lastStr().equalsIgnoreCase("yes"));
                    } else if (ident.equals("persistent")) {
                        scanner.nextToken();
                        persistent = (scanner.lastStr().equalsIgnoreCase("yes"));
                    } else if (ident.equals("country")) {
                        scanner.nextToken();
                        tag = scanner.lastStr();
                    } else if (ident.equals("province")) {
                        scanner.nextToken();
                        province = Integer.parseInt(scanner.lastStr());
                    } else if (ident.equals("name")) {
                        scanner.nextToken();
                        name = scanner.lastStr();
                    } else if (ident.equals("desc")) {
                        scanner.nextToken();
                        desc = scanner.lastStr();
                    } else if (ident.equals("date")) {
                        date = ParseUtils.parseDate(scanner);
                    } else if (ident.equals("offset")) {
                        scanner.nextToken();
                        offset = Integer.parseInt(scanner.lastStr());
                    } else if (ident.equals("deathdate")) {
                        deathdate = ParseUtils.parseDate(scanner);
                    } else if (ident.equals("action_a")
                            || ident.equals("action_b")
                            || ident.equals("action_c")
                            || ident.equals("action_d")
                            || ident.equals("action")) {
                        Action act = new Action(scanner);
                        actions.add(act);
                        if (act.getAIChance() >= 0)
                            totalChance += act.getAIChance();
                    } else if (ident.equals("style")) {
                        scanner.nextToken();    // ignore
                    } else {
                        warn("Unknown variable in event: " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                    }
                    break;
                case RBRACE:
                    break parseLoop;
                default:
                    warn("Invalid event part: " + scanner.lastToken(), scanner.getLine(), scanner.getColumn());
                    break;
            }
        }
    }

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public int getProvince() {
        return province;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isRandom() {
        return random;
    }

    public GregorianCalendar getStartDate() {
        return date;
    }

    public GregorianCalendar getEndDate() {
        return deathdate;
    }
    
    // Used only when generating warnings in Trigger.java during HTML output.
    static int currentId = -1;  // extremely bad practice, but it's the best I could do on short notice

    void generateHTML(BufferedWriter out) throws IOException {
        currentId = id;
        
        out.write("<div class=\"event\">");
        out.newLine();
        out.write("<div class=\"event_head\"><h2><a id=\"evt" + id + "\" class=\"event_title\">" + id + ". " + Text.getText(name) + "</a></h2></div>");
        out.newLine();
        out.write("<div class=\"event_body\">");
        out.newLine();
        out.write("<h3>");
        if (tag != null) {
            out.write(EventDB.formatCountry(tag));
        } else if (province != -1) {
            out.write("Province of " + ProvinceDB.format(province));
        } else {
            out.write("All countries");
        }
        out.write(" &mdash; ");
        out.write(random ? "Random</h3>" : "Not random</h3>");
        out.newLine();
        if (global) {
            out.write("<h3>Shown to all players</h3>");
            out.newLine();
        } else if (hidden) {
            out.write("<h3>Not shown to any other player</h3>");
            out.newLine();
        }
        
        if (trigger != null) {
            out.write("<h3>Conditions</h3>");
            out.newLine();
            trigger.generateHTML(out);
        }
        
        if (date == null && !random) {
            out.write("<h3>Triggered by</h3>");
            out.newLine();
            Map<Integer, String> triggeredBy = EventDB.triggersOf(id);
            if (triggeredBy != null) {
                for (Map.Entry<Integer, String> evt : triggeredBy.entrySet()) {
                    out.write("Action ");
                    out.write(evt.getValue());
                    out.write(" of ");
                    out.write(EventDB.makeLink(evt.getKey()));
                    out.write("<br />");
                    out.newLine();
                }
            } else {
                System.out.println("Warning: Event " + id + " has no date, is not random, and is not triggered by another event");
                out.write("<span class=\"error\">Not triggered!</span>");
                out.newLine();
            }
        }
        
        if (date != null) {
            out.write("<p>");
            if (offset != -1) {
                out.write("Will happen within " + offset + " days of ");
                out.write(String.format(Locale.US, "<span class=\"date\">%1$tB %1$te, %1$tY</span>", date));
                out.newLine();
                if (deathdate != null) {
                    out.write("<br />Checked again every " + offset + " days until trigger is met (cannot happen after ");
                    out.write(String.format(Locale.US, "<span class=\"date\">%1$tB %1$te, %1$tY</span>", deathdate));
                    out.write(")");
                    out.newLine();
                }
            } else {
                if (deathdate == null) {
                    out.write("Will happen on ");
                    out.write(String.format(Locale.US, "<span class=\"date\">%1$tB %1$te, %1$tY</span>", date));
                    out.newLine();
                } else {
                    out.write("Will happen between ");
                    out.write(String.format(Locale.US, "<span class=\"date\">%1$tB %1$te, %1$tY</span>", date));
                    out.write(" and ");
                    out.write(String.format(Locale.US, "<span class=\"date\">%1$tB %1$te, %1$tY</span>", deathdate));
                    out.newLine();
                }
            }

            Map<Integer, String> sleptBy = EventDB.whatSleeps(id);
            if (sleptBy != null) {
                out.newLine();
                out.write("<br />unless prevented by<br />");
                for (Map.Entry<Integer, String> evt : sleptBy.entrySet()) {
                    out.write("Action ");
                    out.write(evt.getValue());
                    out.write(" of ");
                    out.write(EventDB.makeLink(evt.getKey()));
                    out.write("<br />");
                    out.newLine();
                }
            }
            
            if (persistent) {
                out.write("<br />Can happen multiple times");
                out.newLine();
            }
        
            out.write("</p>");
            out.newLine();
        }
        
        out.write("<h3>Description</h3>");
        out.newLine();
        out.write(normalize(Text.getText(desc)));
        out.newLine();
        
        out.write("<h3>Actions</h3>");
        out.newLine();
        out.write("<blockquote>");
        out.newLine();
        
        int actionLetter = 'A';
        for (Action action : actions) {
            action.generateHTML(out, (char)actionLetter, totalChance);
            actionLetter++; // will only work until Z, but that's not likely to happen
        }
        
        out.write("</blockquote>");
        out.newLine();
        out.write("</div> <!-- End of event body -->");
        out.newLine();
        
        out.write("<div class=\"event_footer\">");
        out.newLine();
        out.write("<p><a href=\"#top\">Back to top</a></p>");
        out.newLine();
        out.write("</div>");
        out.newLine();
        out.write("</div> <!-- End of event -->");
        out.newLine();
    }
    
    
    private Map<Character, List<Integer>> canTrigger = null;
    
    /**
     * @return a list of events that this event can trigger.
     */
    Map<Character, List<Integer>> canTrigger() {
        if (canTrigger == null) {
            canTrigger = new HashMap<Character, List<Integer>>();

            int actionLetter = 'A';
            for (Action action : actions) {
                List<Integer> tmp = new ArrayList<Integer>();
                for (Command command : action.getCommands()) {
                    int otherId = command.getTriggeredEvent();
                    if (otherId > 0) {
                        tmp.add(otherId);
                    }
                }
                canTrigger.put((char) actionLetter, tmp);
                actionLetter++;
            }
        }
        return canTrigger;
    }
    
    private Map<Character, List<Integer>> canSleep = null;

    Map<Character, List<Integer>> canSleep() {
        if (canSleep == null) {
            canSleep = new HashMap<Character, List<Integer>>();
            int actionLetter = 'A';
            for (Action action : actions) {
                List<Integer> tmp = new ArrayList<Integer>();
                for (Command command : action.getCommands()) {
                    int otherId = command.getSleptEvent();
                    if (otherId > 0) {
                        tmp.add(otherId);
                    }
                }
                canSleep.put((char) actionLetter, tmp);
                actionLetter++;
            }
        }
        return canSleep;
    }
    
    private static final Pattern colorPattern = Pattern.compile("§[WY]");
    private static final Pattern newLinePattern = Pattern.compile("\\\\n");
    
    private static final String normalize(String str) {
        str = colorPattern.matcher(str).replaceAll("");
        return newLinePattern.matcher(str).replaceAll("<br />");
    }
    
    private static void warn(final String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }

    public static Comparator<Event> SORT_BY_DATE = new Comparator<Event>() {
        private int compareDates(GregorianCalendar c1, GregorianCalendar c2) {
            if (c1 == null)
                return (c2 == null ? 0 : 1);
            if (c2 == null)
                return -1;

            return c1.compareTo(c2);
        }

        @Override
        public int compare(Event o1, Event o2) {
            int ret = compareDates(o1.date, o2.date);
            if (ret == 0)
                ret = compareDates(o1.deathdate, o2.deathdate);
            if (ret == 0) {
                // neither has a date (or both have the same)
                if (o1.isRandom())
                    ret = (o2.isRandom() ? 0 : 1);
                else if (o2.isRandom())
                    ret = -1;
            }
            if (ret == 0) {
                if (EventDB.triggersOf(o1.id) != null) {
                    if (EventDB.triggersOf(o2.id) != null) {
                        int id1 = EventDB.triggersOf(o1.id).keySet().iterator().next();
                        int id2 = EventDB.triggersOf(o2.id).keySet().iterator().next();
                        ret = compare(EventDB.getEvent(id1), EventDB.getEvent(id2));
                    }
                }
            }
            if (ret == 0)
                ret = Text.getText(o1.getName()).compareTo(Text.getText(o2.getName()));
            return ret;
        }
    };
}
