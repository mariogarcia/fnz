ruleset {
    description 'SeeQuestor RuleSet'

    ruleset('rulesets/basic.xml')
    ruleset('rulesets/exceptions.xml')
    ruleset('rulesets/imports.xml')
    ruleset('rulesets/unused.xml')
    ruleset('rulesets/dry.xml')
    ruleset('rulesets/formatting.xml') {
        'ClassJavadoc' {
            enabled = false
        }
        'FileEndsWithoutNewline' {
            enabled = false
        }
    }
    ruleset('rulesets/naming.xml') {
        'MethodName' {
            doNotApplyToClassNames = '*Spec'
        }
    }
    ruleset('rulesets/convention.xml')
}