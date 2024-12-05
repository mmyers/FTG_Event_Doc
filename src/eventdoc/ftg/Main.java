package eventdoc.ftg;

import eug.shared.FilenameResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class Main {
    public static final String NAME = "MichaelM's event documenter";
    public static final String VERSION = "1.06 FTG";

    private static final List<String> eventFiles = new ArrayList<>(); // include files
    private static final List<String> extraEventFiles = new ArrayList<>(); // files of events that aren't in any include file
    private static String moddir;
    private static String language;
    private static String baseDir;
    private static String outputDir;
    private static String title;
    private static String modName;
    
    private static boolean customHeader = false;
    private static boolean customTop = false;
    private static boolean customBottom = false;
    private static boolean customDivider = false;
    
    static boolean checkLeaders = true;
    static boolean checkMonarchs = false;
    
    static boolean onlyCheckIDs = false;
    
    static FilenameResolver resolver;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(0);
        }
        handleArgs(args);

        if (baseDir == null) {
            baseDir = ".";
        }
        resolver = new FilenameResolver(baseDir);
        System.out.println("Base directory is " + resolver.getMainDirName());
        resolver.setModFile(false);
        resolver.setModPrefix("Mods/");
        if (moddir != null) {
            resolver.setModName(moddir);
            System.out.println("Mod directory is " + resolver.getModDirName());
        }

        if (eventFiles.isEmpty()) {
            System.err.println("No event file specified.");
            printUsage();
            System.exit(0);
        }
//        if (language == null) {
//            language = "English";
//        }
        
        if (outputDir == null) {
            outputDir = "." + File.separator;
        } else {
            File output = new File(outputDir);
            if (!output.exists() && !output.mkdirs()) {
                System.err.println("Failed to create output directory " + output.getAbsolutePath());
            }
        }
        
        if (customHeader) {
            EventDB.setCustomHeader(readFile("header.htm"));
        }
        if (customTop) {
            EventDB.setPageStart(readFile("pagetop.htm"));
        }
        if (customBottom) {
            EventDB.setPageEnd(readFile("pagebottom.htm"));
        }
        if (customDivider) {
            EventDB.setBetweenEvents(readFile("betweenevents.htm"));
        }

        Text.loadText();
        //Text.setLanguage(language);
        //Text.loadText("localisation", language);
        MonarchDB.init("db/monarchs");
        LeaderDB.init("db/leaders");
        LeaderDB.checkForMonarchOverlap();
        LeaderDB.dumpLeaderMonarchIdRanges();
        ProvinceDB.init("db/map/provinces.txt");
        GeographyDB.init("db/map/geography.txt");
        
        System.out.println("Loading events using a base of " + resolver.getModDirName());
        for (String eventFile : eventFiles) {
            EventDB.loadEvents(resolver.resolveFilename(eventFile), resolver.getModDirName());
        }
        for (String extra : extraEventFiles) {
            EventDB.loadEventsFromFile(extra, resolver.getModDirName());
        }

        if (title != null) {
            EventDB.setTitle(title);
        }
        
        
        // Stop here if we're only running checks
        if (onlyCheckIDs)
            return;
        
        
        EventDB.generateHTML(outputDir);

        if (! new File(".").equals(new File(outputDir))) {
            try {
                System.out.println();
                System.out.println("Copying extra files to destination...");
                String[] files = {
                    "coin.png",
//                    "corner-1.png", "corner-2.png", "corner-3.png", "corner-4.png",
//                    "right.png",
                    "eventdoc.js",
                };
                for (String filename : files) {
                    System.out.println(filename);
                    File original = new File("./assets/" + filename);
                    File output = new File(outputDir + filename);
                    if (original.equals(output)) {
                        System.out.println("destination was the same as original???");
                        continue;
                    }
                    copy(original, output);
                }
                System.out.println("Done copying.");
            } catch (IOException ex) {
                System.out.println("Couldn't copy files. Please do so manually.");
            }
        }
        
        System.out.println();
        System.out.println("Finished.");
        System.out.println();
        System.out.println("If you have a CSS file you would like to use, it should now be placed in the");
        System.out.println("CSS directory (by default, just the output directory), if it is not already present.");
        System.out.println("It should be called \"eventdoc.css\".");
    }
    
    static String getModName() {
        if (moddir != null) {
            if (modName != null)
                return " " + modName + " mod";
            return " " + moddir + " mod";
        }
        return "";
    }
    
    static String getModNameOrVanilla() {
        if (moddir != null) {
            if (modName != null)
                return modName;
            return moddir;
        }
        return "vanilla";
    }

    // The following method is copied from The Java Developer's Almanac 1.4
    // (http://exampledepot.com/egs/java.io/CopyFile.html)
    // Copies src file to dst file.
    // If the dst file does not exist, it is created
    private static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    private static void handleArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (equals(arg, "-h", "--help")) {
                printUsage();
            } else if (equals(arg, "-f", "--eventfile")) {
                String eventFileArg = stripQuotes(args[++i]);
                if (eventFileArg.startsWith("/") || eventFileArg.startsWith("\\")) {
                    eventFileArg = eventFileArg.substring(1);
                }
                
                eventFiles.add(eventFileArg);
            } else if (equals(arg, "-e", "--extra-events")) {
                String eventFileArg = stripQuotes(args[++i]);
                if (eventFileArg.startsWith("/") || eventFileArg.startsWith("\\")) {
                    eventFileArg = eventFileArg.substring(1);
                }
                
                extraEventFiles.add(eventFileArg);
            } else if (equals(arg, "-x", "--text")) {
                language = stripQuotes(args[++i]);
                if (language.startsWith("/") || language.startsWith("\\")) {
                    language = language.substring(1);
                }
            } else if (equals(arg, "-b", "--basedir")) {
                baseDir = stripQuotes(args[++i]);
                if (baseDir.endsWith("/") || baseDir.endsWith("\\")) {
                    baseDir = baseDir.substring(0, baseDir.length() - 1);
                }
            } else if (equals(arg, "-m", "--moddir")) {
                moddir = stripQuotes(args[++i]);
                if (moddir.contains("/") || moddir.contains("\\")) {
                    moddir = new File(moddir).getName();
                }
            } else if (equals(arg, "-o", "--output")) {
                outputDir = stripQuotes(args[++i]);
                if (!(outputDir.endsWith("/") || outputDir.endsWith("\\"))) {
                    outputDir += File.separator;
                }
            } else if (equals(arg, "-t", "--title")) {
                title = stripQuotes(args[++i]);
                System.out.println("Custom title: " + title);
            } else if (equals(arg, "-n", "--mod-name")) {
                modName = stripQuotes(args[++i]);
                System.out.println("Mod name override: " + modName);
            } else if (equals(arg, "--custom-header")) {
                customHeader = true;
            } else if (equals(arg, "--custom-page-top")) {
                customTop = true;
            } else if (equals(arg, "--custom-page-bottom")) {
                customBottom = true;
            } else if (equals(arg, "--custom-divider")) {
                customDivider = true;
            } else if (equals(arg, "--image-dir")) {
                String dir = stripQuotes(args[++i]);
                dir = dir.replace('\\', '/');
                if (dir.startsWith("/"))
                    dir = dir.substring(1);
                if (!dir.endsWith("/"))
                    dir += "/";
                EventDB.setImageDir(dir);
            } else if (equals(arg, "--css-dir")) {
                String dir = stripQuotes(args[++i]);
                dir = dir.replace('\\', '/');
                if (dir.startsWith("/"))
                    dir = dir.substring(1);
                if (!dir.endsWith("/"))
                    dir += "/";
                EventDB.setCssDir(dir);
            } else if (equals(arg, "--js-dir")) {
                String dir = stripQuotes(args[++i]);
                dir = dir.replace('\\', '/');
                if (dir.startsWith("/"))
                    dir = dir.substring(1);
                if (!dir.endsWith("/"))
                    dir += "/";
                EventDB.setJsDir(dir);
            } else if (equals(arg, "--no-leader-check")) {
                checkLeaders = false;
            } else if (equals(arg, "--no-monarch-check")) {
                checkMonarchs = false;
            } else if (equals(arg, "--only-check-ids")) {
                onlyCheckIDs = true;
            } else {
                System.err.println("Unknown argument: " + arg);
                printUsage();
            }
        }

    }

    private static void printUsage() {
        System.out.println("Usage: java <prog name> <args>");
        System.out.println("where <args> consists of one or more of the following:");
        System.out.println("    -f, --eventfile <filename>");
        System.out.println("        Specifies that the list of events is to be read from the specified file.");
        System.out.println("        This file should be in the standard events.txt form (event = \"xxx\").");
        System.out.println("        The filename is relative to the base directory, so \"Db\\events.txt\" will");
        System.out.println("        usually work.");
        System.out.println("        Can be used multiple times.");
        System.out.println("    -e, --extra-events <filename>");
        System.out.println("        Specifies that events (not a list of event includes) should be read");
        System.out.println("        from this file.");
        System.out.println("        Can be used multiple times.");
        System.out.println("    -x, --text <filename>");
        System.out.println("        Specifies the language in the localization folder to use.");
        System.out.println("        The default is English.");
        System.out.println("    -b, --basedir <directory>");
        System.out.println("        Specifies that the given directory is the base directory of the FTG");
        System.out.println("        installation.");
        System.out.println("        If unspecified, the program will assume it is in the FTG directory.");
        System.out.println("    -m, --moddir <name>");
        System.out.println("        Specifies that the given name is the name of a mod in the mod directory");
        System.out.println("        and should be used when possible.");
        System.out.println("    -o, --output <directory>");
        System.out.println("        Specifies that output should be placed into the given directory.");
        System.out.println("        A subdirectory named \"eventdoc\" will be created to hold the generated");
        System.out.println("        files other than the index.");
        System.out.println("        The default value is the current directory.");
        System.out.println("    -t, --title <format string>");
        System.out.println("        Specifies to use the given title for the generated files. If the string");
        System.out.println("        contains %s, this is where the page name is placed.");
        System.out.println("        The default is \"%s &ndash; FTG Event Documentation\".");
        System.out.println("        Note that the % sign is reserved in batch files; %%s is necessary instead.");
        System.out.println("    -n, --mod-name <name>");
        System.out.println("        When used in conjunction with -m, specifies that the given name is the readable");
        System.out.println("        name of the mod and should be used in page titles instead of the mod directory.");
        System.out.println("    --custom-header");
        System.out.println("        Specifies that custom HTML from the file 'header.htm' will be inserted into");
        System.out.println("        the end of the header of the generated files.");
        System.out.println("        Relative links should not be used, because not all generated pages are in the");
        System.out.println("        same directory.");
        System.out.println("        Note that this is inserted verbatim, so it should not include <html> or <head>.");
        System.out.println("    --custom-page-top");
        System.out.println("        Specifies that custom HTML from the file 'pagetop.htm' will be inserted into");
        System.out.println("        the generated files (including the index) at the start of the body.");
        System.out.println("        Note that this is inserted verbatim, so it should not include <html></html>.");
        System.out.println("    --custom-page-bottom");
        System.out.println("        Specifies that custom HTML from the file 'pagebottom.htm' will be inserted into");
        System.out.println("        the generated files (including the index) at the end of the body.");
        System.out.println("        Note that this is inserted verbatim, so it should not include <html></html>.");
        System.out.println("    --custom-divider");
        System.out.println("        Specifies that custom HTML from the file 'betweenevents.htm' will be inserted");
        System.out.println("        into the generated files between events.");
        System.out.println("        Note that this is inserted verbatim, so it should not include <html></html>.");
        System.out.println("        Note also that this might cause layout problems on some browsers.");
        System.out.println("    --image-dir <directory>");
        System.out.println("        Specifies that any images will be placed in the given directory (relative to the base).");
        System.out.println("        The default is to put them in the base directory.");
        System.out.println("    --css-dir <directory>");
        System.out.println("        Specifies that the generated files will expect eventdoc.css in the given directory");
        System.out.println("        (relative to the base) rather than in the base directory.");
        System.out.println("    --js-dir <directory>");
        System.out.println("        Specifies that the generated files will expect eventdoc.js in the given directory");
        System.out.println("        (relative to the base) rather than in the base directory.");
        System.out.println("    --no-leader-check");
        System.out.println("        Indicates not to perform any checks of leader commands.");
        System.out.println("    --no-monarch-check");
        System.out.println("        Indicates not to perform any checks of monarch commands.");
        System.out.println("    --only-check-ids");
        System.out.println("        Don't generate any HTML files - only load the events, leaders, and monarchs, and check for ID errors.");
        System.out.println("    -h, --help");
        System.out.println("        Prints this help.");
    }

    private static boolean equals(String arg, String shortArg, String longArg) {
        return (arg.equals(shortArg) || arg.equalsIgnoreCase(longArg));
    }
    
    private static boolean equals(String arg, String longArg) {
        return arg.equalsIgnoreCase(longArg);
    }

    private static String stripQuotes(String str) {
        if (str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            return str.substring(1, str.length() - 1);
        } else if (str.charAt(0) == '"') {
            return str.substring(1);
        } else if (str.charAt(str.length() - 1) == '"') {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    }
    
    private static String readFile(String filename) {
        final File file = new File(filename);
        final char[] data = new char[(int)file.length()];
        try (final FileReader reader = new FileReader(file)) {
            if (reader.read(data) != data.length)
                System.err.println("???");
            return String.valueOf(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Main() {
    }
}
