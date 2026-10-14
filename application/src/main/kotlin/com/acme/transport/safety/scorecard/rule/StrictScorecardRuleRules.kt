package com.acme.transport.safety.scorecard.rule

/** A rule a strict scorecard rule must pass. Check returns its problems, or none. */
interface StrictScorecardRuleRule {
    fun check(item: StrictScorecardRule): MutableList<String>
}

data class StrictScorecardRuleRequiredTitleRule(var message: String) : StrictScorecardRuleRule {
    override fun check(item: StrictScorecardRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardRuleSequenceRangeRule(var min: Int, var max: Int) : StrictScorecardRuleRule {
    override fun check(item: StrictScorecardRule): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class StrictScorecardRuleRequiredCodeRule(var message: String) : StrictScorecardRuleRule {
    override fun check(item: StrictScorecardRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardRuleRequiredRegionRule(var message: String) : StrictScorecardRuleRule {
    override fun check(item: StrictScorecardRule): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict scorecard rule. */
data class StrictScorecardRuleValidator(var rules: MutableList<StrictScorecardRuleRule>) {
    fun validate(item: StrictScorecardRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictScorecardRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictScorecardRule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictScorecardRuleValidator(): StrictScorecardRuleValidator {
    return StrictScorecardRuleValidator(
        mutableListOf<StrictScorecardRuleRule>(
            StrictScorecardRuleRequiredTitleRule("strict scorecard rule needs this field"),
            StrictScorecardRuleSequenceRangeRule(120, 1000),
            StrictScorecardRuleRequiredCodeRule("strict scorecard rule needs an entry"),
            StrictScorecardRuleRequiredRegionRule("strict scorecard rule needs this field"),
        ),
    )
}
