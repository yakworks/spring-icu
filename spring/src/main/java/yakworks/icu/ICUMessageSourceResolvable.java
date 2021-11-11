package yakworks.icu;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sister to the {@link org.springframework.context.MessageSourceResolvable} interface
 * to add the map args for ICU. Since arguments are a map then it can store the default message in a defaultMessage key
 * requiring one less prop. Also renames the arguments to params.
 */
public interface ICUMessageSourceResolvable {

    @Nullable
    String getCode();

    /**
     * Return the Map of arguments to be used to resolve this message as ICU.
     */
    @Nullable
    default Map getParams() {
        return null;
    }

}
