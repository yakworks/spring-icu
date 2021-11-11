/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.icu

import groovy.transform.CompileStatic

import org.springframework.context.MessageSourceResolvable

/**
 * Trait implementation for a messageSource for the ICU
 * does not implement the {@link MessageSourceResolvable} but has method to return one if needed
 *
 * This differs in that its simplified and skinnied down
 * - only one code instead of array
 * - message arguments are params and are not an array but based on keys in map
 * - if a list or array is passed then it looks at the first element to see if its a map and uses that
 * - no default message prop but one can be passed into the map with the key 'defaultMessage'
 */
@CompileStatic
trait MsgSource {

    String code
    Map params
    // String defaultMessage

    /**
     * Sets this from a MessageSourceResolvable
     */
    void setMessage(MessageSourceResolvable rsv) {
        String singleCode = (rsv.codes ? rsv.codes[rsv.codes.length - 1] : null)
        setMessage(singleCode, rsv.arguments?.toList(), rsv.defaultMessage)
    }

    void setMessage(String code, List args, String defaultMessage = null) {
        this.code = code
        setParams( args[0] instanceof Map ? (Map)args[0] : null )
        if(defaultMessage && !getParams().containsKey('defaultMessage')) getParams()['defaultMessage'] = defaultMessage
    }

}
