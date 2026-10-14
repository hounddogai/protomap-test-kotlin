package com.acme.transport.safety.scorecard.scorecardledger

/** A rule a scorecard override must pass. Check returns its problems, or none. */
interface ScorecardOverrideRule {
    fun check(item: ScorecardOverride): MutableList<String>
}

data class ScorecardOverrideQuantityRangeRule(var min: Int, var max: Int) : ScorecardOverrideRule {
    override fun check(item: ScorecardOverride): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ScorecardOverrideTitleLengthRule(var maxLength: Int) : ScorecardOverrideRule {
    override fun check(item: ScorecardOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardOverrideRequiredCodeRule(var message: String) : ScorecardOverrideRule {
    override fun check(item: ScorecardOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardOverrideGraceDaysRangeRule(var min: Int, var max: Int) : ScorecardOverrideRule {
    override fun check(item: ScorecardOverride): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scorecard override. */
data class ScorecardOverrideValidator(var rules: MutableList<ScorecardOverrideRule>) {
    fun validate(item: ScorecardOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardOverrideValidator(): ScorecardOverrideValidator {
    return ScorecardOverrideValidator(
        mutableListOf<ScorecardOverrideRule>(
            ScorecardOverrideQuantityRangeRule(3600, 3600),
            ScorecardOverrideTitleLengthRule(120),
            ScorecardOverrideRequiredCodeRule("scorecard override needs a value"),
            ScorecardOverrideGraceDaysRangeRule(32, 255),
        ),
    )
}
