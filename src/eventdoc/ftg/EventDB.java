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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Michael
 */
class EventDB {

    private static final Map<String, List<Event>> eventFiles = new HashMap<>(50);
    
    private static final Map<Integer, Event> allEvents = new HashMap<>(5000);

    private static final Map<Integer, String> eventsInFiles = new HashMap<>(5000);
    
    private static final Map<Integer, String> names = new HashMap<>(5000);
    private static final Map<Integer, String> descs = new HashMap<>(5000);
    
    private static final String INDEX_NAME = "eventdoc.htm";
    private static final String ALL_EVENTS_INDEX_NAME = "all_events.htm";
    private static final String ALL_EVENTS_BY_YEAR_INDEX_NAME = "all_events_by_year.htm";
    private static final String ALL_EVENTS_BY_COUNTRY_INDEX_NAME = "all_events_by_country.htm";

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
            } else if (var.varname.equalsIgnoreCase("event")) {
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
        
        List <Event> currentFile = new ArrayList<>(20);
        eventFiles.put(file.getName(), currentFile);
        
        List<Event> events = new EventParser(file).parse();
        for (Event event : events) {
            currentFile.add(event);
            allEvents.put(event.getId(), event);
            names.put(event.getId(), event.getName());
            descs.put(event.getId(), event.getDesc());
            eventsInFiles.put(event.getId(), file.getName());
        }
        Collections.sort(currentFile, Event.SORT_BY_DATE);
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
        BufferedWriter output = null;

        File folder = new File(directory + docFolder);
        if (!folder.exists())
            folder.mkdir();

        List<String> files = new ArrayList<String>(eventFiles.keySet());
        Collections.sort(files);

        for (String file : files) {
            System.out.println(file);
            List<Event> events = eventFiles.get(file);
            File page = new File(directory + docFolder + file + ".htm");
            try {
                output = new BufferedWriter(new FileWriter(page));
                writeHeader(file, output, false);
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

                for (Event evt : events) {
                    if (evt.isRandom()) {
                        output.write("Random: ");
                    } else if (triggersOf.get(evt.getId()) != null) {
                    output.write("Triggered (");
                    List<Integer> triggeringEvents = new ArrayList<Integer>(triggersOf.get(evt.getId()).keySet());
                    for (int i = 0; i < triggeringEvents.size(); i++) {
                        Event e = allEvents.get(triggeringEvents.get(i));
                        if (e.getStartDate() != null) {
                            output.write(String.valueOf(e.getStartDate().get(GregorianCalendar.YEAR)));
                            if (e.getEndDate() != null) {
                                output.write("-" + e.getEndDate().get(GregorianCalendar.YEAR));
                            }
                        } else if (e.isRandom()) {
                            output.write("random event");
                        } else if (triggersOf.get(e.getId()) != null) {
                            output.write("triggered event");
                        } else {
                            output.write("unknown event");
                        }
                        if (i < triggeringEvents.size() - 1)
                            output.write(", ");
                    }
                    output.write("): ");
                    } else if (evt.getStartDate() != null) {
                        output.write(String.valueOf(evt.getStartDate().get(GregorianCalendar.YEAR)));
                        if (evt.getEndDate() != null) {
                            output.write("-" + evt.getEndDate().get(GregorianCalendar.YEAR));
                        }
                        output.write(": ");
                    } else {
                        output.write("<span class=\"error\">Unknown: </span>");
                    }
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

                // Events
                for (Event evt : events) {
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
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ex) {
                    System.err.println("Error closing " + page);
                }
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

        System.out.println("Finished creating HTML");
    }
    
    
    static final String makeLink(final int eventID) {
        return makeLink(eventID, true, null, true);
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
     * @param underlined whether the link should be made with class
     * "a_und".
     * @param includeID whether the link text should include the event ID.
     * @return an HTML string representing a link to the event.
     */
    static final String makeLink(int eventID, boolean underlined, String dir, boolean includeID) {
        String filename = null;
        String tag = null;
        int province = -1;

        filename = eventsInFiles.get(eventID);
        
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
        ret.append(Text.getText(names.get(eventID)));
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
    
    static String formatCountry(final String tag) {
        if (tag.equals("-6"))
            return "<span class=\"country\">The Holy Roman Emperor</span>";
        if (tag.equals("-2"))
            return "<span class=\"country\">our overlord</span>";
        return "<span class=\"country\" title=\"" + tag + "\">" + Text.getText(tag) + "</span>";
    }
    
    // Mapping from event ID 1 to
    // {mapping from event ID 2 to string holding all actions of #2 that trigger #1}
    private static final Map<Integer, Map<Integer, String>> triggersOf =
            new HashMap<Integer, Map<Integer, String>>();
    
    /**
     * Returns a list of events that trigger the given event.
     * @param id the event that is triggered.
     * @return all events that can trigger the given event (may be null).
     */
    static final Map<Integer, String> triggersOf(final int id) {
        return triggersOf.get(id);
    }

    private static final Map<Integer, Map<Integer, String>> sleptBy =
            new HashMap<Integer, Map<Integer, String>>();

    static final Map<Integer, String> whatSleeps(int id) {
        return sleptBy.get(id);
    }
    
    private static final void initTriggers() {
        for (Map.Entry<Integer, Event> entry : allEvents.entrySet()) {
            final int eventID = entry.getKey();
            final Map<Character, List<Integer>> eventCanTrigger = entry.getValue().canTrigger();

            for (Map.Entry<Character, List<Integer>> actionCanTrigger : eventCanTrigger.entrySet()) {
                final char action = actionCanTrigger.getKey();
                
                for (Integer id : actionCanTrigger.getValue()) {
                    Map<Integer, String> triggers = triggersOf.get(id);
                    if (triggers == null) {
                        triggers = new HashMap<Integer, String>();
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
                        slept = new HashMap<Integer, String>();
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
    
    private static void writeHeader(String fileTitle, BufferedWriter output, boolean isIndex) throws IOException {

        output.write(header);
        output.newLine();

        output.write("<html>");
        output.newLine();
        output.write("<head>");
        output.newLine();
        output.write("<!-- Generated by " + Main.NAME + " version " +
                Main.VERSION + " on " +
                String.format(Locale.US, "%1$tc", new GregorianCalendar()) + " -->");
        output.newLine();

        output.write("<title>");
//        output.write(fileTitle + " -- FTG Event Documentation");
        output.write(String.format(title, fileTitle));
        output.write("</title>");
        output.newLine();
        
        // TODO: metas
        output.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
        output.newLine();
        output.write("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
        output.newLine();
        output.write("<meta http-equiv=\"Keywords\" content=\"For the Glory, For the Glory game\">");
        output.newLine();
        output.write("<meta http-equiv=\"Description\" content=\"For the Glory" + (Main.moddir == null ? "" : " " + Main.moddir + " mod") + " game event documentation\">");
        output.newLine();

        output.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        if (!isIndex)
            output.write("../");
        output.write(cssDir);
        output.write("eventdoc.css\" title=\"Style\" />");
        output.newLine();
        if (!isIndex)
            output.write("<script src=\"../" + jsDir + "eventdoc.js\" type=\"text/javascript\"></script>");
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
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(index));
            writeHeader("Index", output, true);
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
                output.write("<a href=\"" + docFolder + name + ".htm\"><b>" + name + "</b></a> (" + events + ")<br />");
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
            output.newLine();

            output.write("<br /><br />");
            output.newLine();
            output.write("Generated by " + Main.NAME + " for " + (Main.moddir == null ? "vanilla" : Main.moddir));
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
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                System.out.println("Error closing the index file");
            }
        }
    }
    
    private static void writeLookupPage(String directory) {
        File lookup = new File(directory + ALL_EVENTS_INDEX_NAME);
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(lookup));
            writeHeader("All Events", output, true);
            
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
            
            List<Integer> allIds = new ArrayList<Integer>(allEvents.keySet());
            Collections.sort(allIds);

            for (Integer id : allIds) {
                Event e = allEvents.get(id);
                if ("AI_EVENT".equals(e.getName()))
                    continue;

                output.write(makeLink(id, true, docFolder, true));
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
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                System.out.println("Error closing event index file");
            }
        }
    }

    private static void writeYearlyLookupPage(String directory) {
        File lookup = new File(directory + ALL_EVENTS_BY_YEAR_INDEX_NAME);
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(lookup));
            writeHeader("All Events", output, true);

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

            List<Event> events = new ArrayList<Event>(allEvents.values());
            Collections.sort(events, Event.SORT_BY_DATE);

            for (Event evt : events) {
                if ("AI_EVENT".equals(evt.getName()))
                    continue;

                if (evt.isRandom()) {
                    output.write("Random: ");
                } else if (evt.getStartDate() != null) {
                    output.write(String.valueOf(evt.getStartDate().get(GregorianCalendar.YEAR)));
                    if (evt.getEndDate() != null) {
                        output.write("-" + evt.getEndDate().get(GregorianCalendar.YEAR));
                    }
                    output.write(": ");
                } else if (triggersOf.get(evt.getId()) != null) {
                    output.write("Triggered (");
                    List<Integer> triggeringEvents = new ArrayList<Integer>(triggersOf.get(evt.getId()).keySet());
                    for (int i = 0; i < triggeringEvents.size(); i++) {
                        Event e = allEvents.get(triggeringEvents.get(i));
                        if (e.getStartDate() != null) {
                            output.write(String.valueOf(e.getStartDate().get(GregorianCalendar.YEAR)));
                            if (e.getEndDate() != null) {
                                output.write("-" + e.getEndDate().get(GregorianCalendar.YEAR));
                            }
                        } else if (e.isRandom()) {
                            output.write("random event");
                        } else if (triggersOf.get(e.getId()) != null) {
                            output.write("triggered event");
                        } else {
                            output.write("unknown event");
                        }
                        if (i < triggeringEvents.size() - 1)
                            output.write(", ");
                    }
                    output.write("): ");
                } else {
                    output.write("<span class=\"error\">Unknown: </span>");
                }
                output.write(makeLink(evt.getId(), true, docFolder, false));
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
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                System.out.println("Error closing event index file");
            }
        }
    }
    
    private static void writeCountryLookupPage(String directory) {
        File lookup = new File(directory + ALL_EVENTS_BY_COUNTRY_INDEX_NAME);
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(lookup));
            writeHeader("All Events", output, true);

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
            output.write("<h2>Country Events</h2>");
            output.newLine();

            List<Event> events = new ArrayList<Event>(allEvents.values());
            Collections.sort(events, Event.SORT_BY_COUNTRY);
            String lastTag = null;
            int lastProv = -1;
            boolean printedAllCountries = false;
            for (Event evt : events) {
                if ("AI_EVENT".equals(evt.getName()))
                    continue;
                
                // print some header stuff if this is the first event for this country or province
                if (evt.getTag() != null && !evt.getTag().equals(lastTag)) {
                    if (evt.getTag() != null)
                        output.write("<h3>" + Text.getText(evt.getTag()) + "</h3>\n");
                }
                lastTag = evt.getTag();
                
                if (lastProv == -1 && evt.getProvince() != -1)
                    output.write("<h2>Province Events</h2>\n");
                
                if (lastProv != evt.getProvince()) {
                    if (evt.getProvince() != -1)
                        output.write("<h3>" + Text.getText(ProvinceDB.getName(evt.getProvince())) + "</h3>\n");
                }
                lastProv = evt.getProvince();
                
                if (evt.getTag() == null && evt.getProvince() == -1) {
                    if (!printedAllCountries)
                        output.write("<h2>All Countries</h2>\n");
                    printedAllCountries = true;
                }
                // end of header stuff

                if (evt.isRandom()) {
                    output.write("Random: ");
                } else if (evt.getStartDate() != null) {
                    output.write(String.valueOf(evt.getStartDate().get(GregorianCalendar.YEAR)));
                    if (evt.getEndDate() != null) {
                        output.write("-" + evt.getEndDate().get(GregorianCalendar.YEAR));
                    }
                    output.write(": ");
                } else if (triggersOf.get(evt.getId()) != null) {
                    output.write("Triggered (");
                    List<Integer> triggeringEvents = new ArrayList<Integer>(triggersOf.get(evt.getId()).keySet());
                    for (int i = 0; i < triggeringEvents.size(); i++) {
                        Event e = allEvents.get(triggeringEvents.get(i));
                        if (e.getStartDate() != null) {
                            output.write(String.valueOf(e.getStartDate().get(GregorianCalendar.YEAR)));
                            if (e.getEndDate() != null) {
                                output.write("-" + e.getEndDate().get(GregorianCalendar.YEAR));
                            }
                        } else if (e.isRandom()) {
                            output.write("random event");
                        } else if (triggersOf.get(e.getId()) != null) {
                            output.write("triggered event");
                        } else {
                            output.write("unknown event");
                        }
                        if (i < triggeringEvents.size() - 1)
                            output.write(", ");
                    }
                    output.write("): ");
                } else {
                    output.write("<span class=\"error\">Unknown: </span>");
                }
                output.write(makeLink(evt.getId(), true, docFolder, true));
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
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                System.out.println("Error closing event index file");
            }
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
    
    private static final String header =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
    
    private static String title = "%s &ndash; For the Glory Event Documentation";
    
    private static String customHeader = "";
    
    private static String pageStart = "";
    
    private static String betweenEvents = "";
    
    private static String pageEnd = "";
    
    private static String imageDir = "";
    
    private static String cssDir = "";
    
    private static String jsDir = "";
    
    private static final String docFolder = "eventdoc/";
    
}
