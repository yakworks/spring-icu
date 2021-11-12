package yakworks.i18n

import groovy.transform.CompileStatic

/**
 * Trait that that say an object can be a MessageSource
 * Think of it like ToString but for messages.properties
 */
@CompileStatic
trait ToMsgKey {

    MsgKey toMsgKey(String code = null) {
        if(MsgKey.isAssignableFrom(this.class)){
            return (MsgKey)this
        }
        //pull it from the keys
        Map props = this.properties
        if(props.code) {
            def args = props.params?:props.msgArgs
            return MsgKey.of(props.code as String, (args?:props) as Map)
        } else if(props.defaultMessage) {
            return MsgKey.ofDefault(props, props.defaultMessage as String)
        }
    }

}
