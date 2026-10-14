package com.acme.transport.emissions.emissionfactor.milestone

/** A rule a emission factor milestone must pass. Check returns its problems, or none. */
interface EmissionFactorMilestoneRule {
    fun check(item: EmissionFactorMilestone): MutableList<String>
}

data class EmissionFactorMilestoneRequiredSummaryRule(var message: String) : EmissionFactorMilestoneRule {
    override fun check(item: EmissionFactorMilestone): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorMilestoneSummaryLengthRule(var maxLength: Int) : EmissionFactorMilestoneRule {
    override fun check(item: EmissionFactorMilestone): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorMilestoneLimitRangeRule(var min: Int, var max: Int) : EmissionFactorMilestoneRule {
    override fun check(item: EmissionFactorMilestone): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a emission factor milestone. */
data class EmissionFactorMilestoneValidator(var rules: MutableList<EmissionFactorMilestoneRule>) {
    fun validate(item: EmissionFactorMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorMilestoneValidator(): EmissionFactorMilestoneValidator {
    return EmissionFactorMilestoneValidator(
        mutableListOf<EmissionFactorMilestoneRule>(
            EmissionFactorMilestoneRequiredSummaryRule("emission factor milestone needs an entry"),
            EmissionFactorMilestoneSummaryLengthRule(120),
            EmissionFactorMilestoneLimitRangeRule(64, 80),
        ),
    )
}
