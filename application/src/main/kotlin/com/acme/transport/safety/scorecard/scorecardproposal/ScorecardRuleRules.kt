package com.acme.transport.safety.scorecard.scorecardproposal

/** A rule a scorecard rule must pass. Check returns its problems, or none. */
interface ScorecardRuleRule {
    fun check(item: ScorecardRule): MutableList<String>
}

data class ScorecardRuleExternalRefLengthRule(var maxLength: Int) : ScorecardRuleRule {
    override fun check(item: ScorecardRule): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardRuleRequiredExternalRefRule(var message: String) : ScorecardRuleRule {
    override fun check(item: ScorecardRule): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardRuleRequiredTitleRule(var message: String) : ScorecardRuleRule {
    override fun check(item: ScorecardRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardRuleVersionRangeRule(var min: Int, var max: Int) : ScorecardRuleRule {
    override fun check(item: ScorecardRule): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardRuleTitleLengthRule(var maxLength: Int) : ScorecardRuleRule {
    override fun check(item: ScorecardRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard rule. */
data class ScorecardRuleValidator(var rules: MutableList<ScorecardRuleRule>) {
    fun validate(item: ScorecardRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardRule>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultScorecardRuleValidator(): ScorecardRuleValidator {
    return ScorecardRuleValidator(
        mutableListOf<ScorecardRuleRule>(
            ScorecardRuleExternalRefLengthRule(3600),
            ScorecardRuleRequiredExternalRefRule("scorecard rule needs an entry"),
            ScorecardRuleRequiredTitleRule("scorecard rule needs a value"),
            ScorecardRuleVersionRangeRule(80, 255),
            ScorecardRuleTitleLengthRule(255),
        ),
    )
}
