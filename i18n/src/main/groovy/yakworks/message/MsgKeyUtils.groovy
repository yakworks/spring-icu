/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.message

import groovy.transform.CompileStatic

/**
 * Static helpers for messages
 */
@CompileStatic
class MsgKeyUtils {

    /**
     * fuzzy helper to get message key from target byt looking at it properties
     * i target has a code or fallbackMessage then it uses them.
     * look for params and msgArgs to find  the args
     */
    static MsgKey toMsgKey(Object target, String code = null) {
        if(MsgKey.isAssignableFrom(target.class)){
            return (MsgKey)target
        }
        //pull it from the keys
        Map props = target.properties
        if(props.code) {
            def args = props.params?:props.msgArgs
            return MsgKey.ofCode(props.code as String).args((args?:props) as Map)
        } else if(props.fallbackMessage) {
            return MsgKey.ofCode("__nonexistent__").args(props).fallbackMessage(props.fallbackMessage as String)
        } else {
            return null
        }
    }

}
