ruleset {
    description 'SeeQuestor RuleSet'

    ruleset('rulesets/basic.xml')
    ruleset('rulesets/exceptions.xml')
    ruleset('rulesets/imports.xml')
    ruleset('rulesets/unused.xml') {
        'UnusedMethodParameter' {
            doNotApplyToClassNames = '*ExtensionModule'
        }
    }

    ruleset('rulesets/dry.xml') {
        'DuplicateNumberLiteral' {
            doNotApplyToClassNames = '*Spec,*SpecExample'
        }
        'DuplicateStringLiteral' {
            doNotApplyToClassNames = '*Spec,*SpecExample'
        }
        'DuplicateMapLiteral' {
            doNotApplyToClassNames = '*Spec,*SpecExample'
        }
        'DuplicateListLiteral' {
            doNotApplyToClassNames = '*Spec,*SpecExample'
        }
    }

    ruleset('rulesets/formatting.xml') {
        'SpaceBeforeOpeningBrace' {
            doNotApplyToClassNames = '*Spec,*SpecExample'
        }
        'ClassJavadoc' {
            enabled = false
        }
        'FileEndsWithoutNewline' {
            enabled = false
        }
    }

    ruleset('rulesets/naming.xml') {
        'MethodName' {
            doNotApplyToClassNames = '*Spec,*ExtensionModule'
        }
        'VariableName' {
            doNotApplyToClassNames = '*Spec,*SpecExample'
        }
        'FactoryMethodName' {
            doNotApplyToClassNames = '*Spec,*SpecExample'
        }
    }

    ruleset('rulesets/convention.xml')

}
