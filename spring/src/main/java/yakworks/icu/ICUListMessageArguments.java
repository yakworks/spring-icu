package com.transferwise.icu;

import com.ibm.icu.text.MessageFormat;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default, List-based arguments implementation. Used with patterns which have numbered arguments
 */
public class ICUListMessageArguments implements ICUMessageArguments {

    private List<Object> args;

    public ICUListMessageArguments(@Nullable List<Object> args) {
        if (args == null) args = Collections.emptyList();
        this.args = args;
    }

    public ICUListMessageArguments(@Nullable Object[] args) {
        if (args == null) args = new Object[0];
        this.args = Arrays.asList(args);
    }

    @Override
    public boolean isEmpty() {
        return args.isEmpty();
    }

    @Override
    public ICUListMessageArguments transform(Transformation transformation) {
        List<Object> newArgs = new ArrayList<Object>(args.size());
        for (Object item : args)
            newArgs.add(transformation.transform(item));
        return new ICUListMessageArguments(newArgs);
    }

    @Override
    public String formatWith(MessageFormat messageFormat) {
        return messageFormat.format(toArray());
    }

    public Object[] toArray() {
        return args.toArray(new Object[args.size()]);
    }
}
