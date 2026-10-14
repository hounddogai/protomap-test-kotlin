package com.acme.transport.fleetops.utilization.utilizationplan

/** A rule a utilization group must pass. Check returns its problems, or none. */
interface UtilizationGroupRule {
    fun check(item: UtilizationGroup): MutableList<String>
}

data class UtilizationGroupRequiredTrailerIdRule(var message: String) : UtilizationGroupRule {
    override fun check(item: UtilizationGroup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationGroupScoreRangeRule(var min: Double, var max: Double) : UtilizationGroupRule {
    override fun check(item: UtilizationGroup): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationGroupRequiredReviewerRule(var message: String) : UtilizationGroupRule {
    override fun check(item: UtilizationGroup): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization group. */
data class UtilizationGroupValidator(var rules: MutableList<UtilizationGroupRule>) {
    fun validate(item: UtilizationGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationGroupValidator(): UtilizationGroupValidator {
    return UtilizationGroupValidator(
        mutableListOf<UtilizationGroupRule>(
            UtilizationGroupRequiredTrailerIdRule("utilization group needs a value"),
            UtilizationGroupScoreRangeRule(5.6, 2582.4),
            UtilizationGroupRequiredReviewerRule("utilization group needs this field"),
        ),
    )
}
