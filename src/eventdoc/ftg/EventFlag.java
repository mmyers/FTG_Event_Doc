package eventdoc.ftg;

import java.util.HashMap;
import java.util.HashSet;
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
        if (triggerIf.contains(e))
            System.out.println("Flag " + flagName + " is checked more than once in event or decision " + e.getId() + " (" + e.getName() + ")");
        triggerIf.add(e);
    }
    
//    void addTriggerNotIf(Event e) {
//        if (triggerNotIf.contains(e))
//            System.out.println("Flag " + flagName + " is checked more than once in event " + e.getId() + " (" + e.getName() + ")");
//        triggerNotIf.add(e);
//    }
}
