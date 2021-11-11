package yakworks.icu

import spock.lang.Specification

class SanityCheckSpec extends Specification {

    void testEncryption(){
        expect:
        def foo = 'bar'
        foo == 'bar'
    }

}
