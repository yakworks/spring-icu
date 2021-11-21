/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n

import groovy.transform.CompileStatic

/**
 * Static helpers for messages
 */
@CompileStatic
class MsgKeyUtils {

    // static MsgKey addArg(MsgKey msgKey, String key, Object val) {
    //     if(val != null) {
    //         if (msgKey.args == null && msgKey.respondsTo('setArgs')) {
    //             msgKey['args'] = new LinkedHashMap<>()
    //         }
    //         msgKey.args.put("defaultMessage", defMsg)
    //     }
    //     return msgKey
    // }

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
