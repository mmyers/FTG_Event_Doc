/*
 * Text.java
 *
 * Created on Mar 13, 2008, 9:25:53 PM
 */
package eventdoc.ftg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Michael
 */
final class Text {

    // All keys are converted to lower case before putting or getting text, to
    // standardize.
    private static final java.util.Map<String, String> text =
            new HashMap<String, String>();
    //private static final Pattern semicolon = Pattern.compile(";");

    private Text() {
    }

    static void loadText(String textDir, String language) {
        // Forced to hardcode all the text file names because resolver.listFiles()
        // won't look in the parent directory unless there's a mod file.
        // (This is how FTG does it anyway.)
        String folder = textDir + "\\" + language + "\\";
        loadTextFile(Main.resolver.resolveFilename(folder + "countries.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "cultures.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "events.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "geography.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "goods.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "interface.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "misc.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "provinces.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "religions.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "scenarios.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "technologies.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "terrains.csv"));
        loadTextFile(Main.resolver.resolveFilename(folder + "addendum.csv"));

//        for (File f : Main.resolver.listFiles(textDir + "\\" + language)) {
//            if (!f.getName().endsWith(".csv")) {
//                System.out.println("Skipping file " + f.getName() + " in localization directory");
//                continue;
//            }
//            loadTextFile(f);
//        }
    }

    private static void loadTextFile(String filename) {
        File f = new File(filename);

        System.out.println("Reading text from " + f.getAbsolutePath());

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f), Math.min(1024000, (int) f.length()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == '#') {
                    continue;
                }
                //                    String[] splitLine = semicolon.split(line);
                //                    if (splitLine.length < 2) {
                //                        if (!line.contains(";")) {
                //                            // If it contains ";", then it's probably just a line like ;;;;;;;;;;;;
                //                            // If not, we need to know what it is.
                //                            System.err.println("Malformed line in file " + f.getPath() + ":");
                //                            System.err.println(line);
                //                        }
                //                        continue;
                //                    }
                //                    text.put(splitLine[0].toLowerCase(), splitLine[1]); // English
                int firstSemi = line.indexOf(';');
                int secondSemi = line.indexOf(';', firstSemi + 1);
                if (firstSemi < 0 || secondSemi < 0) {
                    System.err.println("Malformed line in file " + f.getPath() + ":");
                    System.err.println(line);
                }
                text.put(line.substring(0, firstSemi).toLowerCase(), line.substring(firstSemi + 1, secondSemi));
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Couldn't find " + f.getName());
        } catch (IOException ex) {
            System.err.println("Couldn't read from " + f.getName());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    
    static String getText(final String key) {
        if (key == null)
            return "null";
        
        final String ret = text.get(key.toLowerCase());
        return (ret != null ? ret : key);
    }
}
