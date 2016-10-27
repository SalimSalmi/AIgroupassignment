package ai2016;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by archah on 27/10/2016.
 */
public class NegotiationLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return record.getMessage() + "\n";
    }
}
