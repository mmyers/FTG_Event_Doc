package eventdoc.ftg;

import eug.parser.EUGScanner;
import eug.parser.TokenType;
import java.io.BufferedWriter;
import java.io.IOException;

public class Command {

    private CommandType commandType;
    private Trigger trigger;

    Command(EUGScanner scanner, Action parent) {
        super();
        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Expected \'{\'", scanner.getLine(), scanner.getColumn());
        }

        boolean foundType = false;
        do {
            switch (scanner.nextToken()) {
                case IDENT:
                    if (scanner.lastStr().equalsIgnoreCase("trigger")) {
                        trigger = Trigger.parseTrigger(scanner, parent.getParent());
                    } else if (!scanner.lastStr().equalsIgnoreCase("type")) {
                        warn("Expected \"type =\"", scanner.getLine(), scanner.getColumn());
                    } else {
                        foundType = true;
                    }
                    break;
                case RBRACE:
                    return;
                default:
                    warn("Expected \"type =\" or \'}\'", scanner.getLine(), scanner.getColumn());
                    return;
            }
        } while (!foundType);

        if (scanner.nextToken() != TokenType.ULSTRING) {
            warn("Expected command type; found " + scanner.lastToken().name()
                    + " (" + scanner.lastStr() + ")",
                    scanner.getLine(), scanner.getColumn());
        }
        
        final String command = scanner.lastStr().toLowerCase();
        if (command.equals("revolt")) {
            commandType = new RevoltCommand(scanner);
        } else if (command.equals("religiousrevolt")) {
            commandType = new ReligiousRevoltCommand(scanner);
        } else if (command.equals("colonialrevolt")) {
            commandType = new ColonialRevoltCommand(scanner);
        } else if (command.equals("heretic")) {
            commandType = new HereticCommand(scanner);
        } else if (command.equals("conversion")) {
            commandType = new ConversionCommand(scanner);
        } else if (command.equals("provincereligion")) {
            commandType = new ProvinceReligionCommand(scanner);
        } else if (command.equals("adm")) {
            commandType = new AdmCommand(scanner);
        } else if (command.equals("dip")) {
            commandType = new DipCommand(scanner);
        } else if (command.equals("mil")) {
            commandType = new MilCommand(scanner);
        } else if (command.equals("domestic")) {
            commandType = new DomesticCommand(scanner);
        } else if (command.equals("dynastic")) {
            commandType = new DynasticCommand(scanner);
        } else if (command.equals("vassal")) {
            commandType = new VassalCommand(scanner);
        } else if (command.equals("alliance")) {
            commandType = new AllianceCommand(scanner);
        } else if (command.equals("inherit")) {
            commandType = new InheritCommand(scanner);
        } else if (command.equals("breakdynastic")) {
            commandType = new BreakDynasticCommand(scanner);
        } else if (command.equals("breakvassal")) {
            commandType = new BreakVassalCommand(scanner);
        } else if (command.equals("independence")) {
            commandType = new IndependenceCommand(scanner);
        } else if (command.equals("war")) {
            commandType = new WarCommand(scanner);
        } else if (command.equals("revoltrisk")) {
            commandType = new RevoltRiskCommand(scanner);
        } else if (command.equals("inflation")) {
            commandType = new InflationCommand(scanner);
        } else if (command.equals("treasury") || command.equals("cash")) {
            commandType = new TreasuryCommand(scanner);
        } else if (command.equals("manpower")) {
            commandType = new ManpowerCommand(scanner);
        } else if (command.equals("land")) {
            commandType = new LandCommand(scanner);
        } else if (command.equals("naval")) {
            commandType = new NavalCommand(scanner);
        } else if (command.equals("infra")) {
            commandType = new InfraCommand(scanner);
        } else if (command.equals("trade")) {
            commandType = new TradeCommand(scanner);
        } else if (command.equals("stability")) {
            commandType = new StabilityCommand(scanner);
        } else if (command.equals("diplomats")) {
            commandType = new DiplomatsCommand(scanner);
        } else if (command.equals("colonists")) {
            commandType = new ColonistsCommand(scanner);
        } else if (command.equals("merchants")) {
            commandType = new MerchantsCommand(scanner);
        } else if (command.equals("missionaries")) {
            commandType = new MissionariesCommand(scanner);
        } else if (command.equals("conquistador")) {
            commandType = new ConquistadorCommand(scanner);
        } else if (command.equals("explorer")) {
            commandType = new ExplorerCommand(scanner);
        } else if (command.equals("general")) {
            commandType = new GeneralCommand(scanner);
        } else if (command.equals("admiral")) {
            commandType = new AdmiralCommand(scanner);
        } else if (command.equals("privateer")) {
            commandType = new PrivateerCommand(scanner);
        } else if (command.equals("relation")) {
            commandType = new RelationCommand(scanner);
        } else if (command.equals("desertion")) {
            commandType = new DesertionCommand(scanner);
        } else if (command.equals("fortress")) {
            commandType = new FortressCommand(scanner);
        } else if (command.equals("population")) {
            commandType = new PopulationCommand(scanner);
        } else if (command.equals("inf") || command.equals("infantry")) {
            commandType = new InfantryCommand(scanner);
        } else if (command.equals("cav") || command.equals("cavalry")) {
            commandType = new CavalryCommand(scanner);
        } else if (command.equals("art") || command.equals("artillery")) {
            commandType = new ArtilleryCommand(scanner);
        } else if (command.equals("warships")) {
            commandType = new WarshipsCommand(scanner);
        } else if (command.equals("galleys")) {
            commandType = new GalleysCommand(scanner);
        } else if (command.equals("transports")) {
            commandType = new TransportsCommand(scanner);
        } else if (command.equals("provincetax")) {
            commandType = new ProvinceTaxCommand(scanner);
        } else if (command.equals("provincemanpower")) {
            commandType = new ProvinceManpowerCommand(scanner);
        } else if (command.equals("provinceculture")) {
            commandType = new ProvinceCultureCommand(scanner);
        } else if (command.equals("mine")) {
            commandType = new MineCommand(scanner);
        } else if (command.equals("province_revoltrisk")) {
            commandType = new ProvinceRevoltRiskCommand(scanner);
        } else if (command.equals("casusbelli")) {
            commandType = new CasusBelliCommand(scanner);
        } else if (command.equals("vp")) {
            commandType = new VPCommand(scanner);
        } else if (command.equals("capital")) {
            commandType = new CapitalCommand(scanner);
        } else if (command.equals("addcore")
                || command.equals("addcore_national")) {
            commandType = new AddCoreCommand(scanner);
        } else if (command.equals("addcore_claim")) {
            commandType = new AddCoreClaimCommand(scanner);
        } else if (command.equals("addcore_casusbelli")) {
            commandType = new AddCoreCBCommand(scanner);
        } else if (command.equals("removecore")
                || command.equals("removecore_national")) {
            commandType = new RemoveCoreCommand(scanner);
        } else if (command.equals("removecore_claim")) {
            commandType = new RemoveCoreClaimCommand(scanner);
        } else if (command.equals("removecore_casusbelli")) {
            commandType = new RemoveCoreCBCommand(scanner);
        } else if (command.equals("cot")) {
            commandType = new CotCommand(scanner);
        } else if (command.equals("removecot")) {
            commandType = new RemoveCotCommand(scanner);
        } else if (command.equals("trigger")) {
            commandType = new TriggerCommand(scanner);
        } else if (command.equals("sleepevent")) {
            commandType = new SleepEventCommand(scanner);
        } else if (command.equals("leader")) {
            commandType = new LeaderCommand(scanner);
        } else if (command.equals("wakeleader")) {
            commandType = new WakeLeaderCommand(scanner);
        } else if (command.equals("monarch") || command.equals("wakemonarch")) {
            commandType = new WakeMonarchCommand(scanner);
        } else if (command.equals("sleepleader")) {
            commandType = new SleepLeaderCommand(scanner);
        } else if (command.equals("sleepmonarch")) {
            commandType = new SleepMonarchCommand(scanner);
        } else if (command.equals("flagname")) {
            commandType = new FlagnameCommand(scanner);
        } else if (command.equals("add_countryculture")) {
            commandType = new AddCountryCultureCommand(scanner);
        } else if (command.equals("remove_countryculture")) {
            commandType = new RemoveCountryCultureCommand(scanner);
        } else if (command.equals("country")) {
            commandType = new CountryCommand(scanner);
        } else if (command.equals("religion")) {
            commandType = new ReligionCommand(scanner);
        } else if (command.equals("secedeprovince")
                || command.equals("cedeprovince")) {
            commandType = new SecedeProvinceCommand(scanner);
        } else if (command.equals("technology")) {
            commandType = new TechnologyCommand(scanner);
        } else if (command.equals("setflag")) {
            commandType = new SetFlagCommand(scanner, parent);
        } else if (command.equals("clrflag")) {
            commandType = new ClrFlagCommand(scanner, parent);
        } else if (command.equals("gainmanufactory")) {
            commandType = new GainManufactoryCommand(scanner);
        } else if (command.equals("losemanufactory")) {
            commandType = new LoseManufactoryCommand(scanner);
        } else if (command.equals("gainbuilding")) {
            commandType = new GainBuildingCommand(scanner);
        } else if (command.equals("losebuilding")) {
            commandType = new LoseBuildingCommand(scanner);
        } else if (command.equals("loansize")) {
            commandType = new LoanSizeCommand(scanner);
        } else if (command.equals("ai")) {
            commandType = new AICommand(scanner);
        } else if (command.equals("flag")) {
            commandType = new FlagCommand(scanner, parent);
        } else if (command.equals("badboy")) {
            commandType = new BadboyCommand(scanner);
        } else if (command.equals("natives")) {
            commandType = new NativesCommand(scanner);
        } else if (command.equals("discover")) { /* New FTG commands below here */
            commandType = new DiscoverCommand(scanner);
        } else if (command.equals("hre")) {
            commandType = new HRECommand(scanner);
        } else if (command.equals("goods")) {
            commandType = new ProvGoodsCommand(scanner);
        } else if (command.equals("terrain")) {
            commandType = new ProvTerrainCommand(scanner);
        } else if (command.equals("annex")) {
            commandType = new AnnexCommand(scanner);
        } else if (command.equals("giveaccess")) {
            commandType = new GiveAccessCommand(scanner);
        } else if (command.equals("givetrade")) {
            commandType = new GiveTradeCommand(scanner);
        } else if (command.equals("revoketrade")) {
            commandType = new RevokeTradeCommand(scanner);
        } else if (command.equals("control")) {
            commandType = new ProvControlCommand(scanner);
        } else if (command.equals("elector")) {
            commandType = new ElectorCommand(scanner);
        } else if (command.equals("nativeattack")) {
            commandType = new NativeAttackCommand(scanner);
        } else if (command.equals("breakoverlord")) {
            commandType = new BreakOverlordCommand(scanner);
        } else if (command.equals("populationpercent")) {
            commandType = new PopPercentCommand(scanner);
        } else if (command.equals("cityname")) {
            commandType = new CityNameCommand(scanner);
        } else if (command.equals("fortresslevel")) {
            commandType = new FortressLevelCommand(scanner);
        } else if (command.equals("cancelaccess")) {
            commandType = new CancelAccessCommand(scanner);
        } else if (command.equals("revokeaccess")) {
            commandType = new RevokeAccessCommand(scanner);
        } else if (command.equals("pirates")) {
            commandType = new PiratesCommand(scanner);
        } else if (command.equals("privateers")) {
            commandType = new PrivateersCommand(scanner);
        } else if (command.equals("alt_provincereligion")) {
            commandType = new AltProvinceReligionCommand(scanner);
        } else if (command.equals("cityculture")) {
            commandType = new CityCultureCommand(scanner);
        } else if (command.equals("countryname")) {
            commandType = new CountryNameCommand(scanner);
        } else if (command.equals("ai_add")) {
            commandType = new AiAddCommand(scanner);
        } else if (command.equals("ai_rem")) {
            commandType = new AiRemCommand(scanner);
        } else if (command.equals("ai_set")) {
            commandType = new AiSetCommand(scanner);
        } else if (command.equals("free")) {
            commandType = new FreeCommand(scanner);
        } else if (command.equals("disbandrebels")) {
            commandType = new DisbandRebelsCommand(scanner);
        } else {
            warn("Unknown command type: " + command, scanner.getLine(), scanner.getColumn());
            while (scanner.nextToken() != TokenType.RBRACE) { }
        }
    }
    
    /**
     * Returns the ID of the event that this command triggers, or -1 if this is
     * not a triggering command.
     * @return the ID of the event that this command triggers.
     */
    int getTriggeredEvent() {
        if (commandType instanceof TriggerCommand) {
            return ((TriggerCommand)commandType).which;
        }
        return -1;
    }

    /**
     * Returns the ID of the event that this command sleeps, or -1 if this is
     * not a sleepevent command.
     * @return the ID of the event that this command sleeps.
     */
    int getSleptEvent() {
        if (commandType instanceof SleepEventCommand) {
            return ((SleepEventCommand)commandType).which;
        }
        return -1;
    }

    boolean isValid() {
        return commandType != null;
    }
    
    public void generateHTML(BufferedWriter out) throws IOException {
        if (trigger != null) {
            // how to make this look reasonable?
            out.write("Conditions:<br />");
            out.newLine();
            trigger.generateHTML(out);
        }
        if (commandType != null)
            commandType.generateHTML(out);
        //else
        //    out.write("(no effects)");
    }
    
    private static void warn(String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }
    
    
    private static abstract class CommandType {
        
        private String which;
        private String value;
        private String where;

        public CommandType(EUGScanner scanner) {
            parseLoop:
            while (true) {
                switch (scanner.nextToken()) {
                    case IDENT:
                        String ident = scanner.lastStr().toLowerCase();
                        if (ident.equals("which")) {
                            scanner.nextToken();
                            which = scanner.lastStr();
                        } else if (ident.equals("value")) {
                            scanner.nextToken();
                            value = scanner.lastStr();
                        } else if (ident.equals("where")) {
                            scanner.nextToken();
                            where = scanner.lastStr();
                        } else if (ident.equals("required")) {
                            scanner.nextToken(); // don't care about required
                        } else {
                            warn("Unknown variable in command (expected which/value/where): "
                                    + scanner.lastStr(), scanner.getLine(), scanner.getColumn());
                            scanner.skipNext();
                        }
                        break;
                    case RBRACE:
                        break parseLoop;
                    default:
                        warn("Invalid command part: " + scanner.lastToken(), scanner.getLine(), scanner.getColumn());
                        break;
                }
            }
        }
        
        protected final String getWhich() {
            // FTG has text aliases for the -X codes
            String s = which;
            if (s == null)
                return s;
            if (s.equalsIgnoreCase("random")) {
                return "-1";
            } else if (s.equalsIgnoreCase("capital")) {
                return "-2";
            } else if (s.equalsIgnoreCase("last_random")) {
                return "-3";
            } else if (s.equalsIgnoreCase("random_distinct")) {
                return "-4";
            } else if (s.equalsIgnoreCase("random_not_capital")) {
                return "-5";
            } else if (s.equalsIgnoreCase("emperor")) {
                return "-6";
            } else if (s.equalsIgnoreCase("random_elector")) {
                return "-7";
            } else if (s.equalsIgnoreCase("random_distinct_not_capital")) {
                return "-8";
            } else if (s.equalsIgnoreCase("random_distinct_elector")) {
                return "-9";
            } else if (s.equalsIgnoreCase("overlord")) {
                return "-10";
            } else if (s.equalsIgnoreCase("random_same_religion")) {
                return "-11";
            } else if (s.equalsIgnoreCase("random_same_religious_group")) {
                return "-12";
            }
            
            return which;
        }
        
        protected final String getValue() {
            return value;
        }

        protected final String getWhere() {
            return where;
        }
        
        private String getWhereProvinceString() {
            if (where == null)
                return "";
            
            if (where.equalsIgnoreCase("neighbor") || where.equalsIgnoreCase("neighbour"))
                return " neighboring our country";
            if (where.equalsIgnoreCase("coastal"))
                return " on the sea coast";
            if (where.equalsIgnoreCase("border"))
                return " bordering another country";
            if (GeographyDB.isValidTag(where))
                return " in " + Text.getText(GeographyDB.getName(where));
            System.out.println("Unknown 'where' type: " + where);
            return "";
        }
        
        private String getWhereCountryString() {
            if (where == null)
                return "";
            
            if (where.equalsIgnoreCase("neighbor") || where.equalsIgnoreCase("neighbour"))
                return " neighboring our country";
            //if (where.equalsIgnoreCase("coastal")) // coastal not valid for countries
            //    return " on the sea coast";
            //if (where.equalsIgnoreCase("border")) // border not valid for countries
            //    return " bordering another country";
            if (GeographyDB.isValidTag(where))
                return " in " + Text.getText(GeographyDB.getName(where));
            System.out.println("Unknown 'where' type: " + where);
            return "";
        }
        
        /** Resolve the given int target, which may be either a province ID or a random set */
        protected String getProv(int which) {
            // If 0 or above, it's just a province
            if (which >= 0) {
                return ProvinceDB.format(which);
            }
            
            switch (which) {
                case -1:
                    return "a random province" + getWhereProvinceString();
                case -2:
                    return "the capital province";
                case -3:
                    return "the same province";
                case -4:
                    return "a different random province" + getWhereProvinceString();
                case -5:
                    return "a random non-capital province" + getWhereProvinceString();
                case -6:
                case -7:
                case -9:
                case -10:
                case -11:
                case -12:
                    System.out.println("Cannot use " + which + " for province in commands");
                    return which + " (error)";
                case -8:
                    return "a different non-capital province" + getWhereProvinceString();
                default:
                    int id = (-which) - 1000;
                    return "a random province in " + Text.getText(GeographyDB.getName(id)) + getWhereProvinceString();
            }
        }

        /** Same as getProv(), only capitalized */
        protected String getProvCap(int which) {
            if (which >= 0) {
                return ProvinceDB.format(which);
            }
            switch (which) {
                case -1:
                    return "A random province" + getWhereProvinceString();
                case -2:
                    return "The capital province";
                case -3:
                    return "The same province";
                case -4:
                    return "A different random province" + getWhereProvinceString();
                case -5:
                    return "A random non-capital province" + getWhereProvinceString();
                case -6:
                case -7:
                case -9:
                case -10:
                case -11:
                case -12:
                    System.out.println("Cannot use " + which + " for province in commands");
                    return which + " (error)";
                case -8:
                    return "A different non-capital province" + getWhereProvinceString();
                default:
                    int id = (-which) - 1000;
                    return "A random province in " + Text.getText(GeographyDB.getName(id)) + getWhereProvinceString();
            }
        }


        protected String getCountry(String tag) {
            if (!tag.matches("-?\\d{1,2}")) { // not a negative integer, let's hope it's a tag
                return EventDB.formatCountry(tag);
            }
            String ret;
            int whichType = Integer.parseInt(tag);
            boolean needsWhere = false;

            switch (whichType) {
                case -1:
                    ret = "a random country";
                    needsWhere = true;
                    break;
                case -3:
                    ret = "the same country";
                    break;
                case -4:
                    ret = "a different random country";
                    needsWhere = true;
                    break;
                case -6:
                    ret = "the Holy Roman Emperor";
                    break;
                case -7:
                    ret = "a random elector";
                    needsWhere = true;
                    break;
                case -9:
                    ret = "a different random elector";
                    needsWhere = true;
                    break;
                case -10: 
                    ret = "the overlord";
                    break;
                case -11:
                    ret = "a random country with the same religion";
                    needsWhere = true;
                    break;
                case -12:
                    ret = "a random country in the same religious group";
                    needsWhere = true;
                    break;
                case -2: // capital
                case -5: // random_not_capital
                case -8: // random_distinct_not_capital
                    System.out.println("Cannot use " + whichType + " to target countries in event commands");
                    ret = "UNKNOWN";
                    break;
                default:
                    System.out.println("Unknown country type in event command: " + tag);
                    ret = "UNKNOWN";
                    break;
            }

            if (needsWhere)
                ret += getWhereCountryString();
            else if (getWhere() != null)
                System.out.println("Unexpected 'where' clause with " + tag + " in event command");
            
            return ret;
        }

        protected abstract void generateHTML(BufferedWriter out) throws IOException;
    }
    
    private static abstract class IntCommand extends CommandType {
        protected int which;
        
        IntCommand(EUGScanner scanner) {
            super(scanner);
            which = Integer.parseInt(getWhich());
        }
    }
    
    /** A command that targets a province with "which = xxx" */
    private static abstract class ProvinceCommand extends IntCommand {
        ProvinceCommand(EUGScanner scanner) {
            super(scanner);
        }

        protected String getProv() {
            return getProv(which);
        }
        protected String getProvCap() {
            return getProvCap(which);
        }
    }
    
    private static class RevoltCommand extends ProvinceCommand {

        RevoltCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (getValue() != null) {
                out.write(EventDB.formatCountry(getValue()) + " revolt in " + getProv());
            } else {
                out.write(getProvCap() + " revolts");
            }
        }
    }

    private static class ReligiousRevoltCommand extends ProvinceCommand {

        ReligiousRevoltCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Create a religious revolt in " + getProv());
        }
    }

    private static class ColonialRevoltCommand extends ProvinceCommand {

        ColonialRevoltCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Create a colonial revolt in " + getProv());
        }
    }

    private static class HereticCommand extends ProvinceCommand {

        HereticCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " converts to a heretic religion");
        }
    }

    private static class ConversionCommand extends ProvinceCommand {

        ConversionCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " converts to the state religion");
        }
    }
    
    private static class ConquistadorCommand extends ProvinceCommand {
        ConquistadorCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            String values = "";
            if (getValue() != null) {
                int maxValue = Integer.parseInt(getValue());
                values = " with maximum stats " + maxValue + "/" + maxValue + "/" + maxValue + "/" + (maxValue/2);
            }
            out.write("Gain a conquistador in " + getProv() + values);
        }
    }
    
    private static class ExplorerCommand extends ProvinceCommand {
        ExplorerCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            String values = "";
            if (getValue() != null) {
                int maxValue = Integer.parseInt(getValue());
                values = " with maximum stats " + maxValue + "/" + maxValue + "/" + maxValue;
            }
            out.write("Gain an explorer in " + getProv() + values);
        }
    }
    
    private static class GeneralCommand extends ProvinceCommand {
        GeneralCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            String values = "";
            if (getValue() != null) {
                int maxValue = Integer.parseInt(getValue());
                values = " with maximum stats " + maxValue + "/" + maxValue + "/" + maxValue + "/" + (maxValue/2);
            }
            out.write("Gain a general in " + getProv() + values);
        }
    }
    
    private static class AdmiralCommand extends ProvinceCommand {
        AdmiralCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            String values = "";
            if (getValue() != null) {
                int maxValue = Integer.parseInt(getValue());
                values = " with maximum stats " + maxValue + "/" + maxValue + "/" + maxValue;
            }
            out.write("Gain an admiral in " + getProv() + values);
        }
    }
    
    private static class PrivateerCommand extends ProvinceCommand {
        PrivateerCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            String values = "";
            if (getValue() != null) {
                int maxValue = Integer.parseInt(getValue());
                values = " with maximum stats " + maxValue + "/" + maxValue + "/" + maxValue;
            }
            out.write("Gain a privateer in " + getProv() + values);
        }
    }
    
    private static class CapitalCommand extends ProvinceCommand {
        CapitalCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Move capital to " + getProv());
        }
    }
    
    private static class AddCoreCommand extends ProvinceCommand {
        AddCoreCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " will be considered a national province");
        }
    }

    /** FTG */
    private static class AddCoreClaimCommand extends ProvinceCommand {

        AddCoreClaimCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " will be considered a claim province");
        }
    }

    /** FTG */
    private static class AddCoreCBCommand extends ProvinceCommand {

        AddCoreCBCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " will be considered a casus belli province");
        }
    }
    
    private static class RemoveCoreCommand extends ProvinceCommand {
        RemoveCoreCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " will no longer be considered a national province");
        }
    }

    /** FTG */
    private static class RemoveCoreClaimCommand extends ProvinceCommand {
        RemoveCoreClaimCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " will no longer be considered a claim province");
        }
    }

    /** FTG */
    private static class RemoveCoreCBCommand extends ProvinceCommand {
        RemoveCoreCBCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " will no longer be considered a casus belli province");
        }
    }
    
    private static class LoseManufactoryCommand extends ProvinceCommand {
        LoseManufactoryCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Manufactory in " + getProv() + " is destroyed");
        }
    }
    
    private static class CotCommand extends ProvinceCommand {
        CotCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap() + " becomes a center of trade");
        }
    }
    
    private static class RemoveCotCommand extends ProvinceCommand {
        RemoveCotCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Center of trade in " + getProv() + " closes");
        }
    }
    
    private static class TriggerCommand extends IntCommand {
        TriggerCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Event " + EventDB.makeLink(which) + " is triggered immediately");
        }
    }
    
    private static class SleepEventCommand extends IntCommand {
        SleepEventCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Event " + EventDB.makeLink(which) + " will never fire");
        }
    }
    
    private static class LeaderCommand extends IntCommand {
        LeaderCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (Main.checkLeaders && !LeaderDB.isDormant(which))
                System.out.println(Event.currentId + ": Warning: Waking leader " + which + ", who might not be dormant");
            out.write(LeaderDB.format(which) + " becomes active");
        }
    }
    
    private static class WakeMonarchCommand extends IntCommand {
        WakeMonarchCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (Main.checkMonarchs && !MonarchDB.isDormant(which))
                System.out.println(Event.currentId + ": Warning: Waking monarch " + which + ", who might not be dormant");
            out.write("Monarch " + MonarchDB.format(which) + " becomes active");
        }
    }
    
    private static class WakeLeaderCommand extends IntCommand {
        WakeLeaderCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (Main.checkLeaders && !LeaderDB.isDormant(which))
                System.out.println(Event.currentId + ": Warning: Waking leader " + which + ", who might not be dormant");
            out.write(LeaderDB.format(which) + " becomes active");
        }
    }
    
    private static class SleepMonarchCommand extends IntCommand {
        SleepMonarchCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (Main.checkMonarchs && MonarchDB.isDormant(which))
                System.out.println(Event.currentId + ": Warning: Sleeping monarch " + which + ", who might already be dormant");
            out.write("Monarch " + MonarchDB.format(which) + " will never rule");
        }
    }
    
    private static class SleepLeaderCommand extends IntCommand {
        SleepLeaderCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (Main.checkLeaders && LeaderDB.isDormant(which))
                System.out.println(Event.currentId + ": Warning: Sleeping leader " + which + ", who might already be dormant");
            out.write(LeaderDB.format(which) + " will never be active");
        }
    }
    
    private static class LoanSizeCommand extends IntCommand {
        LoanSizeCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Size of loans changed to " + which + " ducats");
        }
    }
    
    private static class FlagCommand extends IntCommand {
        FlagCommand(EUGScanner scanner, Action parent) {
            super(scanner);
            EventFlag.getFlag(Integer.toString(which)).addSet(parent);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Global flag " + EventFlag.formatFlag(Integer.toString(which)) + " is set");// (" + Text.getText("ee_flag_" + which).replace("\\n", "") + ")");
        }
    }

    private static abstract class ProvinceStringCommand extends CommandType {

        protected int provNum;
        protected String value;

        ProvinceStringCommand(EUGScanner scanner) {
            super(scanner);
            provNum = Integer.parseInt(getWhich());
            value = getValue();
        }
        
        protected String getProv() {
            return getProv(provNum);
        }
        
        protected String getProvCap() {
            return getProvCap(provNum);
        }
    }

    private static class ProvinceReligionCommand extends ProvinceStringCommand {

        ProvinceReligionCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
//            if (value.equals("-1"))
//                out.write("Religion in " + getProv() + " changes to the state religion");
//            else
                out.write("Religion in " + getProv() + " changes to " + Text.getText("religion_" + value));
        }
    }
    
    private static class ProvinceCultureCommand extends ProvinceStringCommand {
        ProvinceCultureCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (value.equals("-1"))
                out.write("Culture in " + getProv() + " changes to the primary state culture");
            else
                out.write("Culture in " + getProv() + " changes to " + Text.getText("culture_" + value));
        }
    }
    
    private static class GainManufactoryCommand extends ProvinceStringCommand {
        GainManufactoryCommand(EUGScanner scanner) {
            super(scanner);
        }
        
        private final String getManu() {
            if (value.equals("-1")) {
                return "random manufactory";
            } else if (value.equalsIgnoreCase("luxury")) {
                return Text.getText("facility_luxurygoods");
            } else if (value.equalsIgnoreCase("weapons")) {
                return Text.getText("facility_weapons");
            } else if (value.equalsIgnoreCase("navalequipment")) {
                return Text.getText("facility_navalequipments");
            } else if (value.equalsIgnoreCase("refinery")) {
                return Text.getText("facility_refinery");
            } else if (value.equalsIgnoreCase("goods")) {
                return Text.getText("facility_manufacturedgoods");
            } else {
                return "<font color=\"red\">unknown manufactory (error)</font>";
            }
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain " + getManu() + " in " + getProv());
        }
    }
    
    private static class GainBuildingCommand extends ProvinceStringCommand {
        GainBuildingCommand(EUGScanner scanner) {
            super(scanner);
        }
        
        private final String getBuilding() {
            if (value.equals("-1")) {
                return "random building";
            } else {
                return value;
            }
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain " + getBuilding() + " in " + getProv());
        }
    }
    
    private static class LoseBuildingCommand extends ProvinceStringCommand {
        LoseBuildingCommand(EUGScanner scanner) {
            super(scanner);
        }
        
        private final String getBuilding() {
            if (value.equals("-1")) {
                return "random building";
            } else {
                return value;
            }
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Lose " + getBuilding() + " in " + getProv());
        }
    }
    
    private static abstract class TwoIntCommand extends CommandType {
        
        protected int which;
        protected int value;
        
        TwoIntCommand(EUGScanner scanner) {
            super(scanner);
            which = Integer.parseInt(getWhich());
            value = Integer.parseInt(getValue());
        }
    }
    
    private static class AdmCommand extends TwoIntCommand {

        AdmCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Monarch's administrative skill " + (which > 0 ? "+" : "") + which + " for " + value + " months");
        }
        
    }
    
    private static class DipCommand extends TwoIntCommand {

        DipCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Monarch's diplomatic skill " + (which > 0 ? "+" : "") + which + " for " + value + " months");
        }
    }
    
    private static class MilCommand extends TwoIntCommand {

        MilCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Monarch's military skill " + (which > 0 ? "+" : "") + which + " for " + value + " months");
        }
    }
    
    private static class RevoltRiskCommand extends CommandType {
        private int which;
        private int value;
        RevoltRiskCommand(EUGScanner scanner) {
            super(scanner);
            if (getWhich() != null)
                which = Integer.parseInt(getWhich());
            else
                which = Integer.MIN_VALUE;

            value = Integer.parseInt(getValue());
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (which == Integer.MIN_VALUE) {
                out.write("Global revolt risk " + (value > 0 ? "+" : "") + value + " for 12 months");
            } else {
                out.write("Global revolt risk  " + (value > 0 ? "+" : "") + value + " for " + which + " months");
            }
        }
        
    }
    
    private static class DomesticCommand extends CommandType {
        private String policy;
        private int value;
        
        DomesticCommand(EUGScanner scanner) {
            super(scanner);
            policy = getWhich();
            value = Integer.parseInt(getValue());
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(Text.getText("domname_" + policy.substring(0, 3) + "_r") + (value > 0 ? " +" : " ") + value);
        }
    }
    
    private static abstract class StringCommand extends CommandType {
        protected String which;
        
        StringCommand(EUGScanner scanner) {
            super(scanner);
            which = getWhich();
        }
    }
    
    private static abstract class TagCommand extends StringCommand {
        TagCommand(EUGScanner scanner) {
            super(scanner);
        }
        
        protected final String getCountry() {
            return getCountry(which);
        }
    }
    
    private static class DynasticCommand extends TagCommand {

        DynasticCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain a royal marriage with " + getCountry());
        }
    }

    private static class VassalCommand extends TagCommand {

        VassalCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain " + getCountry() + " as vassals");
        }
    }

    private static class AllianceCommand extends TagCommand {

        AllianceCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain an alliance with " + getCountry());
        }
    }

    private static class InheritCommand extends TagCommand {

        InheritCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Inherit the realms of " + getCountry());
        }
    }
    
    private static class BreakDynasticCommand extends TagCommand {
        BreakDynasticCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Break royal marriage with " + getCountry());
        }
    }
    
    private static class BreakVassalCommand extends TagCommand {
        BreakVassalCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Break vassalization with " + getCountry());
        }
    }
    
    private static class IndependenceCommand extends TagCommand {
        IndependenceCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Grant independence to " + getCountry() + " as a vassal");
        }
    }
    
    private static class WarCommand extends TagCommand {
        WarCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Start a war with " + getCountry());
        }
    }
    
    private static class CountryCommand extends TagCommand {
        CountryCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Become " + getCountry());
        }
    }
    
    private static class AddCountryCultureCommand extends StringCommand {
        AddCountryCultureCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(Text.getText("culture_" + which) + " will become an accepted culture");
        }
    }
    
    private static class RemoveCountryCultureCommand extends StringCommand {
        RemoveCountryCultureCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(Text.getText("culture_" + which) + " will no longer be an accepted culture");
        }
    }
    
    private static class ReligionCommand extends StringCommand {
        ReligionCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Change religion to " + Text.getText("religion_" + which));
        }
    }
    
    private static class TechnologyCommand extends StringCommand {
        TechnologyCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Change technology group to " + Text.getText("techgroup_" + which));
        }
    }
    
    private static class SetFlagCommand extends StringCommand {
        SetFlagCommand(EUGScanner scanner, Action parent) {
            super(scanner);
            EventFlag.getFlag(which).addSet(parent);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Set flag " + EventFlag.formatFlag(which) + " for events");
        }
    }
    
    private static class ClrFlagCommand extends StringCommand {
        ClrFlagCommand(EUGScanner scanner, Action parent) {
            super(scanner);
            EventFlag.getFlag(which).addClear(parent);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Clear flag " + EventFlag.formatFlag(which));
        }
    }
    
    private static class FlagnameCommand extends CommandType {
        private String flagname;
        
        FlagnameCommand(EUGScanner scanner) {
            super(scanner);
            flagname = getWhich();
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (flagname.length() == 0)
                out.write("Flag graphics set to default");
            else
                out.write("Flag graphics extension set to \"" + flagname + "\"");
        }
    }
    
    private static class AICommand extends CommandType {
        private String ai;
        
        AICommand(EUGScanner scanner) {
            super(scanner);
            ai = getWhich();
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("AI file changed to \"" + ai + "\"");
        }
    }
    
    private static abstract class ValueCommand extends CommandType {
        protected int value;
        
        ValueCommand(EUGScanner scanner) {
            super(scanner);
            value = Integer.parseInt(getValue());
        }
    }
    
    private static class InflationCommand extends ValueCommand {

        InflationCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + "% inflation");
        }
    }

    private static class TreasuryCommand extends ValueCommand {

        TreasuryCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " " + EventDB.getCoinLink());
        }
    }

    private static class ManpowerCommand extends ValueCommand {

        ManpowerCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " national manpower");
        }
    }

    private static class LandCommand extends ValueCommand {

        LandCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Land tech investment: " + (value > 0 ? "+" : "") + value);
        }
    }

    private static class NavalCommand extends ValueCommand {

        NavalCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Naval tech investment: " + (value > 0 ? "+" : "") + value);
        }
    }

    private static class InfraCommand extends ValueCommand {

        InfraCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Infrastructure tech investment: " + (value > 0 ? "+" : "") + value);
        }
    }

    private static class TradeCommand extends ValueCommand {

        TradeCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Trade tech investment: " + (value > 0 ? "+" : "") + value);
        }
    }
    
    private static class StabilityCommand extends ValueCommand {

        StabilityCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Stability " + (value > 0 ? "+" : "") + value);
        }
    }

    private static class DiplomatsCommand extends ValueCommand {

        DiplomatsCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " diplomats");
        }
    }

    private static class ColonistsCommand extends ValueCommand {

        ColonistsCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " colonists");
        }
    }

    private static class MerchantsCommand extends ValueCommand {

        MerchantsCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " merchants");
        }
    }

    private static class MissionariesCommand extends ValueCommand {

        MissionariesCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " missionaries");
        }
    }
    
    private static class VPCommand extends ValueCommand {
        
        VPCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " victory points");
        }
    }
    
    private static class BadboyCommand extends ValueCommand {
        BadboyCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " badboy");
        }
    }
    
    private static abstract class CountryValueCommand extends CommandType {
        protected String tag;
        protected int value;
        
        CountryValueCommand(EUGScanner scanner) {
            super(scanner);
            tag = getWhich();
            value = Integer.parseInt(getValue());
        }
        
        protected final String getCountry() {
            return getCountry(tag);
        }
    }
    
    private static class RelationCommand extends CountryValueCommand {
        
        RelationCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+"+value : value) + " relations with " + getCountry());
        }
    }
    
    private static class CasusBelliCommand extends CountryValueCommand {
        
        CasusBelliCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain a temporary casus belli against " + getCountry() + " for " + value + " months");
        }
    }
    
    private static class SecedeProvinceCommand extends CountryValueCommand {
        SecedeProvinceCommand(EUGScanner scanner) {
            super(scanner);
        }
        
        private final String getProv() {
            return getProv(value);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Cede " + getProv() + " to " + getCountry());
        }
    }
    
    private static abstract class ProvinceValueCommand extends CommandType {
        protected int provNum;
        protected int value;
        
        ProvinceValueCommand(EUGScanner scanner) {
            super(scanner);
            provNum = Integer.parseInt(getWhich());
            value = Integer.parseInt(getValue());
        }
        
        protected final String getProv() {
            return getProv(provNum);
        }
    }
    
    private static class DesertionCommand extends ProvinceValueCommand {

        DesertionCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Lose " + value + " troops in " + getProv());
        }
    }
    
    private static class FortressCommand extends ProvinceValueCommand {

        FortressCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Fortress level in " + getProv() + " " +  (value > 0 ? "+" : "") + value);
        }
    }
    
    private static class PopulationCommand extends ProvinceValueCommand {

        PopulationCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " population in " + getProv());
        }
    }
    
    private static class InfantryCommand extends ProvinceValueCommand {

        InfantryCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " infantry in " + getProv());
        }
    }
    
    private static class CavalryCommand extends ProvinceValueCommand {

        CavalryCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " cavalry in " + getProv());
        }
    }

    private static class ArtilleryCommand extends ProvinceValueCommand {

        ArtilleryCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " artillery in " + getProv());
        }
    }

    private static class WarshipsCommand extends ProvinceValueCommand {

        WarshipsCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain " + value + " warships in " + getProv());
        }
    }

    private static class GalleysCommand extends ProvinceValueCommand {

        GalleysCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain " + value + " galleys in " + getProv());
        }
    }

    private static class TransportsCommand extends ProvinceValueCommand {

        TransportsCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Gain " + value + " transports in " + getProv());
        }
    }
    
    private static class ProvinceTaxCommand extends ProvinceValueCommand {

        ProvinceTaxCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " base tax value in " + getProv());
        }
    }
    
    private static class ProvinceManpowerCommand extends ProvinceValueCommand {

        ProvinceManpowerCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " base manpower in " + getProv());
        }
    }
    
    private static class MineCommand extends ProvinceValueCommand {

        MineCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + " mine value in " + getProv());
        }
    }
    
    private static class ProvinceRevoltRiskCommand extends ProvinceValueCommand {
        ProvinceRevoltRiskCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Revolt risk value in " + getProv() + " " + (value > 0 ? "+" : "") + value);
        }
    }
    
    private static class NativesCommand extends ProvinceValueCommand {
        NativesCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Natives aggressiveness in " + getProv() + " " + (value > 0 ? "+" : "") + value);
        }
    }
    
    /****************************************/
    /* New FTG commands below here          */
    /****************************************/
    
    private static class DiscoverCommand extends ProvinceCommand {
        DiscoverCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Discover " + getProv());
        }
    }
    
    private static class HRECommand extends CommandType {
        private int which;
        private boolean value;
        HRECommand(EUGScanner scanner) {
            super(scanner);
            which = Integer.parseInt(getWhich());
            value = getValue().equalsIgnoreCase("yes");
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getProvCap(which) + (value ? " enters" : " leaves") + " the Holy Roman Empire");
        }
    }
    
    private static class ProvGoodsCommand extends ProvinceStringCommand {
        ProvGoodsCommand(EUGScanner scanner) {
            super(scanner);
            value = Text.getText("goods_" + value);
            if (value.startsWith("goods_"))
                warn("Unknown goods type \"" + value + "\" in province goods command", scanner.getLine(), scanner.getColumn());
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Goods in " + getProv() + " changed to " + value);
        }
    }

    private static class ProvTerrainCommand extends ProvinceStringCommand {
        ProvTerrainCommand(EUGScanner scanner) {
            super(scanner);
            value = Text.getText("terrain_" + value);
            if (value.startsWith("terrain_"))
                warn("Unknown terrain type \"" + value + "\" in province terrain command", scanner.getLine(), scanner.getColumn());
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Terrain in " + getProv() + " changed to " + value);
        }
    }

    private static class AnnexCommand extends TagCommand {
        AnnexCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Annex " + getCountry());
        }
    }

    private static class GiveAccessCommand extends TagCommand {
        GiveAccessCommand(EUGScanner scanner) {
            super(scanner);
        }
        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Grant military access to " + getCountry());
        }
    }

    private static class CancelAccessCommand extends TagCommand {
        CancelAccessCommand(EUGScanner scanner) {
            super(scanner);
        }
        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Cancel military access through " + getCountry());
        }
    }

    private static class RevokeAccessCommand extends TagCommand {
        RevokeAccessCommand(EUGScanner scanner) {
            super(scanner);
        }
        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Revoke military access granted to " + getCountry());
        }
    }

    private static class GiveTradeCommand extends TagCommand {
        GiveTradeCommand(EUGScanner scanner) {
            super(scanner);
        }
        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Sign a trade agreement with " + getCountry());
        }
    }

    private static class RevokeTradeCommand extends TagCommand {
        RevokeTradeCommand(EUGScanner scanner) {
            super(scanner);
        }
        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Revoke any trade agreement with " + getCountry());
        }
    }

    private static class ProvControlCommand extends ProvinceStringCommand {
        ProvControlCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            String country = Text.getText(value);
            if (country.equals("-1"))
                country = "this country";
            out.write("Give control of " + getProv() + " to " + country);
        }
    }

    private static class ElectorCommand extends CommandType {
        protected String tag;
        protected int value;

        ElectorCommand(EUGScanner scanner) {
            super(scanner);
            tag = getWhich();
            if (tag == null)
                tag = "0";
            value = Integer.parseInt(getValue());
        }

//        protected final String getTag(EUGScanner scanner) {
//            if (scanner.nextToken() != TokenType.IDENT) {
//                warn("Expected \"which =\" or \"value =\"", scanner.getLine(), scanner.getColumn());
//            }
//
//            if (!scanner.lastStr().equalsIgnoreCase("which")) {
//                if (scanner.lastStr().equalsIgnoreCase("value")) {
//                    scanner.pushBack();
//                    return "0";
//                }
//                warn("Expected \"which =\" or \"value =\"", scanner.getLine(), scanner.getColumn());
//            }
//
//            if (scanner.nextToken() != TokenType.ULSTRING) {
//                warn("Expected \"which = <value>\"", scanner.getLine(), scanner.getColumn());
//            }
//
//            return scanner.lastStr();
//        }

        protected final String getCountry() {
            if (tag.equals("0")) { // special-cased only for this command
                return "this country";
            } else {
                return getCountry(tag);
            }
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(getCountry() + " will now have " + value
                    + " electoral votes in the Holy Roman Empire");
        }
    }

    private static class NativeAttackCommand extends ProvinceCommand {
        NativeAttackCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Natives in " + getProv() + " attack any settlement present");
        }
    }

    private static class BreakOverlordCommand extends TagCommand {
        BreakOverlordCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Break vassalization of " + getCountry());
        }
    }

    private static class PopPercentCommand extends ProvinceValueCommand {
        PopPercentCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write((value > 0 ? "+" : "") + value + "% population in " + getProv());
        }
    }

    private static class CityNameCommand extends ProvinceStringCommand {
        CityNameCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Change the city name in " + getProv() + " to " + value);
        }
    }

    private static class FortressLevelCommand extends ProvinceValueCommand {
        FortressLevelCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Fortress level in " + getProv() + " set to " + value);
        }
    }

    private static class PiratesCommand extends ProvinceValueCommand {
        PiratesCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write(value + " pirate vessels will appear in " + getProv());
        }
    }

    private static class PrivateersCommand extends ProvinceCommand {
        PrivateersCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Commission privateers in " + getProv());
        }
    }

    private static class AltProvinceReligionCommand extends ProvinceStringCommand {
        AltProvinceReligionCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Religion in " + getProv() + " changes to " + Text.getText(value)); // even if owned by someone else
        }
    }

    private static class CityCultureCommand extends ProvinceStringCommand {
        CityCultureCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            if (value.equals("-1"))
                out.write("City culture graphics in " + getProv() + " change to the primary state culture");
            else
                out.write("City culture graphics in " + getProv() + " change to " + Text.getText("culture_" + value));
        }
    }

    private static class CountryNameCommand extends CommandType {

        private String value;

        CountryNameCommand(EUGScanner scanner) {
            super(scanner);
            value = getValue();
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Set country name to " + Text.getText(value));
        }
    }

    private static abstract class AiListCommand extends CommandType {
        protected String which;
        protected String value;

        public static String[] lists = { "area", "region", "continent", "ignore", "combat" };

        AiListCommand(EUGScanner scanner) {
            super(scanner);
            which = getWhich();
            value = getValue();

            boolean validList = false;
            for (String list : lists) {
                if (list.equalsIgnoreCase(which)) {
                    validList = true;
                    break;
                }
            }

            if (!validList)
                warn("Unknown AI list: \"" + which + "\"", scanner.getLine(), scanner.getColumn());
        }
    }

    private static class AiAddCommand extends AiListCommand {

        public AiAddCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("AI adds the value \"" + value + "\" to the list \"" + which + "\"");
        }
    }

    private static class AiRemCommand extends AiListCommand {

        public AiRemCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("AI removes the value \"" + value + "\" from the list \"" + which + "\"");
        }
    }

    private static class AiSetCommand extends CommandType {
        public static String[] prefs = new String[]{
            "expansion",
            "war",
            "ferocity",
            "sreligion",
            "naval",
            "galleys",
            "base",
            "front",
            "conquer.enemy",
            "conquer.supply",
            "conquer.distance",
            "conquer.owner",
            "conquer.notsupply",
            "conquer.base",
            "garrison.fortress",
            "garrison.strategic",
            "garrison.size",
            "garrison.supply",
            "garrison.war"
        };
        public AiSetCommand(EUGScanner scanner) {
            super(scanner);

            boolean valid = false;
            String which = getWhich();
            for (String pref : prefs) {
                if (pref.equalsIgnoreCase(which)) {
                    valid = true;
                    break;
                }
            }

            if (!valid)
                warn("Unknown AI value: \"" + which + "\"", scanner.getLine(), scanner.getColumn());
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("AI sets the preference \"" + getWhich() + "\" to \"" + getValue() + "\"");
        }
    }
    
    private static class FreeCommand extends TagCommand {
        FreeCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Grant independence to " + getCountry());
        }
    }
    
    private static class DisbandRebelsCommand extends ProvinceCommand {
        DisbandRebelsCommand(EUGScanner scanner) {
            super(scanner);
        }

        @Override
        protected void generateHTML(BufferedWriter out) throws IOException {
            out.write("Disband all rebels in " + getProv());
        }
    }
}
