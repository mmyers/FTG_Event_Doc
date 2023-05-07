/*
 * Trigger.java
 *
 * Created on Mar 15, 2008, 4:54:48 PM
 */

package eventdoc.ftg;

import eug.parser.EUGScanner;
import eug.parser.TokenType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public abstract class Trigger {

    static Trigger parseTrigger(EUGScanner scanner) {
        return new MainTrigger(scanner);
    }

    protected static void warn(String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }
    
    abstract void generateHTML(BufferedWriter out) throws IOException;
    
    String getListStartString() {
        return "<li>";
    }
}


abstract class CombinedTrigger extends Trigger {

    protected List<Trigger> triggers;

    protected CombinedTrigger() {
        // any class using this constructor must be sure to call init at some point
    }
    protected CombinedTrigger(EUGScanner scanner) {
        init(scanner, true);
    }
    protected void init(EUGScanner scanner, boolean needsBrace) {
        triggers = new ArrayList<Trigger>();

        if (needsBrace) {
            if (scanner.nextToken() != TokenType.LBRACE) {
                warn("Missing '{' in trigger", scanner.getLine(), scanner.getColumn());
            }
        }

        parseLoop:
        while (true) {
            switch (scanner.nextToken()) {
                case IDENT:
                    String ident = scanner.lastStr().toLowerCase();
                    if (ident.equals("and")) {
                        triggers.add(new AndTrigger(scanner));
                    } else if (ident.equals("or")) {
                        triggers.add(new OrTrigger(scanner));
                    } else if (ident.equals("not")) {
                        triggers.add(new NotTrigger(scanner));
                    } else if (ident.equals("religion")) {
                        triggers.add(new ReligionTrigger(scanner));
                    } else if (ident.equals("leader")) {
                        triggers.add(new LeaderTrigger(scanner));
                    } else if (ident.equals("monarch")) {
                        triggers.add(new MonarchTrigger(scanner));
                    } else if (ident.equals("event")) {
                        triggers.add(new EventTrigger(scanner));
                    } else if (ident.equals("owned")) {
                        triggers.add(new OwnedTrigger(scanner));
                    } else if (ident.equals("control")) {
                        triggers.add(new ControlTrigger(scanner));
                    } else if (ident.equals("core")) {
                        triggers.add(new CoreTrigger(scanner));
                    } else if (ident.equals("continent")) {
                        triggers.add(new ContinentTrigger(scanner));
                    } else if (ident.equals("exists")) {
                        triggers.add(new ExistTrigger(scanner));
                    } else if (ident.equals("alliance")) {
                        triggers.add(new AllianceTrigger(scanner));
                    } else if (ident.equals("war")) {
                        triggers.add(new WarTrigger(scanner));
                    } else if (ident.equals("dynastic")) {
                        triggers.add(new DynasticTrigger(scanner));
                    } else if (ident.equals("vassal")) {
                        scanner.nextToken();
                        scanner.pushBack();
                        if (scanner.lastToken() == TokenType.LBRACE)
                            triggers.add(new VassalTrigger(scanner));
                        else
                            triggers.add(new IsOurVassalTrigger(scanner));
                    } else if (ident.equals("domestic")) {
                        triggers.add(new DomesticTrigger(scanner));
                    } else if (ident.equals("stability")) {
                        triggers.add(new StabilityTrigger(scanner));
                    } else if (ident.equals("land")) {
                        triggers.add(new LandTrigger(scanner));
                    } else if (ident.equals("naval")) {
                        triggers.add(new NavalTrigger(scanner));
                    } else if (ident.equals("infra")) {
                        triggers.add(new InfraTrigger(scanner));
                    } else if (ident.equals("trade")) {
                        triggers.add(new TradeTrigger(scanner));
                    } else if (ident.equals("discovered")) {
                        triggers.add(new DiscoveredTrigger(scanner));
                    } else if (ident.equals("cot")) {
                        triggers.add(new COTTrigger(scanner));
                    } else if (ident.equals("atwar")) {
                        triggers.add(new AtWarTrigger(scanner));
                    } else if (ident.equals("year")) {
                        triggers.add(new YearTrigger(scanner));
                    } else if (ident.equals("ai")) {
                        triggers.add(new AITrigger(scanner));
                    } else if (ident.equals("flag")) {
                        triggers.add(new FlagTrigger(scanner));
                    } else if (ident.equals("emperor")) {
                        scanner.nextToken();
                        scanner.pushBack();
                        if (scanner.lastToken() == TokenType.LBRACE)
                            triggers.add(new OtherCountryTrigger(scanner, "the Holy Roman Emperor"));
                        else
                            triggers.add(new EmperorTrigger(scanner));
                    } else if (ident.equals("elector")) {
                        triggers.add(new ElectorTrigger(scanner));
                    } else if (ident.equals("countrysize")) {
                        triggers.add(new CountrySizeTrigger(scanner));
                    } else if (ident.equals("badboy")) {
                        triggers.add(new BadboyTrigger(scanner));
                    } else if (ident.equals("relation")) {
                        triggers.add(new RelationTrigger(scanner));
                    } else if (ident.equals("provincereligion")) {
                        triggers.add(new ProvinceReligionTrigger(scanner));
                    } else if (ident.equals("provinceculture")) {
                        triggers.add(new ProvinceCultureTrigger(scanner));
                    } else if (ident.equals("neighbour")) {
                        triggers.add(new NeighbourTrigger(scanner));
                    } else if (ident.equals("region")) { /* New FTG triggers below here */
                        triggers.add(new RegionTrigger(scanner));
                    } else if (ident.equals("area")) {
                        triggers.add(new AreaTrigger(scanner));
                    } else if (ident.equals("random")) {
                        triggers.add(new RandomTrigger(scanner));
                    } else if (ident.equals("access")) {
                        triggers.add(new AccessTrigger(scanner));
                    } else if (ident.equals("tradingpost")) {
                        triggers.add(new TradingPostTrigger(scanner));
                    } else if (ident.equals("colony")) {
                        triggers.add(new ColonyTrigger(scanner));
                    } else if (ident.equals("colonialcity")) {
                        triggers.add(new ColonialCityTrigger(scanner));
                    } else if (ident.equals("city")) {
                        triggers.add(new CityTrigger(scanner));
                    } else if (ident.equals("isvassal")) {
                        triggers.add(new IsVassalTrigger(scanner));
                    } else if (ident.equals("isoverlord")) {
                        triggers.add(new IsOverlordTrigger(scanner));
                    } else if (ident.equals("controlchange")) {
                        triggers.add(new ControlChangeTrigger(scanner));
                    } else if (ident.equals("ownerchange")) {
                        triggers.add(new OwnerChangeTrigger(scanner));
                    } else if (ident.equals("someof")) {
                        triggers.add(new SomeOfTrigger(scanner));
                    } else if (ident.equals("inflation")) {
                        triggers.add(new InflationTrigger(scanner));
                    } else if (ident.equals("treasury")) {
                        triggers.add(new TreasuryTrigger(scanner));
                    } else if (ident.equals("tech")) {
                        triggers.add(new TechGroupTrigger(scanner));
                    } else if (ident.equals("tag")) {
                        triggers.add(new TagTrigger(scanner));
                    } else if (ident.equals("capital")) {
                        triggers.add(new CapitalTrigger(scanner));
                    } else if (ident.equals("provincepopulation")) {
                        triggers.add(new ProvincePopTrigger(scanner));
                    } else if (ident.equals("fortresslevel")) {
                        triggers.add(new FortressLevelTrigger(scanner));
                    } else if (ident.equals("core_national")) {
                        triggers.add(new CoreNationalTrigger(scanner));
                    } else if (ident.equals("core_claim")) {
                        triggers.add(new CoreClaimTrigger(scanner));
                    } else if (ident.equals("core_casusbelli")) {
                        triggers.add(new CoreCasusBelliTrigger(scanner));
                    } else if (ident.equals("cityculture")) {
                        triggers.add(new CityCultureTrigger(scanner));
                    } else if (ident.equals("truce")) {
                        triggers.add(new TruceTrigger(scanner));
                    } else if (ident.equals("union")) {
                        scanner.nextToken();
                        scanner.pushBack();
                        if (scanner.lastToken() == TokenType.LBRACE)
                            triggers.add(new UnionTrigger(scanner));
                        else
                            triggers.add(new IsUnionWithUsTrigger(scanner));
                    } else if (ident.equals("culture")) {
                        triggers.add(new CultureTrigger(scanner));
                    } else if (ident.equals("hre")) {
                        triggers.add(new HreTrigger(scanner));
                    } else if (ident.equals("diplomats")) {
                        triggers.add(new OfficialsTrigger(scanner, ident));
                    } else if (ident.equals("merchants")) {
                        triggers.add(new OfficialsTrigger(scanner, ident));
                    } else if (ident.equals("colonists")) {
                        triggers.add(new OfficialsTrigger(scanner, ident));
                    } else if (ident.equals("missionaries")) {
                        triggers.add(new OfficialsTrigger(scanner, ident));
                    } else if (ident.equals("overlord")) {
                        scanner.nextToken();
                        scanner.pushBack();
                        if (scanner.lastToken() == TokenType.LBRACE)
                            triggers.add(new OverlordTrigger(scanner));
                        else
                            triggers.add(new IsOurOverlordTrigger(scanner));
                    } else if (ident.equals("num_of_ports")) {
                        triggers.add(new NumOfPortsTrigger(scanner));
                    } else if (ident.equals("num_of_colonies")) {
                        triggers.add(new NumOfColoniesTrigger(scanner));
                    } else if (ident.equals("num_of_tps")) {
                        triggers.add(new NumOfTradingPostsTrigger(scanner));
                    } else if (ident.equals("num_of_cots")) {
                        triggers.add(new NumOfCotsTrigger(scanner));
                    } else if (ident.equals("religion_group")) {
                        triggers.add(new ReligionGroupTrigger(scanner));
                    } else if (ident.equals("religion_subgroup")) {
                        triggers.add(new ReligionSubgroupTrigger(scanner));
                    } else if (ident.length() == 3) {
                        // assume it's a country trigger
                        triggers.add(new OtherCountryTrigger(scanner));
                    } else {
                        warn("Unknown trigger type: " + scanner.lastStr(),
                                scanner.getLine(), scanner.getColumn());
                    }
                    break;
                case RBRACE:
                    break parseLoop;
                default:
                    warn("Unexpected token in trigger: " + scanner.lastStr(),
                            scanner.getLine(), scanner.getColumn());
                }
        }
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("<ul class=\"trigger\">");
        out.newLine();
        for (Trigger t : triggers) {
            out.write(t.getListStartString());
            out.newLine();
            t.generateHTML(out);
            out.newLine();
            out.write("</li>");
            out.newLine();
        }
        out.write("</ul>");
        out.newLine();
    }
    
    @Override
    String getListStartString() {
        return "<li class=\"combo\">";
    }
}


class MainTrigger extends CombinedTrigger {
    
    MainTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (triggers.size() == 1 && triggers.get(0) instanceof AndTrigger)
            System.out.println(Event.currentId + ": Style issue: Trigger contains only a single AND-element");
        super.generateHTML(out);
    }
    
}
class AndTrigger extends CombinedTrigger {

    AndTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (triggers.size() == 1)
            System.out.println(Event.currentId + ": Style issue: AND-block in trigger with only one element");
        out.write("All of the following must occur:");
        out.newLine();
        super.generateHTML(out);
    }
}

class OrTrigger extends CombinedTrigger {

    OrTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (triggers.size() == 1)
            System.out.println(Event.currentId + ": Style issue: OR-block in trigger with only one element");
        out.write("At least one of the following must occur:");
        out.newLine();
        super.generateHTML(out);
    }
}

class NotTrigger extends CombinedTrigger {

    NotTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (triggers.size() == 1 && triggers.get(0) instanceof OrTrigger)
            System.out.println(Event.currentId + ": Style issue: NOT-block with only an OR-block subelement (redundant)");
        if (triggers.size() == 1)
            out.write("The following must not occur:");
        else
            out.write("None of the following must occur:");
        out.newLine();
        super.generateHTML(out);
    }
}

/** FTG */
class OtherCountryTrigger extends CombinedTrigger {
    private String tag;
    OtherCountryTrigger(EUGScanner scanner) {
        super(scanner);
        tag = scanner.lastStr();
    }

    OtherCountryTrigger(EUGScanner scanner, String country) {
        super(scanner);
        tag = country;
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("All of the following must be true for " + EventDB.formatCountry(tag) + ":");
        out.newLine();
        super.generateHTML(out);
    }
}

/** FTG */
class SomeOfTrigger extends CombinedTrigger {
    private int number;
    SomeOfTrigger(EUGScanner s) {
        if (s.nextToken() != TokenType.LBRACE) {
            warn("Missing '{' in trigger", s.getLine(), s.getColumn());
        }
        
        if (s.nextToken() != TokenType.IDENT) {
            warn("someof trigger must begin with \"number = x\"", s.getLine(), s.getColumn());
        }
        if (s.lastStr().equals("number")) {
            s.nextToken();
            number = Integer.parseInt(s.lastStr());
        } else {
            warn("someof trigger must begin with \"number = x\"; found \"" + s.lastStr() + "\"", s.getLine(), s.getColumn());
            s.nextToken();
        }
        init(s, false);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("At least " + number + " of the following must be true:");
        out.newLine();
        super.generateHTML(out);
    }
}

abstract class StringTrigger extends Trigger {

    protected String value;

    protected StringTrigger(EUGScanner scanner) {
        TokenType next = scanner.nextToken();

        if (next != TokenType.ULSTRING && next != TokenType.DLSTRING) {
            warn("Unexpected value type: " + next + " (expected string)", scanner.getLine(), scanner.getColumn());
        }

        value = scanner.lastStr();
    }
}


class ReligionTrigger extends StringTrigger {

    ReligionTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("State religion is " + value);
    }
}

class FlagTrigger extends StringTrigger {

    FlagTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (!value.contains(" "))
            out.write("Flag " + value + " is set");
        else
            out.write("Flag \"" + value + "\" is set");
    }
}
abstract class IntTrigger extends Trigger {

    protected int value;

    protected IntTrigger(EUGScanner scanner) {
        TokenType next = scanner.nextToken();

        if (next != TokenType.ULSTRING) {
            warn("Unexpected value type: " + next + " (expected integer)", scanner.getLine(), scanner.getColumn());
        }

        value = Integer.parseInt(scanner.lastStr());
    }
}

abstract class FloatTrigger extends Trigger {

    protected double value;

    protected FloatTrigger(EUGScanner scanner) {
        TokenType next = scanner.nextToken();

        if (next != TokenType.ULSTRING) {
            warn("Unexpected value type: " + next + " (expected float)", scanner.getLine(), scanner.getColumn());
        }

        value = Double.parseDouble(scanner.lastStr());
    }
}


class LeaderTrigger extends IntTrigger {

    LeaderTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(LeaderDB.format(value) + " is active");
    }
}

class MonarchTrigger extends IntTrigger {

    MonarchTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Monarch " + MonarchDB.format(value) + " is active");
    }
}

class EventTrigger extends IntTrigger {

    EventTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Event " + EventDB.makeLink(value) + " has already occurred");
    }
}

class CountrySizeTrigger extends IntTrigger {

    CountrySizeTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Country has at least " + value + " non-colonial province" + (value == 1 ? "" : "s"));
    }
}

class BadboyTrigger extends IntTrigger {

    BadboyTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Badboy is at " + value + " or higher");
    }
}

abstract class ProvinceTrigger extends Trigger {

    protected int province;
    protected String data;

    protected ProvinceTrigger(EUGScanner scanner) {
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Expected '{'", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"province =\"", scanner.getLine(), scanner.getColumn());
        }

        if (!scanner.lastStr().equalsIgnoreCase("province")) {
            warn("Expected \"province =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected province ID", scanner.getLine(), scanner.getColumn());
        }

        province = Integer.parseInt(scanner.lastStr());

        TokenType tok = scanner.nextToken();
        
        if (tok != TokenType.IDENT) {
            warn("Expected \"data =\"", scanner.getLine(), scanner.getColumn());
            if (tok == TokenType.RBRACE) {
                data = "-1";
                return;
            }
        }

        if (!scanner.lastStr().equalsIgnoreCase("data")) {
            warn("Expected \"data =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected data", scanner.getLine(), scanner.getColumn());
        }

        data = scanner.lastStr();

        if (scanner.nextToken() != TokenType.RBRACE) {
            warn("Expected '}'", scanner.getLine(), scanner.getColumn());
        }
    }
}

class OwnedTrigger extends ProvinceTrigger {

    OwnedTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (data.equals("-1"))
            out.write("Own " + ProvinceDB.format(province));
        else
            out.write(EventDB.formatCountry(data) + " owns " + ProvinceDB.format(province));
    }
}


class ControlTrigger extends ProvinceTrigger {

    ControlTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (data.equals("-1"))
            out.write("Control " + ProvinceDB.format(province));
        else
            out.write(EventDB.formatCountry(data) + " controls " + ProvinceDB.format(province));
    }
}

class CoreTrigger extends ProvinceTrigger {

    CoreTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (data.equals("-1"))
            out.write(ProvinceDB.format(province) + " is a national (core) province");
        else
            out.write(ProvinceDB.format(province) + " is a national (core) province of " + EventDB.formatCountry(data));
    }
}

class ProvinceReligionTrigger extends ProvinceTrigger {

    ProvinceReligionTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(ProvinceDB.format(province) + " has religion " + Text.getText(data));
    }
}

class ProvinceCultureTrigger extends ProvinceTrigger {

    ProvinceCultureTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(ProvinceDB.format(province) + " has culture " + Text.getText("culture_" + data));
    }
}

class ContinentTrigger extends StringTrigger {

    ContinentTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Capital is on continent " + Text.getText(GeographyDB.getName(value)));
    }
}

/** FTG */
class RegionTrigger extends StringTrigger {

    RegionTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Capital is in the region " + Text.getText(GeographyDB.getName(value)));
    }
}

/** FTG */
class AreaTrigger extends StringTrigger {

    AreaTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Capital is in the area " + Text.getText(GeographyDB.getName(value)));
    }
}

class ExistTrigger extends StringTrigger {

    ExistTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(value) + " exists");
    }
}

class NeighbourTrigger extends StringTrigger {

    NeighbourTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(value) + " is a neighbor");
    }
}
abstract class CountryTrigger extends Trigger {

    protected String ctag1;
    protected String ctag2;

    protected CountryTrigger(EUGScanner scanner) {
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Expected '{'", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"country =\"", scanner.getLine(), scanner.getColumn());
        }

        if (!scanner.lastStr().equalsIgnoreCase("country")) {
            warn("Expected \"country =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected country tag", scanner.getLine(), scanner.getColumn());
        }

        ctag1 = scanner.lastStr();

        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"country =\"", scanner.getLine(), scanner.getColumn());
        }

        if (!scanner.lastStr().equalsIgnoreCase("country")) {
            warn("Expected \"country =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected country tag", scanner.getLine(), scanner.getColumn());
        }

        ctag2 = scanner.lastStr();

        if (scanner.nextToken() != TokenType.RBRACE) {
            warn("Expected '}'", scanner.getLine(), scanner.getColumn());
        }
    }
}


class AllianceTrigger extends CountryTrigger {

    AllianceTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(ctag1) + " and " + EventDB.formatCountry(ctag2) + " are allied");
    }
}

class WarTrigger extends CountryTrigger {

    WarTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(ctag1) + " and " + EventDB.formatCountry(ctag2) + " are at war");
    }
}

class DynasticTrigger extends CountryTrigger {

    DynasticTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(ctag1) + " and " + EventDB.formatCountry(ctag2) + " have a royal marriage");
    }
}

class VassalTrigger extends CountryTrigger {

    VassalTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(ctag2) + " is a vassal of " + EventDB.formatCountry(ctag1));
    }
}

class IsOurVassalTrigger extends StringTrigger {

    public IsOurVassalTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Is the overlord of " + EventDB.formatCountry(value));
    }
}

class RelationTrigger extends Trigger {

    protected String tag;
    protected int relation;

    protected RelationTrigger(EUGScanner scanner) {
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Expected '{'", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"country =\"", scanner.getLine(), scanner.getColumn());
        }

        if (!scanner.lastStr().equalsIgnoreCase("country")) {
            warn("Expected \"country =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected country tag", scanner.getLine(), scanner.getColumn());
        }

        tag = scanner.lastStr();

        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"data =\"", scanner.getLine(), scanner.getColumn());
        }

        if (!scanner.lastStr().equalsIgnoreCase("data")) {
            warn("Expected \"data =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected relation value", scanner.getLine(), scanner.getColumn());
        }

        relation = Integer.parseInt(scanner.lastStr());

        if (scanner.nextToken() != TokenType.RBRACE) {
            warn("Expected '}'", scanner.getLine(), scanner.getColumn());
        }
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Relations with " + EventDB.formatCountry(tag) + " are at " + relation + " or higher");
    }
}

class DomesticTrigger extends Trigger {

    private String type;
    private int value;

    DomesticTrigger(EUGScanner scanner) {
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Expected '{'", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"type =\"", scanner.getLine(), scanner.getColumn());
        }

        if (!scanner.lastStr().equalsIgnoreCase("type")) {
            warn("Expected \"type =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected domestic policy type", scanner.getLine(), scanner.getColumn());
        }

        type = scanner.lastStr();

        if (scanner.nextToken() != TokenType.IDENT) {
            warn("Expected \"value =\"", scanner.getLine(), scanner.getColumn());
        }

        if (!scanner.lastStr().equalsIgnoreCase("value")) {
            warn("Expected \"value =\"", scanner.getLine(), scanner.getColumn());
        }

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected int from 0 to 10", scanner.getLine(), scanner.getColumn());
        }

        value = Integer.parseInt(scanner.lastStr());

        if (scanner.nextToken() != TokenType.RBRACE) {
            warn("Expected '}'", scanner.getLine(), scanner.getColumn());
        }
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(Text.getText("domname_" + type.substring(0, 3) + "_r") + " is at " + value + " or higher");
    }
}

class StabilityTrigger extends IntTrigger {

    StabilityTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Stability is at " + value + " or higher");
    }
}

class LandTrigger extends IntTrigger {

    LandTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Land tech is at " + value + " or higher");
    }
}

class NavalTrigger extends IntTrigger {

    NavalTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Naval tech is at " + value + " or higher");
    }
}

class InfraTrigger extends IntTrigger {

    InfraTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Infrastructure tech is at " + value + " or higher");
    }
}

class TradeTrigger extends IntTrigger {

    TradeTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Trade tech is at " + value + " or higher");
    }
}

class DiscoveredTrigger extends IntTrigger {

    DiscoveredTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(ProvinceDB.format(value) + " has been discovered by Europeans");
    }
}

class COTTrigger extends IntTrigger {

    COTTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(ProvinceDB.format(value) + " is a center of trade");
    }
}
abstract class BooleanTrigger extends Trigger {

    protected boolean value;

    protected BooleanTrigger(EUGScanner scanner) {
        TokenType next = scanner.nextToken();

        if (next != TokenType.ULSTRING) {
            warn("Unexpected value type: " + next + " (expected \"yes\" or \"no\")",
                    scanner.getLine(), scanner.getColumn());
        }

        value = (scanner.lastStr().equalsIgnoreCase("yes"));
    }
}


class AtWarTrigger extends BooleanTrigger {

    AtWarTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(value ? "Country is at war" : "Country is not at war");
    }
}

class AITrigger extends BooleanTrigger {

    AITrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(value ? "Country is controlled by AI" : "Country is controlled by human");
    }
}

class EmperorTrigger extends BooleanTrigger {

    EmperorTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Country " + (value ? "is" : "is not") + " the emperor of the Holy Roman Empire");
    }
}


class ElectorTrigger extends BooleanTrigger {
    ElectorTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Country " + (value ? "is" : "is not") + " an elector of the Holy Roman Empire");
    }
}
class YearTrigger extends IntTrigger {

    YearTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("It is the year " + value + " or later");
    }
}

/************************************************/
/* New FTG triggers below here                  */
/************************************************/
// (also the Region, Area, OtherCountry, and SomeOf triggers above)

class RandomTrigger extends IntTrigger {
    RandomTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(value + "% chance");
    }
}

class AccessTrigger extends StringTrigger {
    AccessTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have military access through " + EventDB.formatCountry(value));
    }
}

class TradingPostTrigger extends IntTrigger {
    TradingPostTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have a trading post in " + ProvinceDB.format(value));
    }
}

class ColonyTrigger extends IntTrigger {
    ColonyTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have a colony in " + ProvinceDB.format(value));
    }
}

class ColonialCityTrigger extends IntTrigger {
    ColonialCityTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have a colonial city in " + ProvinceDB.format(value));
    }
}

class CityTrigger extends IntTrigger {
    CityTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have a city in " + ProvinceDB.format(value));
    }
}

class IsVassalTrigger extends BooleanTrigger {
    IsVassalTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (value)
            out.write("Is a vassal");
        else
            out.write("Is not a vassal");
    }
}

class IsOverlordTrigger extends BooleanTrigger {
    IsOverlordTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (value)
            out.write("Have at least one vassal");
        else
            out.write("Have no vassals");
    }
}

abstract class ProvinceDateTrigger extends Trigger {
    protected int provId;
    protected int years;
    protected int months;
    protected int days;
    ProvinceDateTrigger(EUGScanner s) {
        if (s.nextToken() != TokenType.LBRACE) {
            warn("Expected '{'", s.getLine(), s.getColumn());
        }

        if (s.nextToken() != TokenType.IDENT) {
            warn("Expected \"type =\"", s.getLine(), s.getColumn());
        }

        if (!s.lastStr().equalsIgnoreCase("province")) {
            warn("Expected \"province =\"", s.getLine(), s.getColumn());
        }

        if (s.nextToken() != TokenType.ULSTRING) {
            warn("Expected province ID", s.getLine(), s.getColumn());
        }

        provId = Integer.parseInt(s.lastStr());

        while (true) {
            TokenType type = s.nextToken();
            if (type == TokenType.RBRACE) {
                break;
            } else if (type != TokenType.IDENT) {
                warn("Expected identifier; got " + s.lastToken(), s.getLine(), s.getColumn());
                while (s.nextToken() != TokenType.RBRACE) { }
            } else {
                if (s.lastStr().equals("years")) {
                    s.nextToken();
                    years = Integer.parseInt(s.lastStr());
                } else if (s.lastStr().equals("months")) {
                    s.nextToken();
                    months = Integer.parseInt(s.lastStr());
                } else if (s.lastStr().equals("days")) {
                    s.nextToken();
                    days = Integer.parseInt(s.lastStr());
                } else {
                    warn("Expected years, months, or days; got \"" + s.lastStr() + "\"", s.getLine(), s.getColumn());
                    s.nextToken();
                }
            }
        }

        if (s.lastToken() != TokenType.RBRACE) {
            while (s.nextToken() != TokenType.RBRACE) { }
        }
    }
    protected String getTimeSpan() {
        String ret = "";
        if (years != 0)
            ret += years + " years";
        if (months != 0) {
            if (years != 0)
                ret += " and ";
            ret += months + " months";
        }
        if (days != 0) {
            if (years != 0 || months != 0)
                ret += " and ";
            ret += days + " days";
        }
        return ret;
    }
}

class ControlChangeTrigger extends ProvinceDateTrigger {
    ControlChangeTrigger(EUGScanner s) {
        super(s);
    }
    
    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have controlled " + ProvinceDB.format(provId) + " for " + getTimeSpan());
    }
}

class OwnerChangeTrigger extends ProvinceDateTrigger {
    OwnerChangeTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have owned " + ProvinceDB.format(provId) + " for " + getTimeSpan());
    }
}

class InflationTrigger extends FloatTrigger {
    InflationTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have more than " + String.format("%.1f", value*100.0) + "% inflation");
    }
}

class TreasuryTrigger extends IntTrigger {
    TreasuryTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Have more than " + value + EventDB.getCoinLink());
    }
}

class TechGroupTrigger extends StringTrigger {
    TechGroupTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("In the " + Text.getText("techgroup_" + value) + " technology group");
    }
}

class TagTrigger extends StringTrigger {
    TagTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Is " + EventDB.formatCountry(value));
    }
}

class CapitalTrigger extends IntTrigger {
    CapitalTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(ProvinceDB.format(value) + " is the capital");
    }
}

class ProvincePopTrigger extends ProvinceTrigger {
    ProvincePopTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Population in " + ProvinceDB.format(province) + " is at least " + Integer.parseInt(data));
    }
}

class FortressLevelTrigger extends ProvinceTrigger {
    FortressLevelTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Fortress in " + ProvinceDB.format(province) + " is at least level " + Integer.parseInt(data));
    }
}

class CoreNationalTrigger extends ProvinceTrigger {
    CoreNationalTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (data.equals("-1"))
            out.write(ProvinceDB.format(province) + " is a national province");
        else
            out.write(ProvinceDB.format(province) + " is a national province of " + EventDB.formatCountry(data));
    }
}

class CoreClaimTrigger extends ProvinceTrigger {
    CoreClaimTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (data.equals("-1"))
            out.write(ProvinceDB.format(province) + " is a claim province");
        else
            out.write(ProvinceDB.format(province) + " is a claim province of " + EventDB.formatCountry(data));
    }
}

class CoreCasusBelliTrigger extends ProvinceTrigger {
    CoreCasusBelliTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        if (data.equals("-1"))
            out.write(ProvinceDB.format(province) + " is a casus belli province");
        else
            out.write(ProvinceDB.format(province) + " is a casus belli province of " + EventDB.formatCountry(data));
    }
}

class CityCultureTrigger extends ProvinceTrigger {
    CityCultureTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("City sprite graphics in " + ProvinceDB.format(province) + " are " + Text.getText("culture_" + data));
    }
}

class TruceTrigger extends CountryTrigger {

    TruceTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(ctag1) + " and " + EventDB.formatCountry(ctag2) + " have a truce");
    }
}

class UnionTrigger extends CountryTrigger {

    UnionTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(ctag2) + " is in a union with " + EventDB.formatCountry(ctag1));
    }
}

class IsUnionWithUsTrigger extends StringTrigger {

    public IsUnionWithUsTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Is in a union with " + EventDB.formatCountry(value));
    }
}

class CultureTrigger extends StringTrigger {
    CultureTrigger(EUGScanner s) {
        super(s);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(Text.getText("culture_" + value) + " is a state culture");
    }
}

class HreTrigger extends BooleanTrigger {
    HreTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Country's capital " + (value ? "is" : "is not") + " in the Holy Roman Empire");
    }
}

class OfficialsTrigger extends IntTrigger {

    private String type;

    public OfficialsTrigger(EUGScanner scanner, String type) {
        super(scanner);
        this.type = type;
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Country has at least " + value + " " + type);
    }
}

class OverlordTrigger extends CountryTrigger {

    OverlordTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write(EventDB.formatCountry(ctag2) + " is a vassal of " + EventDB.formatCountry(ctag1));
    }
}

class IsOurOverlordTrigger extends StringTrigger {

    public IsOurOverlordTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Is a vassal of " + EventDB.formatCountry(value));
    }
}

class NumOfPortsTrigger extends IntTrigger {

    public NumOfPortsTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Has at least " + value + " port" + (value == 1 ? "" : "s"));
    }
}

class NumOfColoniesTrigger extends IntTrigger {

    public NumOfColoniesTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Has at least " + value + " colon" + (value == 1 ? "y" : "ies"));
    }
}

class NumOfTradingPostsTrigger extends IntTrigger {

    public NumOfTradingPostsTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Has at least " + value + " trading post" + (value == 1 ? "" : "s"));
    }
}

class NumOfCotsTrigger extends IntTrigger {

    public NumOfCotsTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Has at least " + value + " center" + (value == 1 ? "" : "s") + " of trade");
    }
}

class ReligionGroupTrigger extends StringTrigger {

    public ReligionGroupTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Is in the " + Text.getText("religion_" + value) + " religious group");
    }
}

class ReligionSubgroupTrigger extends StringTrigger {

    public ReligionSubgroupTrigger(EUGScanner scanner) {
        super(scanner);
    }

    @Override
    void generateHTML(BufferedWriter out) throws IOException {
        out.write("Is in the " + Text.getText("religion_" + value) + " religious subgroup");
    }
}