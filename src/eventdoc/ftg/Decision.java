package eventdoc.ftg;

import eug.parser.EUGScanner;
import eug.parser.TokenType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Michael
 */
public class Decision implements EventDecision {
    
    private int id;
    private Trigger potential;
    private Trigger trigger;
    private Trigger aiTrigger;
    private boolean major;
    private boolean unique;
    private boolean persistent;
    private String name;
    private String desc;
    private Action action;
    
    Decision(EUGScanner scanner) {
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("No '{' after \"decision =\"", scanner.getLine(), scanner.getColumn());
        }
        
        parseLoop: while (true) {
            switch (scanner.nextToken()) {
                case IDENT:
                    String ident = scanner.lastStr().toLowerCase();
                    if (ident.equals("id")) {
                        scanner.nextToken();
                        id = Integer.parseInt(scanner.lastStr());
                    } else if (ident.equals("potential")) {
                        potential = Trigger.parseTrigger(scanner, this);
                    } else if (ident.equals("trigger")) {
                        trigger = Trigger.parseTrigger(scanner, this);
                    } else if (ident.equals("ai_trigger")) {
                        aiTrigger = Trigger.parseTrigger(scanner, this);
                    } else if (ident.equals("major")) {
                        scanner.nextToken();
                        major = (scanner.lastStr().equalsIgnoreCase("yes"));
                    } else if (ident.equals("unique")) {
                        scanner.nextToken();
                        unique = (scanner.lastStr().equalsIgnoreCase("yes"));
                    } else if (ident.equals("persistent")) {
                        scanner.nextToken();
                        persistent = (scanner.lastStr().equalsIgnoreCase("yes"));
                    } else if (ident.equals("name")) {
                        scanner.nextToken();
                        name = scanner.lastStr();
                    } else if (ident.equals("desc")) {
                        scanner.nextToken();
                        desc = scanner.lastStr();
                    } else if (ident.equals("action")) {
                        action = new Action(scanner, this);
                    } else {
                        warn("Unknown variable in decision: " + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                    }
                    break;
                case RBRACE:
                    break parseLoop;
                default:
                    warn("Invalid decision part: " + scanner.lastToken(), scanner.getLine(), scanner.getColumn());
                    break;
            }
        }
    }
    

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
    
    
    private List<Integer> canTrigger = null;
    
    /**
     * @return a list of events that this decision can trigger.
     */
    List<Integer> canTrigger() {
        if (canTrigger == null) {
            canTrigger = new ArrayList<>();

            for (Command command : action.getCommands()) {
                int otherId = command.getTriggeredEvent();
                if (otherId > 0) {
                    canTrigger.add(otherId);
                }
            }
        }
        return canTrigger;
    }
    
    private List<Integer> canSleep = null;

    List<Integer> canSleep() {
        if (canSleep == null) {
            canSleep = new ArrayList<>();
            for (Command command : action.getCommands()) {
                int otherId = command.getSleptEvent();
                if (otherId > 0) {
                    canSleep.add(otherId);
                }
            }
        }
        return canSleep;
    }
    
    private static final Pattern COLOR_PATTERN = Pattern.compile("§[WY]");
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\\\n");
    
    private static String normalize(String str) {
        str = COLOR_PATTERN.matcher(str).replaceAll("");
        return NEWLINE_PATTERN.matcher(str).replaceAll("<br />");
    }
    
    private static void warn(final String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }
    
    
    public static final Comparator<Decision> SORT_BY_ID = new Comparator<Decision>() {
        @Override
        public int compare(Decision o1, Decision o2) {
            return o1.getId() -  o2.getId();
        }
    };

    @Override
    public void generateHTML(BufferedWriter out) throws IOException {
        Event.currentId = id; // hack, but it's only used for printing warnings in triggers
        
        out.write("<div class=\"event\">");
        out.newLine();
        out.write("<div class=\"event_head\"><h2><a id=\"dec" + id + "\" class=\"event_title\">Decision " + id + ". " + Text.getText(name) + "</a></h2></div>");
        out.newLine();
        out.write("<div class=\"event_body\">");
        out.newLine();
        if (major) {
            out.write("<h3>Major</h3>");
            out.newLine();
        }
        if (unique) {
            out.write("<h3>Unique</h3>");
            out.newLine();
        }
        if (persistent) {
            out.write("<h3>Repeatable decision</h3>");
            out.newLine();
        }
        
        if (potential != null && potential.getSize() > 0) {
            out.write("<h3>Visiblity conditions</h3>");
            out.newLine();
            potential.generateHTML(out);
        }
        if (trigger != null && trigger.getSize() > 0) {
            out.write("<h3>Conditions</h3>");
            out.newLine();
            trigger.generateHTML(out);
        }
        if (aiTrigger != null && aiTrigger.getSize() > 0) {
            out.write("<h3>AI conditions</h3>");
            out.newLine();
            aiTrigger.generateHTML(out);
        }
        
        out.write("<h3>Description</h3>");
        out.newLine();
        out.write(normalize(Text.getText(desc)));
        out.newLine();
        
        out.write("<h3>Effects</h3>");
        out.newLine();
        out.write("<blockquote>");
        out.newLine();
        
        action.generateHTML(out);
        
        out.write("</blockquote>");
        out.newLine();
        out.write("</div> <!-- End of decision body -->");
        out.newLine();
        
        out.write("<div class=\"event_footer\">");
        out.newLine();
        out.write("<p><a href=\"#top\">Back to top</a></p>");
        out.newLine();
        out.write("</div>");
        out.newLine();
        out.write("</div> <!-- End of decision -->");
        out.newLine();
    }
    
}
