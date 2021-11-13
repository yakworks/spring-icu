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

    E code(String v) { code = v; return (E)this;}
    E args(Map v) { params = v; return (E)this;}

}
