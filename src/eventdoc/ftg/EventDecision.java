package eventdoc.ftg;

/**
 * Interface covering common behavior between events and decisions.
 * @author Michael
 */
public interface EventDecision extends HtmlObject {
    int getId();
    String getName(); // desc = ""
}
