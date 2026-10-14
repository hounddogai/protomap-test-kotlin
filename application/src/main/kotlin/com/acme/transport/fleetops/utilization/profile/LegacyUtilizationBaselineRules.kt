package com.acme.transport.fleetops.utilization.profile

/** A rule a legacy utilization baseline must pass. Check returns its problems, or none. */
interface LegacyUtilizationBaselineRule {
    fun check(item: LegacyUtilizationBaseline): MutableList<String>
}

data class LegacyUtilizationBaselineRequiredLaneRule(var message: String) : LegacyUtilizationBaselineRule {
    override fun check(item: LegacyUtilizationBaseline): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationBaselineMarginRangeRule(
    var min: Double,
    var max: Double,
) : LegacyUtilizationBaselineRule {
    override fun check(item: LegacyUtilizationBaseline): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class LegacyUtilizationBaselineCodePrefixRule(var prefix: String) : LegacyUtilizationBaselineRule {
    override fun check(item: LegacyUtilizationBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationBaselineReviewerLengthRule(var maxLength: Int) : LegacyUtilizationBaselineRule {
    override fun check(item: LegacyUtilizationBaseline): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationBaselineTitleLengthRule(var maxLength: Int) : LegacyUtilizationBaselineRule {
    override fun check(item: LegacyUtilizationBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy utilization baseline. */
data class LegacyUtilizationBaselineValidator(var rules: MutableList<LegacyUtilizationBaselineRule>) {
    fun validate(item: LegacyUtilizationBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyUtilizationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyUtilizationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyUtilizationBaselineValidator(): LegacyUtilizationBaselineValidator {
    return LegacyUtilizationBaselineValidator(
        mutableListOf<LegacyUtilizationBaselineRule>(
            LegacyUtilizationBaselineRequiredLaneRule("legacy utilization baseline needs a value"),
            LegacyUtilizationBaselineMarginRangeRule(4.5, 2082.7),
            LegacyUtilizationBaselineCodePrefixRule("REF"),
            LegacyUtilizationBaselineReviewerLengthRule(64),
            LegacyUtilizationBaselineTitleLengthRule(32),
        ),
    )
}
