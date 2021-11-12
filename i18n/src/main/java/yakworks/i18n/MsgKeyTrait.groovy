/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n

import groovy.transform.CompileStatic

/**
 * Trait implementation for MsgKey
 */
@CompileStatic
trait MsgKeyTrait<E> implements MsgKey {

    String code
    Map params

    void setMessage(String code, List args, String defaultMessage = null) {
        this.code = code
        setParams( args[0] instanceof Map ? (Map)args[0] : null )
        if(defaultMessage && !getParams().containsKey('defaultMessage')) getParams()['defaultMessage'] = defaultMessage
    }

    E params(Map v) { params = v; return (E)this;}

    /**
     * sets the defaultMessage key in the map, creates an arg map if none exists
     * @param defMsg
     * @return
     */
    E defaultMessage(String defMsg) {
        if(params == null) params = new LinkedHashMap<>();
        params.put("defaultMessage", defMsg);
        return (E)this;
    }

}
