package yakworks.icu;

import com.ibm.icu.text.MessageFormat;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map-based arguments implementation. Used with patterns which have named arguments
 */
@SuppressWarnings("unchecked")
public class MapMessageArgs implements ICUMessageArgs {

    Map<Object, Object> args;

    public MapMessageArgs(@Nullable Map<Object, Object> args) {
        if (args == null) args = Collections.emptyMap();
        this.args = args;
    }

    @Override
    public boolean isEmpty() {
        return args.isEmpty();
    }

    @Override
    public MapMessageArgs transform(Transformation transformation) {
        Map newArgs = new LinkedHashMap<>(args.size());
        for (Map.Entry item: args.entrySet())
            newArgs.put(item.getKey(), transformation.transform(item.getValue()));
        return new MapMessageArgs(newArgs);
    }

    @Override
    public String formatWith(MessageFormat messageFormat) {
        return messageFormat.format(args);
    }
}
