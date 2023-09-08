/*
 * Action.java
 *
 * Created on Mar 15, 2008, 6:00:00 PM
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
class Action implements HtmlObject {

    private String name;
    private int aiChance = -1;
    private Trigger trigger;
    private List<Command> commands;

    Action(EUGScanner scanner) {
        commands = new ArrayList<Command>();

        if (scanner.nextToken() != TokenType.LBRACE) {
            warn("Expected '{'", scanner.getLine(), scanner.getColumn());
        }

        parseLoop: while (true) {
            switch (scanner.nextToken()) {
                case IDENT:
                    String ident = scanner.lastStr().toLowerCase();

                    if (ident.equals("name")) {
                        scanner.nextToken();
                        name = scanner.lastStr();
                    } else if (ident.equals("command")) {
                        commands.add(new Command(scanner));
                    } else if (ident.equals("ai_chance")) {
                        scanner.nextToken();
                        aiChance = Integer.parseInt(scanner.lastStr());
                    } else if (ident.equals("trigger")) {
                        trigger = Trigger.parseTrigger(scanner);
                    }
                    break;
                case RBRACE:
                    break parseLoop;
                default:
                    warn("Unexpected token type in action: " + scanner.lastToken(),
                            scanner.getLine(), scanner.getColumn());
                }
        }
    }
    
    List<Command> getCommands() {
        return commands;
    }

    int getAIChance() {
        return aiChance;
    }
    
    @Override
    public void generateHTML(BufferedWriter out) throws IOException {
        generateHTML(out, '0', -1);
    }
    
    public void generateHTML(BufferedWriter out, char action, int totalChance) throws IOException {
        if (action != '0') {
            out.write("<h4 class=\"action_title\">");
            out.write(action);
            out.write(". ");
            out.write(Text.getText(name));
            out.write("</h4>");
            out.newLine();
            if (aiChance != -1) {
                out.write("AI chance: " + (int)(((double)aiChance/totalChance)*100) + "%");
            }
        }

        if (trigger != null) {
            out.write("<h4 class=\"action_conditions\">Conditions</h4>");
            out.newLine();
            trigger.generateHTML(out);

            out.write("<h4 class=\"action_effects\">Effects</h4>");
            out.newLine();
        }

        out.write("<ul class=\"action\">");
        out.newLine();
        for (Command command : commands) {
            if (command.isValid()){
                out.write("<li>");
                command.generateHTML(out);
                out.write("</li>");
                out.newLine();
            }
        }
        out.write("</ul>");
        out.newLine();
    }

    private static void warn(String msg, int line, int column) {
        System.out.print("Line " + line + ", column " + column + ": ");
        System.out.println(msg);
    }
}
