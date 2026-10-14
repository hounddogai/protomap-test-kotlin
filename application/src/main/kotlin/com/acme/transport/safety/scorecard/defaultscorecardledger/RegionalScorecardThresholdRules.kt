package com.acme.transport.safety.scorecard.defaultscorecardledger

/** A rule a regional scorecard threshold must pass. Check returns its problems, or none. */
interface RegionalScorecardThresholdRule {
    fun check(item: RegionalScorecardThreshold): MutableList<String>
}

data class RegionalScorecardThresholdRequiredNameRule(var message: String) : RegionalScorecardThresholdRule {
    override fun check(item: RegionalScorecardThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalScorecardThresholdRequiredCodeRule(var message: String) : RegionalScorecardThresholdRule {
    override fun check(item: RegionalScorecardThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalScorecardThresholdRequiredNotesRule(var message: String) : RegionalScorecardThresholdRule {
    override fun check(item: RegionalScorecardThreshold): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalScorecardThresholdNameLengthRule(var maxLength: Int) : RegionalScorecardThresholdRule {
    override fun check(item: RegionalScorecardThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalScorecardThresholdWeightRangeRule(
    var min: Double,
    var max: Double,
) : RegionalScorecardThresholdRule {
    override fun check(item: RegionalScorecardThreshold): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional scorecard threshold. */
data class RegionalScorecardThresholdValidator(var rules: MutableList<RegionalScorecardThresholdRule>) {
    fun validate(item: RegionalScorecardThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalScorecardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalScorecardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalScorecardThresholdValidator(): RegionalScorecardThresholdValidator {
    return RegionalScorecardThresholdValidator(
        mutableListOf<RegionalScorecardThresholdRule>(
            RegionalScorecardThresholdRequiredNameRule("regional scorecard threshold needs an entry"),
            RegionalScorecardThresholdRequiredCodeRule("regional scorecard threshold needs this field"),
            RegionalScorecardThresholdRequiredNotesRule("regional scorecard threshold needs this field"),
            RegionalScorecardThresholdNameLengthRule(16),
            RegionalScorecardThresholdWeightRangeRule(8.2, 4428.2),
        ),
    )
}
