package fnz.control

import fnz.test.AstBaseSpec
import spock.lang.Unroll
import org.codehaus.groovy.control.CompilePhase

class UnlessSpec extends AstBaseSpec {

    void 'simple unless example'() {
         given: 'an instance of the example'
         def instance =
             getClassToTestForPhase(UnlessAst, CompilePhase.CANONICALIZATION)
               .newInstance()
         when: 'executing a unless statement'
         Integer result = instance.basicUnlessExample(sample)
         then: 'we should get the proper result'
         result == expected
         where:
         sample | expected
         1|4
         0|null
         -1|null
         2|5
         3|6
    }

    @Unroll
    void 'nested unless expressions'() {
     given: 'an instance of the example'
         def instance =
             getClassToTestForPhase(UnlessAst, CompilePhase.CANONICALIZATION)
               .newInstance()
         when: 'executing a unless statement'
         Integer result = instance.nestedUnlessExample(sample)
         then: 'we should get the proper result'
         result == expected
         where:
         sample | expected
         1|4
         2|5
         0|null
         -1|null
         3|null

    }

}
