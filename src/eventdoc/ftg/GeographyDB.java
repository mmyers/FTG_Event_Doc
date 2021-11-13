package eventdoc.ftg;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael
 */
public class GeographyDB {

    private static final Map<Integer, Geography> geographyById =
            new HashMap<Integer, Geography>();
    private static final Map<String, Geography> geographyByTag =
            new HashMap<String, Geography>();


    static void init(String filename) {
        filename = Main.resolver.resolveFilename(filename);
        System.out.println("Loading geography from " + filename);
        GenericObject file = EUGFileIO.load(filename);

        for (GenericObject continent : file.getChildren("continent")) {
            Geography c = new Geography();
            c.type = Geography.GeographyType.CONTINENT;
            c.id = continent.getInt("id");
            c.tag = continent.getString("tag");
            c.name = continent.getString("name");

            geographyById.put(c.id, c);
            geographyByTag.put(c.tag.toLowerCase(), c);
        }
        for (GenericObject region : file.getChildren("region")) {
            Geography r = new Geography();
            r.type = Geography.GeographyType.REGION;
            r.id = region.getInt("id");
            r.tag = region.getString("tag");
            r.name = region.getString("name");

            geographyById.put(r.id, r);
            geographyByTag.put(r.tag.toLowerCase(), r);
        }
        for (GenericObject area : file.getChildren("area")) {
            Geography a = new Geography();
            a.type = Geography.GeographyType.AREA;
            a.id = area.getInt("id");
            a.tag = area.getString("tag");
            a.name = area.getString("name");

            geographyById.put(a.id, a);
            geographyByTag.put(a.tag.toLowerCase(), a);
        }
    }

    public static String getName(int id) {
        Geography g = geographyById.get(id);
        if (g == null)
            return "(unknown geography type)";
        else
            return g.name;
    }

    public static String getName(String tag) {
        Geography g = geographyByTag.get(tag.toLowerCase());
        if (g == null)
            return "(unknown geography type)";
        else
            return g.name;
    }

    static class Geography {
        private int id;
        private String tag;
        private String name;
        private GeographyType type;

        static enum GeographyType {
            CONTINENT,
            REGION,
            AREA
        }
    }
}
