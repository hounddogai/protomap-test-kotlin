package com.acme.transport.linehaul.backhaul.overrideitem

/** A rule a regional backhaul baseline must pass. Check returns its problems, or none. */
interface RegionalBackhaulBaselineRule {
    fun check(item: RegionalBackhaulBaseline): MutableList<String>
}

data class RegionalBackhaulBaselineDiscountRangeRule(
    var min: Double,
    var max: Double,
) : RegionalBackhaulBaselineRule {
    override fun check(item: RegionalBackhaulBaseline): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class RegionalBackhaulBaselineMaximumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalBackhaulBaselineRule {
    override fun check(item: RegionalBackhaulBaseline): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RegionalBackhaulBaselineAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalBackhaulBaselineRule {
    override fun check(item: RegionalBackhaulBaseline): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional backhaul baseline. */
data class RegionalBackhaulBaselineValidator(var rules: MutableList<RegionalBackhaulBaselineRule>) {
    fun validate(item: RegionalBackhaulBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalBackhaulBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalBackhaulBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalBackhaulBaselineValidator(): RegionalBackhaulBaselineValidator {
    return RegionalBackhaulBaselineValidator(
        mutableListOf<RegionalBackhaulBaselineRule>(
            RegionalBackhaulBaselineDiscountRangeRule(2.0, 2420.2),
            RegionalBackhaulBaselineMaximumRangeRule(9.4, 3223.1),
            RegionalBackhaulBaselineAttemptsRangeRule(32, 120),
        ),
    )
}
