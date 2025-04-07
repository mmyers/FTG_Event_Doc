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
import java.util.Map;

/**
 *
 * @author Michael
 */
final class Text {

    // All keys are converted to lower case before putting or getting text, to standardize.
    private static final Map<String, String> englishText = new HashMap<>();
    private static final Map<String, String> frenchText = new HashMap<>();
    //private static final Pattern semicolon = Pattern.compile(";");

    private Text() {
    }
    
    static void loadText() {
        loadText(englishText, "English");
        loadText(frenchText, "French");
        validate();
    }

    private static void loadText(Map<String, String> text, String language) {
        // Forced to hardcode all the text file names because resolver.listFiles()
        // won't look in the parent directory unless there's a mod file.
        // (This is how FTG does it anyway.)
        String folder = "localisation\\" + language + "\\";
        loadTextFile(text, Main.resolver.resolveFilename(folder + "interface.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "countries.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "geography.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "provinces.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "cultures.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "terrains.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "goods.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "technologies.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "religions.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "scenarios.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "rebels.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "events.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "decisions.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "triggers.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "addendum.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "misc.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "1.3.csv"));
        loadTextFile(text, Main.resolver.resolveFilename(folder + "1.31.csv"));

//        for (File f : Main.resolver.listFiles(textDir + "\\" + language)) {
//            if (!f.getName().endsWith(".csv")) {
//                System.out.println("Skipping file " + f.getName() + " in localization directory");
//                continue;
//            }
//            loadTextFile(f);
//        }
    }

    private static void loadTextFile(Map<String, String> text, String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("Not reading text from missing file " + filename);
            return;
        }

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
                } else {
                    String key = line.substring(0, firstSemi).toLowerCase();
                    String value = line.substring(firstSemi + 1, secondSemi);
                    if (text.get(key) != null)
                        System.out.println("Text key " + key + " is already defined");
                    text.put(key, value);
                }
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
    
    private static void validate() {
        for (String key : englishText.keySet()) {
            if (!frenchText.containsKey(key))
                System.out.println("Missing French localization: " + key);
        }
        for (String key : frenchText.keySet()) {
            if (!englishText.containsKey(key))
                System.out.println("Missing English localization: " + key);
        }
    }
    
    static String getText(final String key) {
        if (key == null)
            return "null";
        
        final String ret = englishText.get(key.toLowerCase());
        //if (ret == null && !key.isEmpty() && !key.equals("AI_EVENT"))
        //    System.out.println("No text found for key " + key);
        return (ret != null ? ret : key);
    }
    
    static String getTextCleaned(final String key) {
        String ret = getText(key);
        return ret.replace("\\n", "").replace("§Y", "").replace("§W", "");
    }
}
