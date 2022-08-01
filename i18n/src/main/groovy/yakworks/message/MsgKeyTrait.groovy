/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.message

import groovy.transform.CompileStatic

/**
 * Trait implementation for MsgKey to allow easy ability to make anything in groovy a MsgKey
 */
@CompileStatic
trait MsgKeyTrait<E> implements MsgKey  {

    String code
    MsgArgs args
    String fallbackMessage

    E code(String code){ this.code = code; return (E)this;}

    E args(Object args){
        this.args = MsgArgs.of(args)
        return (E)this
    }

    E args(MsgArgs args){
        this.args = args
        return (E)this
    }

    /**
     * If one is set then return it,
     * if not it looks at args and if its a map then returns the defaultMessage key if it exists
     */
    @Override
    String getFallbackMessage(){
        return (fallbackMessage != null) ? fallbackMessage : getArgs().getFallbackMessage();
    }

    E fallbackMessage(String defMsg){ fallbackMessage = defMsg; return (E)this;}
}
