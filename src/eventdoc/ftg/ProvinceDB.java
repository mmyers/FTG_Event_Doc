/*
 * ProvinceDB.java
 *
 * Created on Mar 18, 2008, 11:49:59 AM
 */

package eventdoc.ftg;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Michael
 */
public class ProvinceDB {
    
    private static final Map<Integer, Province> allProvs =
            new HashMap<Integer, Province>(2020);

    private ProvinceDB() {
    }
    
    static void init(String filename) {
        filename = Main.resolver.resolveFilename(filename);
        
        System.out.println("Parsing provinces from " + filename);

        GenericObject provinces = EUGFileIO.load(filename);

        for (GenericObject provObj : provinces.getChildren("province")) {
            Province p = new Province(provObj);
            allProvs.put(p.id, p);
        }

        // Old EU2 code
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(new FileReader(filename));
//
//            reader.readLine();
//
//            String line = null;
//
//            while ((line = reader.readLine()) != null) {
//                if (line.charAt(0) == '#' || line.charAt(0) == ';') {
//                    continue;
//                }
//                Province p = new Province(line);
//
//                allProvs.put(p.id, p);
//            }
//        } catch (FileNotFoundException ex) {
//            System.out.println("Couldn't find file");
//        } catch (IOException ex) {
//            System.out.println("Error reading file");
//        } finally {
//            try {
//                reader.close();
//            } catch (IOException ex) {
//                System.out.println("Error closing file");
//            }
//        }
    }
    
    static String format(final int provID) {
        final Province prov = allProvs.get(provID);
        if (prov == null) {
            return "<font color=red>Province #" + provID + "</font>";
        }
        
        final StringBuilder ret = new StringBuilder();
        
        ret.append("<span class=\"province\" title=\"");
        ret.append("Id:&nbsp;").append(prov.id);
//        ret.append(", ").append(prov.religion).append("/").append(prov.culture);
//        ret.append(", base tax: ").append(prov.tax);
//        ret.append(", goods: ").append(prov.goods);
        ret.append(",&nbsp;area:&nbsp;").append(prov.area);
        ret.append(",&nbsp;region:&nbsp;").append(prov.region);
        ret.append(",&nbsp;continent:&nbsp;").append(prov.continent);
        ret.append("\">");
        ret.append(Text.getText(prov.name));
        ret.append("</span>");
        
        return ret.toString();
    }
    
    static String getName(final int provID) {
        final Province prov = allProvs.get(provID);
        if (prov == null) {
            return "(unknown province)";
        } else {
            return prov.name;
        }
    }
    
    private static class Province {
        private int id;
        private String name;
        private String area;
        private String region;
        private String continent;
//        private int tax;
//        private String religion;
//        private String culture;
//        private String goods;
        
        private static final Pattern SEMICOLON = Pattern.compile(";");
        private static final Pattern SPACE = Pattern.compile(" ");

        private Province(final String line) {
            final String[] arr = SEMICOLON.split(line);
            
            id = Integer.parseInt(arr[0]);
            name = SPACE.matcher(arr[1]).replaceAll("&nbsp;");
//            tax = Integer.parseInt(arr[12]);
//            religion = Text.getText(arr[3]);
//            culture = Text.getText("culture_" + arr[4]);
//            goods = Text.getText(arr[19]).replace("&", "&amp;");    // in case it's Bullion & Gems
            area = SPACE.matcher(arr[49]).replaceAll("&nbsp;");
            region = SPACE.matcher(arr[50]).replaceAll("&nbsp;");
            continent = SPACE.matcher(arr[51]).replaceAll("&nbsp;");
        }

        private Province(final GenericObject provObj) {
            id = provObj.getInt("id");
            name = provObj.getString("name");
            area = provObj.getString("area");
            region = provObj.getString("region");
            continent = provObj.getString("continent");
        }
    }
}
