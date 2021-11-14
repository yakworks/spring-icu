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

    /**
     * sets the defaultMessage key in the map, creates an arg map if none exists
     * @param defMsg
     */
    static MsgKey fallbackMessage(MsgKey msgKey, String defMsg) {
        if(defMsg != null) {
            if (msgKey.args == null && msgKey.respondsTo('setArgs')) {
                msgKey['args'] = new LinkedHashMap<>()
            }
            msgKey.args.put("fallbackMessage", defMsg)
        }
        return msgKey
    }

    static MsgKey toMsgKey(Object target, String code = null) {
        if(MsgKey.isAssignableFrom(target.class)){
            return (MsgKey)target
        }
        //pull it from the keys
        Map props = target.properties
        if(props.code) {
            def args = props.params?:props.msgArgs
            return MsgKey.of(props.code as String).args((args?:props) as Map)
        } else if(props.fallbackMessage) {
            return MsgKey.of("__nonexistent__").args(props).fallbackMessage(props.fallbackMessage as String)
        } else {
            return null
        }
    }

    static MsgKey setMessage(MsgKey msgKey, String code, List args, String defMessage = null) {
        msgKey['code'] = code
        msgKey['args'] = ( args[0] instanceof Map ? (Map)args[0] : null )
        if(defMessage) fallbackMessage(msgKey, defMessage)
        return msgKey
    }

}
