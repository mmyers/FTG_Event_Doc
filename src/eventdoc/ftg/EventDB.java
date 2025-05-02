/*
 * EventDB.java
 *
 * Created on Mar 13, 2008, 8:59:19 PM
 */

package eventdoc.ftg;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Michael
 */
class EventDB {

    private static final Map<String, List<EventDecision>> eventFiles = new HashMap<>(50);
    
    private static final Map<Integer, Event> allEvents = new HashMap<>(5000);
    private static final Map<Integer, Decision> allDecisions = new HashMap<>(5000);

    private static final Map<Integer, String> eventsInFiles = new HashMap<>(5000);
    private static final Map<Integer, String> decisionsInFiles = new HashMap<>(5000);
    
    private static final Map<Integer, String> eventNames = new HashMap<>(5000);
    private static final Map<Integer, String> eventDescs = new HashMap<>(5000);
    private static final Map<Integer, String> decisionNames = new HashMap<>(5000);
    private static final Map<Integer, String> decisionDescs = new HashMap<>(5000);
    
    private static final String INDEX_NAME = "eventdoc.htm";
    private static final String ALL_EVENTS_INDEX_NAME = "all_events.htm";
    private static final String ALL_EVENTS_BY_YEAR_INDEX_NAME = "all_events_by_year.htm";
    private static final String ALL_EVENTS_BY_COUNTRY_INDEX_NAME = "all_events_by_country.htm";
    private static final String MONARCHS_INDEX_NAME = "monarchs.htm";
    private static final String LEADERS_INDEX_NAME = "leaders.htm";
    private static final String EVENT_FLAG_INDEX_NAME = "eventflags.htm";

    private EventDB() {
    }

    static void loadEvents(String eventFile, String rootDir) {
        System.out.println("Loading events from " + eventFile);
        GenericObject file = EUGFileIO.load(eventFile);
        if (file == null) {
            System.out.println("Could not read file");
            return;
        }
        for (ObjectVariable var : file.values) {
            if (var.varname.equalsIgnoreCase("include")) {
                loadEvents(rootDir + File.separator + var.getValue(), rootDir); // recursion
            } else if (var.varname.equalsIgnoreCase("event")
                    || (var.varname.equalsIgnoreCase("decision"))) {
                parseEvents(rootDir + File.separator + var.getValue());
            } else {
                System.out.println("Don't know what to do with variable " + var.varname);
            }
        }
        
        initTriggers();
    }
    
    static void loadEventsFromFile(String eventFile, String rootDir) {
        parseEvents(rootDir + File.separator + eventFile);
        initTriggers();
    }

    private static void parseEvents(String eventFile) {
        File file = new File(eventFile);
        System.out.println("Parsing " + file.getAbsolutePath());
        
        List<EventDecision> currentFile = new ArrayList<>(20);
        eventFiles.put(file.getName(), currentFile);
        
        List<EventDecision> events = new EventParser(file).parse();
        for (EventDecision eventOrDec : events) {
            currentFile.add(eventOrDec);
            if (eventOrDec instanceof Event) {
                Event event = (Event) eventOrDec;
                if (allEvents.containsKey(event.getId()))
                    System.out.println("Event ID collision: " + event.getId() + " already exists in " + eventsInFiles.get(event.getId()));
                allEvents.put(event.getId(), event);
                eventNames.put(event.getId(), event.getName());
                eventDescs.put(event.getId(), event.getDesc());
                eventsInFiles.put(event.getId(), file.getName());
            } else if (eventOrDec instanceof Decision) {
                Decision dec = (Decision) eventOrDec;
                if (allDecisions.containsKey(dec.getId()))
                    System.out.println("Decision ID collision: " + dec.getId() + " already exists in " + decisionsInFiles.get(dec.getId()));
                allDecisions.put(dec.getId(), dec);
                decisionNames.put(dec.getId(), dec.getName());
                decisionDescs.put(dec.getId(), dec.getDesc());
                decisionsInFiles.put(dec.getId(), file.getName());
            } 
        }
        // Collections.sort(currentFile, SORT_BY_DATE_DECISIONS_FIRST); // we'll sort later, after all event triggering data has been collected
    }

    public static Event getEvent(int id) {
        return allEvents.get(id);
    }

    private static void writePageStart(BufferedWriter output) throws IOException {
        output.write("<!-- Custom page start content -->");
        output.newLine();
        output.write(pageStart);
        output.newLine();
        output.write("<!-- End of custom content -->");
    }

    private static void writePageEnd(BufferedWriter output) throws IOException {
        output.write("<!-- Custom page end content -->");
        output.newLine();
        output.write("<div></div> <!-- Empty div to help the layout when there is no custom content. -->");
        output.newLine();
        output.write(pageEnd);
        output.newLine();
        output.write("<!-- End of custom content -->");
        output.newLine();
    }
    
    static void generateHTML(String directory) {
        System.out.println("Creating HTML...");

        File folder = new File(directory + DOC_FOLDER);
        if (!folder.exists())
            folder.mkdir();

        List<String> files = new ArrayList<>(eventFiles.keySet());
        Collections.sort(files);

        for (String file : files) {
            System.out.println(file);
            List<EventDecision> events = eventFiles.get(file);
            Collections.sort(events, SORT_BY_DATE_DECISIONS_FIRST); // must sort here again because originally we didn't have data on which events triggered which other events!
            File page = new File(directory + DOC_FOLDER + file + ".htm");
            try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(page), StandardCharsets.UTF_8.name()))) {
                writeHeader(file, output, false, false, true);
                output.write("<body onload=\"toggle('eventlist')\">");
                output.newLine();
                
                writePageStart(output);
                output.newLine();

                // Top index
                output.write("<div class=\"index\" id=\"index\">");
                output.newLine();
                output.write("<div class=\"index_head\"><h2><a id=\"top\" class=\"index_title\">");
                output.write(file);
                output.write("</a></h2></div>");
                output.newLine();
                output.write("<div class=\"index_body\">");
                output.newLine();
                output.write("<a href=\"javascript:toggle('eventlist')\">Toggle table of contents</a>");
                output.write("<br />");
                output.newLine();
                output.write("<div class=\"eventlist\" id=\"eventlist\">");
                output.newLine();

                for (EventDecision evtOrDec : events) {
                    if (evtOrDec instanceof Decision) {
                        output.write("Decision ");
                        output.write(makeDecisionLink(((Decision)evtOrDec).getId(), false, null, true));
                        output.write("<br />");
                        output.newLine();
                        continue;
                    }
                    if (!(evtOrDec instanceof Event))
                        continue;
                    Event evt = (Event) evtOrDec;
                    writeEventYear(evt, output);
                    output.write(makeLink(evt.getId(), false, null, false));
                    output.write("<br />");
                    output.newLine();
                }

                output.write("</div> <!-- End of event list -->");
                output.newLine();
                output.write("<br />");
                output.newLine();
                
                output.write("</div> <!-- End of index body -->");
                output.newLine();

                output.write("<div class=\"index_footer\">");
                output.newLine();
                output.write("<p><a href=\"../" + INDEX_NAME + "\">Back to Index</a></p>");
                output.newLine();
                output.write("</div> <!-- End of index footer -->");
                output.newLine();
                
                output.write("</div> <!-- End of index -->");
                output.newLine();
                
                output.newLine();
                output.write("<div class=\"main\">");
                output.newLine();

                // Events/Decisions
                for (EventDecision evt : events) {
                    evt.generateHTML(output);
                    output.newLine();
                    output.write(betweenEvents);
                    output.newLine();
                    output.newLine();
                }
                
                output.write("<div class=\"index\">");
                output.newLine();
                output.write("<div class=\"index_head\"><h2><span class=\"index_title\">");
                output.write(file);
                output.write("</span></h2></div>");
                output.newLine();
                output.write("<div class=\"index_body\"></div> <!-- This must be present for the borders to work. -->");
                output.newLine();
                output.write("<div class=\"index_footer\">");
                output.newLine();
                output.write("<p><a href=\"../" + INDEX_NAME + "\">Back to index</a><br />");
                output.newLine();
                output.write("<a href=\"#top\">Back to top</a></p>");
                output.newLine();
                output.write("</div> <!-- End of index footer -->");
                output.newLine();
                output.write("</div> <!-- End of bottom index -->");
                output.newLine();

                output.write("</div> <!-- End of main div -->");
                output.newLine();
                
                output.newLine();
                writePageEnd(output);
                
                output.write("</body>");
                output.newLine();
                output.write("</html>");
                output.newLine();
            } catch (IOException ex) {
                System.err.println("Error writing to " + page + "\nPlease ensure output folder exists.");
            }
        }

        System.out.println(INDEX_NAME);
        writeIndex(directory, files);

        System.out.println(ALL_EVENTS_INDEX_NAME);
        writeLookupPage(directory);
        System.out.println(ALL_EVENTS_BY_YEAR_INDEX_NAME);
        writeYearlyLookupPage(directory);
        System.out.println(ALL_EVENTS_BY_COUNTRY_INDEX_NAME);
        writeCountryLookupPage(directory);
        
        System.out.println(MONARCHS_INDEX_NAME);
        writeMonarchTable(directory);
        
        System.out.println(LEADERS_INDEX_NAME);
        writeLeaderTable(directory);
        
        System.out.println(EVENT_FLAG_INDEX_NAME);
        writeEventFlagTable(directory);

        System.out.println("Finished creating HTML");
    }

    private static void writeEventYear(Event evt, final BufferedWriter output) throws IOException {
        if (evt.isRandom()) {
            output.write("Random: ");
        } else if (triggersOf.get(evt.getId()) != null) {
            // might have to recursively check triggers of triggers until we find one with an actual date
            List<Integer> triggeringEvents = new ArrayList<>(triggersOf.get(evt.getId()).keySet());
            for (int i = 0; i < triggeringEvents.size(); i++) {
                Event e = allEvents.get(triggeringEvents.get(i));
                do {
                    if (e.getStartDate() != null) {
                        output.write(String.valueOf(e.getStartDate().get(GregorianCalendar.YEAR)));
                        if (e.getEndDate() != null) {
                            output.write("-" + e.getEndDate().get(GregorianCalendar.YEAR));
                        }
                        if (i < triggeringEvents.size() - 1)
                            output.write(", ");
                        break;
                    } else if (e.isRandom()) {
                        output.write("random event");
                        if (i < triggeringEvents.size() - 1)
                            output.write(", ");
                        break;
                    } else if (triggersOf.get(e.getId()) != null) {
                        e = allEvents.get(triggersOf.get(e.getId()).keySet().iterator().next());
                    } else {
                        output.write("unknown event");
                        if (i < triggeringEvents.size() - 1)
                            output.write(", ");
                        break;
                    }
                } while (e != null);
            }
            if (triggeringEvents.size() > 1)
                output.write(" (triggered by " + triggeringEvents.size() + " events): ");
            else
                output.write(" (triggered): ");
        } else if (evt.getStartDate() != null) {
            output.write(String.valueOf(evt.getStartDate().get(GregorianCalendar.YEAR)));
            if (evt.getEndDate() != null) {
                output.write("-" + evt.getEndDate().get(GregorianCalendar.YEAR));
            }
            output.write(": ");
        } else if (evt.getRebelFactionTriggers() != null) {
            output.write("Triggered by rebels: ");
        } else {
            output.write("<span class=\"error\">Unknown: </span>");
        }
    }
    
    static final String makeLink(final int eventID) {
        return makeLink(eventID, true, null, true);
    }
    
    static final String makeSubfolderLink(final int eventID) {
        return makeLink(eventID, true, DOC_FOLDER, true);
    }
    
    /**
     * Returns an HTML string representing a link to the given event.
     * This is useful because one HTML page is generated per event file, and the
     * caller doesn't know which file the given event is in.
     * <p />
     * The generated link is in the form
     * <pre>
     * &lt;a href="/eventdoc/(filename).html#(eventID)" title="(event name) for (event owner)"&gt;(eventID)&lt;/a&gt;
     * </pre>
     * where (filename) is the name of the file that the event occurs in and
     * (eventID) is the given ID.
     * @param eventID the ID of the event to create a link to.
     * @param underlined whether the link should be made with class "a_und".
     * @param dir the subdirectory where the filename is located, or null if it is in the same directory.
     * @param includeID whether the link text should include the event ID.
     * @return an HTML string representing a link to the event.
     */
    static final String makeLink(int eventID, boolean underlined, String dir, boolean includeID) {
        String tag = null;
        int province = -1;

        String filename = eventsInFiles.get(eventID);
        
        Event event = allEvents.get(eventID);
        
        if (event != null) {
            tag = event.getTag();
            if (tag == null)
                province = event.getProvince();
        }
        
        if (filename == null) {
            System.out.println("Cannot find event " + eventID);
            return "<span class=\"error\" title=\"Event&nbsp;not&nbsp;found\">"+eventID+"</span>";
        }

        if (dir != null) {
            filename = dir + filename;
        }
        
        final StringBuilder ret = new StringBuilder();
        
        ret.append("<a href=\"").append(filename).append(".htm#evt").append(eventID).append("\"");
        
        if (underlined)
            ret.append(" class=\"a_und\"");
        ret.append(">");
        if (includeID)
            ret.append(eventID).append(" - ");
        ret.append(Text.getText(eventNames.get(eventID)));
        ret.append("</a>");
        
        ret.append(" for ");
        if (tag != null)
            ret.append(formatCountry(tag));
        else if (province != -1)
            ret.append(ProvinceDB.format(province));
        else
            ret.append("all countries");
        
        return ret.toString();
    }
    
    static final String makeDecisionLink(final int decisionID) {
        return makeDecisionLink(decisionID, true, null, false);
    }
    
    static final String makeSubfolderDecisionLink(final int eventID) {
        return makeDecisionLink(eventID, true, DOC_FOLDER, true);
    }
    
    static final String makeDecisionLink(int decisionID, boolean underlined, String dir, boolean includeID) {
        String filename = decisionsInFiles.get(decisionID);
        
        if (filename == null) {
            System.out.println("Cannot find decision " + decisionID);
            return "<span class=\"error\" title=\"Decision&nbsp;not&nbsp;found\">"+decisionID+"</span>";
        }

        if (dir != null) {
            filename = dir + filename;
        }
        
        final StringBuilder ret = new StringBuilder();
        
        ret.append("<a href=\"").append(filename).append(".htm#dec").append(decisionID).append("\"");
        
        if (underlined)
            ret.append(" class=\"a_und\"");
        ret.append(">");
        if (includeID)
            ret.append(decisionID).append(" - ");
        ret.append(Text.getText(decisionNames.get(decisionID)));
        ret.append("</a>");
        
        return ret.toString();
    }
    
    static String formatCountry(final String tag) {
        if (tag.equals("-6"))
            return "<span class=\"country\">The Holy Roman Emperor</span>";
        if (tag.equals("-2"))
            return "<span class=\"country\">our overlord</span>";
        if (tag.equals("-1"))
            return "this country";
        return "<span class=\"country\" title=\"" + tag.toUpperCase() + "\">" + Text.getText(tag) + "</span>";
    }
    
    // Mapping from event ID 1 to
    // (mapping from event ID 2 to string holding all actions of #2 that trigger #1)
    private static final Map<Integer, Map<Integer, String>> triggersOf = new HashMap<>();
    
    /**
     * Returns a list of events that trigger the given event.
     * @param id the event that is triggered.
     * @return all events that can trigger the given event (may be null).
     */
    static final Map<Integer, String> triggersOf(final int id) {
        return triggersOf.get(id);
    }

    private static final Map<Integer, Map<Integer, String>> sleptBy = new HashMap<>();

    static final Map<Integer, String> whatSleeps(int id) {
        return sleptBy.get(id);
    }
    
    private static void initTriggers() {
        for (Map.Entry<Integer, Event> entry : allEvents.entrySet()) {
            final int eventID = entry.getKey();
            final Map<Character, List<Integer>> eventCanTrigger = entry.getValue().canTrigger();

            for (Map.Entry<Character, List<Integer>> actionCanTrigger : eventCanTrigger.entrySet()) {
                final char action = actionCanTrigger.getKey();
                
                for (Integer id : actionCanTrigger.getValue()) {
                    Map<Integer, String> triggers = triggersOf.get(id);
                    if (triggers == null) {
                        triggers = new HashMap<>();
                        triggersOf.put(id, triggers);
                    }
                    
                    // Now we know that event 'eventID' action 'action' triggers
                    // event 'id'. We need to add it to the list.
                    
                    String str = triggers.get(eventID);
                    if (str == null) {
                        triggers.put(eventID, String.valueOf(action));
                    } else if (!str.contains(String.valueOf(action))) {
                        // If it's not already there, add it.
                        triggers.put(eventID, str + ", " + action);
                    }
                }
            }

            final Map<Character, List<Integer>> eventCanSleep = entry.getValue().canSleep();

            for (Map.Entry<Character, List<Integer>> actionCanSleep : eventCanSleep.entrySet()) {
                final char action = actionCanSleep.getKey();

                for (Integer id : actionCanSleep.getValue()) {
                    Map<Integer, String> slept = sleptBy.get(id);
                    if (slept == null) {
                        slept = new HashMap<>();
                        sleptBy.put(id, slept);
                    }

                    // Now we know that event 'eventID' action 'action' sleeps
                    // event 'id'. We need to add it to the list.

                    String str = slept.get(eventID);
                    if (str == null) {
                        slept.put(eventID, String.valueOf(action));
                    } else if (!str.contains(String.valueOf(action))) {
                        // If it's not already there, add it.
                        slept.put(eventID, str + ", " + action);
                    }
                }
            }
        }
    }
    
    private static void writeHeader(String fileTitle, BufferedWriter output, boolean isTopLevel, boolean isLongPage, boolean includeJs) throws IOException {
        output.write(HEADER);
        output.newLine();

        output.write("<html>");
        output.newLine();
        output.write("<head>");
        output.newLine();
        if (isTopLevel) {
            output.write("<!-- Generated by " + Main.NAME + " version " +
                    Main.VERSION + " on " +
                    String.format(Locale.US, "%1$tc", new GregorianCalendar()) + " -->");
            output.newLine();
        }

        output.write("<title>");
        output.write(String.format(title, fileTitle));
        output.write("</title>");
        output.newLine();
        
        output.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        output.newLine();
        output.write("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
        output.newLine();
        output.write("<meta http-equiv=\"Keywords\" content=\"For the Glory, For the Glory game\">");
        output.newLine();
        output.write("<meta http-equiv=\"Description\" content=\"For the Glory" + Main.getModName() + " game event documentation\">");
        output.newLine();

        output.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        if (!isTopLevel)
            output.write("../");
        output.write(cssDir);
        output.write(isLongPage ? "NoImage.css" : "eventdoc.css");
        output.write("\" title=\"Style\" />");
        output.newLine();
        if (includeJs)
            output.write("<script src=\"" + (isTopLevel ? "" : "../") + jsDir + "eventdoc.js\" type=\"text/javascript\"></script>");
        output.newLine();
        
        output.write("<!-- Custom header data -->");
        output.newLine();
        output.write(customHeader);
        output.newLine();
        output.write("<!-- End of custom header data -->");
        output.newLine();
        
        output.write("</head>");
        output.newLine();
    }
    
    private static void writeIndex(String directory, List<String> files) {
        File index = new File(directory + INDEX_NAME);
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(index), StandardCharsets.UTF_8.name()))) {
            writeHeader("Index", output, true, false, false);
            output.write("<body>");
            output.newLine();
            writePageStart(output);
            output.newLine();
            output.write("<div class=\"index\">");
            output.newLine();
            output.write("<div class=\"index_head\"><h2><a id=\"top\" class=\"index_title\">Index</a></h2></div>");
            output.newLine();
            output.write("<div class=\"index_body\">");
            output.newLine();
            for (String name : files) {
                int num = eventFiles.get(name).size();
                String events = (num == 1 ? "1 event" : (num + " events"));
                output.write("<a href=\"" + DOC_FOLDER + name + ".htm\"><b>" + name + "</b></a> (" + events + ")<br />");
                output.newLine();
            }
            output.write("</div> <!-- End of list -->");
            output.newLine();

            output.write("<div class=\"index_footer\"><p>");
            output.newLine();
            output.write("<a href=\"" + ALL_EVENTS_INDEX_NAME + "\" class=\"a_und\">List of all events by ID</a>");
            output.write("<br />");
            output.newLine();
            output.write("<a href=\"" + ALL_EVENTS_BY_YEAR_INDEX_NAME + "\" class=\"a_und\">List of all events by year</a>");
            output.write("<br />");
            output.newLine();
            output.write("<a href=\"" + ALL_EVENTS_BY_COUNTRY_INDEX_NAME + "\" class=\"a_und\">List of all events by country</a>");
            output.write("<br />");
            output.write("<br />");
            output.newLine();
            output.write("<a href=\"" + MONARCHS_INDEX_NAME + "\" class=\"a_und\">Table of monarchs</a>");
            output.write("<br />");
            output.newLine();
            output.write("<a href=\"" + LEADERS_INDEX_NAME + "\" class=\"a_und\">Table of leaders</a>");
            output.newLine();

            output.write("<br /><br />");
            output.newLine();
            output.write("Generated by " + Main.NAME + " for " + Main.getModNameOrVanilla());
            output.newLine();
            output.write("</p></div>");
            output.newLine();
            output.write("</div> <!-- End of main content -->");
            output.newLine();
            output.newLine();
            writePageEnd(output);
            output.write("</body>");
            output.newLine();
            output.write("</html>");
            output.newLine();
        } catch (IOException ex) {
            System.out.println("Error writing the index file");
        }
    }
    
    private static void writeLookupPage(String directory) {
        File lookup = new File(directory + ALL_EVENTS_INDEX_NAME);
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lookup), StandardCharsets.UTF_8.name()))) {
            writeHeader("All Events", output, true, true, false);
            
            output.write("<body>");
            output.newLine();
            
            writePageStart(output);
            output.newLine();
            
            output.write("<div class=\"index\">");
            output.newLine();

            output.write("<div class=\"index_head\"><h2><a id=\"top\" class=\"index_title\">All Events</a></h2></div>");
            output.newLine();

            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            output.newLine();

            output.write("<div class=\"index_body\">");
            output.newLine();
            
            List<Integer> allIds = allEvents.entrySet().stream()
                    .filter(entry -> !entry.getValue().isAIOnly())
                    .map(entry -> entry.getKey())
                    .collect(Collectors.toList());
            Collections.sort(allIds);

            for (Integer id : allIds) {
                Event e = allEvents.get(id);

                output.write(makeLink(id, true, DOC_FOLDER, true));
                output.write("<br />");
                output.newLine();
            }
            output.write("</div> <!-- End of list -->");
            output.newLine();
            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<p>" + allIds.size() + " total events.</p>");
            output.newLine();
            output.write("<p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            output.newLine();
            output.write("</div> <!-- End of main content -->");
            output.newLine();
            output.newLine();
            writePageEnd(output);
            output.write("</body>");
            output.newLine();
            output.write("</html>");
            output.newLine();
        } catch (IOException ex) {
            System.out.println("Error writing event index file");
        }
    }

    private static void writeYearlyLookupPage(String directory) {
        File lookup = new File(directory + ALL_EVENTS_BY_YEAR_INDEX_NAME);
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lookup), StandardCharsets.UTF_8.name()))) {
            writeHeader("All Events", output, true, true, false);

            output.write("<body>");
            output.newLine();

            writePageStart(output);
            output.newLine();

            output.write("<div class=\"index\">");
            output.newLine();

            output.write("<div class=\"index_head\"><h2><a id=\"top\" class=\"index_title\">All Events</a></h2></div>");
            output.newLine();

            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            output.newLine();

            output.write("<div class=\"index_body\">");
            output.newLine();

            List<Event> events = allEvents.values().stream()
                    .filter(e -> !e.isAIOnly())
                    .collect(Collectors.toList());
            Collections.sort(events, Event.SORT_BY_DATE);

            for (Event evt : events) {
                writeEventYear(evt, output);
                output.write(makeLink(evt.getId(), true, DOC_FOLDER, false));
                output.write("<br />");
                output.newLine();
            }
            output.write("</div> <!-- End of list -->");
            output.newLine();
            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<p>" + events.size() + " total events.</p>");
            output.newLine();
            output.write("<p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            output.newLine();
            output.write("</div> <!-- End of main content -->");
            output.newLine();
            output.newLine();
            writePageEnd(output);
            output.write("</body>");
            output.newLine();
            output.write("</html>");
            output.newLine();
        } catch (IOException ex) {
            System.out.println("Error writing event index file");
        }
    }
    
    private static void writeCountryLookupPage(String directory) {
        File lookup = new File(directory + ALL_EVENTS_BY_COUNTRY_INDEX_NAME);
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lookup), StandardCharsets.UTF_8.name()))) {
            writeHeader("All Events", output, true, true, true);
            
            output.write("<body onload=\"toggle('eventlist')\">");
            output.newLine();

            writePageStart(output);
            output.newLine();
            
            // Top index
            output.write("<div class=\"index\" id=\"index\">");
            output.newLine();
            output.write("<div class=\"index_head\"><h2><a id=\"top\" class=\"index_title\">All Events</a></h2></div>");
            output.newLine();
            output.write("<div class=\"index_body\">");
            output.newLine();
            output.write("<a href=\"javascript:toggle('eventlist')\">Toggle table of contents</a>");
            output.write("<br />");
            output.newLine();
            output.write("<div class=\"eventlist\" id=\"eventlist\">");
            output.newLine();

            List<String> allCountryTags = new ArrayList<>(allEvents.values().stream().filter(e -> !e.isAIOnly()).map(e -> e.getTag()).distinct().collect(Collectors.toList()));
            Collections.sort(allCountryTags, (t1, t2) -> {
                return Text.getText(t1).compareTo(Text.getText(t2)); // sort by name, not by tag
            });
            
            for (String tag : allCountryTags) {
                if (tag == null || tag.isEmpty())
                    continue;
                output.write("<b><a href=\"#" + tag + "\">" + Text.getText(tag) + "</a></b>");
                long numEvts = allEvents.values().stream().filter(e -> tag.equals(e.getTag())).filter(e -> !e.isAIOnly()).count();
                output.write(" (" + numEvts + " event" + (numEvts > 1 ? "s" : "") + ")<br />\n");
            }
            
            List<Integer> allEventProvs =  new ArrayList<>(allEvents.values().stream().filter(e -> !e.isAIOnly()).map(e -> e.getProvince()).distinct().collect(Collectors.toList()));
            Collections.sort(allEventProvs, (id1, id2) -> {
                return Text.getText(ProvinceDB.getName(id1)).compareTo(Text.getText(ProvinceDB.getName(id2)));
            });
            
            output.write("<hr />\n");
            
            for (Integer provId : allEventProvs) {
                if (provId <= 0)
                    continue;
                output.write("<b><a href=\"#prov" + provId + "\">" + Text.getText(ProvinceDB.getName(provId)) + "</a></b>");
                long numEvts = allEvents.values().stream().filter(e -> provId == e.getProvince()).filter(e -> !e.isAIOnly()).count();
                output.write(" (" + numEvts + " event" + (numEvts > 1 ? "s" : "") + ")<br />\n");
            }
            
            output.write("<hr />\n");
            output.write("<b><a href=\"#global\">All Countries</a></b>");
            long numEvts = allEvents.values().stream().filter(e -> e.getTag() == null && e.getProvince() < 0).filter(e -> !e.isAIOnly()).count();
            output.write(" (" + numEvts + " event" + (numEvts > 1 ? "s" : "") + ")<br />\n");
            

            output.write("</div> <!-- End of event list -->");
            output.newLine();
            output.write("<br />");
            output.newLine();

            output.write("</div> <!-- End of index body -->");
            output.newLine();

            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            output.newLine();

            output.write("<div class=\"index_body\">");
            output.newLine();
            output.write("<h2>Country Events</h2>");
            output.newLine();

            List<Event> events = new ArrayList<>(allEvents.values());
            Collections.sort(events, Event.SORT_BY_COUNTRY);
            String lastTag = null;
            int lastProv = -1;
            boolean printedAllCountries = false;
            for (Event evt : events) {
                if (evt.isAIOnly())
                    continue;
                
                // print some header stuff if this is the first event for this country or province
                if (evt.getTag() != null && !evt.getTag().equals(lastTag)) {
                    if (evt.getTag() != null)
                        output.write("<h3 id=\"" + evt.getTag() + "\">" + Text.getText(evt.getTag()) + "</h3>\n");
                }
                lastTag = evt.getTag();
                
                if (lastProv == -1 && evt.getProvince() != -1)
                    output.write("<h2>Province Events</h2>\n");
                
                if (lastProv != evt.getProvince()) {
                    if (evt.getProvince() != -1)
                        output.write("<h3 id=\"prov" + evt.getProvince() + "\">" + Text.getText(ProvinceDB.getName(evt.getProvince())) + "</h3>\n");
                }
                lastProv = evt.getProvince();
                
                if (evt.getTag() == null && evt.getProvince() == -1) {
                    if (!printedAllCountries)
                        output.write("<h2 id=\"global\">All Countries</h2>\n");
                    printedAllCountries = true;
                }
                // end of header stuff
                
                writeEventYear(evt, output);
                output.write(makeLink(evt.getId(), true, DOC_FOLDER, true));
                output.write("<br />");
                output.newLine();
            }
            output.write("</div> <!-- End of list -->");
            output.newLine();
            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<p>" + events.size() + " total events.</p>");
            output.newLine();
            output.write("<p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            output.newLine();
            output.write("</div> <!-- End of main content -->");
            output.newLine();
            output.newLine();
            writePageEnd(output);
            output.write("</body>");
            output.newLine();
            output.write("</html>");
            output.newLine();
        } catch (IOException ex) {
            System.out.println("Error writing event index file");
        }
    }
    
    private static void writeMonarchTable(String directory) {
        File lookup = new File(directory + MONARCHS_INDEX_NAME);
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lookup), StandardCharsets.UTF_8.name()))) {
            writeHeader("Monarchs", output, true, true, true);

            output.write("<body>");
            output.newLine();

            writePageStart(output);
            output.newLine();

            output.write("<div class=\"index\">");
            output.newLine();

            output.write("<div class=\"index_head\"><h2><a id=\"top\" class=\"index_title\">All Monarchs</a> | <a href=\"" + LEADERS_INDEX_NAME + "\">All Leaders</a></h2></div>");
            output.newLine();
            
            output.write(MonarchDB.getTableHtml());

            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<br /><p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            
            output.newLine();
            output.write("</div>");
            output.newLine();
            output.write("</div> <!-- End of main content -->");
            output.newLine();
            output.newLine();
            writePageEnd(output);
            output.write("<script type=text/javascript>setupTable();</script>");
            output.write("</body>");
            output.newLine();
            output.write("</html>");
            output.newLine();
        } catch (IOException ex) {
            System.out.println("Error writing monarch index file");
        }
    }
    
    private static void writeLeaderTable(String directory) {
        File lookup = new File(directory + LEADERS_INDEX_NAME);
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lookup), StandardCharsets.UTF_8.name()))) {
            writeHeader("Leaders", output, true, true, true);

            output.write("<body>");
            output.newLine();

            writePageStart(output);
            output.newLine();

            output.write("<div class=\"index\">");
            output.newLine();

            output.write("<div class=\"index_head\"><h2><a href=\"" + MONARCHS_INDEX_NAME + "\">All Monarchs</a> | <a id=\"top\" class=\"index_title\">All Leaders</a></h2></div>");
            output.newLine();
            
            output.write(LeaderDB.getTableHtml());

            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<br /><p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            
            output.newLine();
            output.write("</div>");
            output.newLine();
            output.write("</div> <!-- End of main content -->");
            output.newLine();
            output.newLine();
            writePageEnd(output);
            output.write("<script type=text/javascript>setupTable();</script>");
            output.write("</body>");
            output.newLine();
            output.write("</html>");
            output.newLine();
        } catch (IOException ex) {
            System.out.println("Error writing leader index file");
        }
    }
    
    private static void writeEventFlagTable(String directory) {
        File lookup = new File(directory + EVENT_FLAG_INDEX_NAME);
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lookup), StandardCharsets.UTF_8.name()))) {
            writeHeader("Event Flags", output, true, true, true);

            output.write("<body onload=\"toggle('eventlist')\">");
            output.newLine();

            writePageStart(output);
            output.newLine();

            output.write("<div class=\"index\">");
            output.newLine();

            output.write("<div class=\"index_head\"><h2><a id=\"top\" class=\"index_title\">Event Flags</a></h2></div>");
            output.newLine();
            output.write("<div class=\"index_body\">");
            output.newLine();
            
            output.write("<a href=\"javascript:toggle('eventlist')\">Toggle table of contents</a>");
            output.write("<br />");
            output.newLine();
            output.write("<div class=\"eventlist\" id=\"eventlist\">");
            output.newLine();
            
            output.write(EventFlag.getIndexHtml());
            
            output.write("</div> <!-- End of flag list -->");
            output.newLine();
            output.write("<br />");
            output.newLine();
            output.write("</div> <!-- End of index body -->");
            output.newLine();
            
            output.write(EventFlag.getTableHtml());

            output.write("<div class=\"index_footer\">");
            output.newLine();
            output.write("<br /><p><a href=\"" + INDEX_NAME + "\">Back to Index</a></p>");
            output.newLine();
            output.write("</div>");
            
            output.newLine();
            output.write("</div>");
            output.newLine();
            output.write("</div> <!-- End of main content -->");
            output.newLine();
            output.newLine();
            writePageEnd(output);
            output.write("<script type=text/javascript>setupTable();</script>");
            output.write("</body>");
            output.newLine();
            output.write("</html>");
            output.newLine();
        } catch (IOException ex) {
            System.out.println("Error writing event flags index file");
        }
    }
    
    static String getCoinLink() {
        return "<img src=\"../" + imageDir + "coin.png\" alt=\"gold\" title=\"ducats\" />";
    }
    
    static void setTitle(String newTitle) {
        if (newTitle != null)
            title = newTitle;
    }
    
    static void setCustomHeader(String newText) {
        if (newText != null)
            customHeader = newText;
    }
    
    static void setPageStart(String newText) {
        if (newText != null)
            pageStart = newText;
    }
    
    static void setPageEnd(String newText) {
        if (newText != null)
            pageEnd = newText;
    }
    
    static void setBetweenEvents(String newText) {
        if (newText != null)
            betweenEvents = newText;
    }
    
    static void setImageDir(String dir) {
        if (dir != null)
            imageDir = dir;
    }
    
    static void setCssDir(String dir) {
        if (dir != null)
            cssDir = dir;
    }
    
    static void setJsDir(String dir) {
        if (dir != null)
            jsDir = dir;
    }
    
    private static final String HEADER =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
    
    private static String title = "%s &ndash; For the Glory Event Documentation";
    
    private static String customHeader = "";
    
    private static String pageStart = "";
    
    private static String betweenEvents = "";
    
    private static String pageEnd = "";
    
    private static String imageDir = "";
    
    private static String cssDir = "";
    
    private static String jsDir = "";
    
    private static final String DOC_FOLDER = "eventdoc/";
    
    
    
    static final Comparator<Object> SORT_BY_DATE_DECISIONS_FIRST = new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof Event && o2 instanceof Decision)
                return 1;
            if (o1 instanceof Decision && o2 instanceof Event)
                return -1;
            
            if (o1 instanceof Event && o2 instanceof Event)
                return Event.SORT_BY_DATE.compare((Event)o1, (Event)o2);
            if (o1 instanceof Decision && o2 instanceof Decision)
                return Decision.SORT_BY_ID.compare((Decision)o1, (Decision)o2);
            return 0;
        }
    };
    
}
