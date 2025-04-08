package eventdoc.ftg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael
 */
public class EventFlag {
    private final String flagName;
    private final Set<Action> sets;
    private final Set<Action> clears;
    private final Set<EventDecision> triggerIf;
    //private Set<Event> triggerNotIf;
    
    
    /// static ///
    private static final Map<String, EventFlag> database = new HashMap<>();
    
    public static EventFlag getFlag(String name) {
        return database.computeIfAbsent(name, s -> new EventFlag(s));
    }
    
    public static void checkAllFlags() {
        for (EventFlag f : database.values()) {
            if (f.sets.isEmpty())
                System.out.println(f.flagName + " not set");
            if (f.triggerIf.isEmpty()) // && f.triggerNotIf.isEmpty())
                System.out.println(f.flagName + " not checked");
        }
        
        List<String> orderedKeys = new ArrayList<>(database.keySet());
        Collections.sort(orderedKeys);
        
        System.out.format("%30s %10s %10s %10s%n", "Flag name", "# sets", "# clears", "# triggers");
        for (String k : orderedKeys) {
            EventFlag f = database.get(k);
            System.out.format("%30s %10d %10d %10d%n", f.flagName, f.sets.size(), f.clears.size(), f.triggerIf.size());
        }
    }
    
    static String formatFlag(String flag) {
        final EventFlag f = database.get(flag);
        if (f == null) {
            System.out.println("Cannot find event flag " + flag);
            return "<font color=\"red\">"+flag+"</font>";
        }
        
        String flagFormattedName = flag;
        if (flag.matches("\\d+"))
            flagFormattedName = flag + " (" + Text.getTextCleaned("EE_FLAG_" + flag) + ")";
        
        return "<a href=../eventflags.htm#flag" + flag + " class='a_und'>" + flagFormattedName + "</a>";
    }
    
    static String getTableHtml() {
        StringBuilder sb = new StringBuilder(database.size() * 100);
        
        database.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            EventFlag e = entry.getValue();
            sb.append("<div class=\"event_flag_wrapper\">\n");
            
            String flagFormattedName = e.flagName;
            if (e.flagName.matches("\\d+"))
                flagFormattedName = Text.getTextCleaned("EE_FLAG_" + e.flagName);
            
            sb.append("<h2><a id='flag").append(e.flagName).append("' class='event_title'>").append(flagFormattedName).append("</a></h2>\n");
            
            sb.append("<div class=\"event_flag_table_wrapper\">\n");
            sb.append("<table>\n<tr><th>Set by</th><th>Cleared by</th><th>Triggers</th></tr>\n");
            sb.append("<tr>");
            StringBuilder sets = new StringBuilder(e.sets.size() * 50);
            for (Action act : e.sets) {
                EventDecision evtDec = act.getParent();
                if (evtDec instanceof Event)
                    sets.append(EventDB.makeSubfolderLink(evtDec.getId()));
                else
                    sets.append(EventDB.makeSubfolderDecisionLink(evtDec.getId()));
                sets.append("<br>");
            }
            if (e.sets.isEmpty()) {
                sets.append("(none)");
            }
            appendTD(sb, sets);
            
            StringBuilder clears = new StringBuilder(e.clears.size() * 50);
            for (Action act : e.clears) {
                EventDecision evtDec = act.getParent();
                if (evtDec instanceof Event)
                    clears.append(EventDB.makeSubfolderLink(evtDec.getId()));
                else
                    clears.append(EventDB.makeSubfolderDecisionLink(evtDec.getId()));
                clears.append("<br>");
            }
            if (e.clears.isEmpty()) {
                clears.append("(none)");
            }
            appendTD(sb, clears);
            
            StringBuilder triggered = new StringBuilder(e.triggerIf.size() * 50);
            List<EventDecision> triggerIf = new ArrayList<>(e.triggerIf);
            Collections.sort(triggerIf, (o1, o2) -> {
                return Integer.compare(o1.getId(), o2.getId()); // probably should sort decisions and events separately too
            });
            for (EventDecision evtDec : triggerIf) {
                if (evtDec instanceof Event)
                    triggered.append(EventDB.makeSubfolderLink(evtDec.getId()));
                else
                    triggered.append(EventDB.makeSubfolderDecisionLink(evtDec.getId()));
                triggered.append("<br>");
            }
            if (e.triggerIf.isEmpty()) {
                triggered.append("(none)");
            }
            appendTD(sb, triggered);
            
            sb.append("</tr>\n");
            sb.append("</table>\n");
            sb.append("</div>\n<br>");
            sb.append("</div>\n<br>");
        });
        
        sb.append("<p class='event_flag_wrapper'>").append(database.size()).append(" total event flags</p>\n");
        return sb.toString();
    }
    private static void appendTD(StringBuilder sb, Object value) {
        sb.append("<td>").append(value).append("</td>");
    }
    /// end static ///
    
    
    private EventFlag(String name) {
        this.flagName = name;
        this.sets = new HashSet<>();
        this.clears = new HashSet<>();
        this.triggerIf = new HashSet<>();
        //this.triggerNotIf = new HashSet<>();
    }
    
    void addSet(Action a) {
        if (sets.contains(a))
            System.out.println("Flag " + flagName + " is set more than once in event or decision " + a.getParent().getId() + " (" + a.getParent().getName() + ")");
        sets.add(a);
    }
    
    void addClear(Action a) {
        if (clears.contains(a))
            System.out.println("Flag " + flagName + " is cleared more than once in event or decision " + a.getParent().getId() + " (" + a.getParent().getName() + ")");
        clears.add(a);
    }
    
    void addTriggerIf(EventDecision e) {
        //if (triggerIf.contains(e))
        //    System.out.println("Flag " + flagName + " is checked more than once in event or decision " + e.getId() + " (" + e.getName() + ")");
        triggerIf.add(e);
    }
    
//    void addTriggerNotIf(Event e) {
//        if (triggerNotIf.contains(e))
//            System.out.println("Flag " + flagName + " is checked more than once in event " + e.getId() + " (" + e.getName() + ")");
//        triggerNotIf.add(e);
//    }
}
