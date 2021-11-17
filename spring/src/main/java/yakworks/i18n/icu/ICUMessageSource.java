package yakworks.i18n.icu;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import yakworks.i18n.MsgContext;
import yakworks.i18n.MsgKey;
import yakworks.i18n.MsgService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Extends the {@link org.springframework.context.MessageSource} interface in that adds util for passing
 * arguments as Map. We also add lists and some of similiar shorter helper methods
 * to {@link org.springframework.context.support.MessageSourceAccessor}
 *
 * @author Joshua Burnett (@basejump)
 * @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface ICUMessageSource extends HierarchicalMessageSource, MsgService {


    default Locale getHolderLocale() {
        return LocaleContextHolder.getLocale();
    }

    default String getMessage(MessageSourceResolvable resolvable){
        return getMessage(resolvable, getHolderLocale());
    }

}
