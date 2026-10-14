package com.acme.transport.fleetops.utilization.utilizationvariance

/** A rule a legacy utilization variance must pass. Check returns its problems, or none. */
interface LegacyUtilizationVarianceRule {
    fun check(item: LegacyUtilizationVariance): MutableList<String>
}

data class LegacyUtilizationVarianceRequiredReviewerRule(var message: String) : LegacyUtilizationVarianceRule {
    override fun check(item: LegacyUtilizationVariance): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationVarianceDepotLengthRule(var maxLength: Int) : LegacyUtilizationVarianceRule {
    override fun check(item: LegacyUtilizationVariance): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationVarianceBudgetRangeRule(
    var min: Double,
    var max: Double,
) : LegacyUtilizationVarianceRule {
    override fun check(item: LegacyUtilizationVariance): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class LegacyUtilizationVarianceEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyUtilizationVarianceRule {
    override fun check(item: LegacyUtilizationVariance): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class LegacyUtilizationVarianceRequiredTitleRule(var message: String) : LegacyUtilizationVarianceRule {
    override fun check(item: LegacyUtilizationVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationVarianceTitleLengthRule(var maxLength: Int) : LegacyUtilizationVarianceRule {
    override fun check(item: LegacyUtilizationVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy utilization variance. */
data class LegacyUtilizationVarianceValidator(var rules: MutableList<LegacyUtilizationVarianceRule>) {
    fun validate(item: LegacyUtilizationVariance): MutableList<String> {
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

    fun requireValid(item: LegacyUtilizationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyUtilizationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyUtilizationVarianceValidator(): LegacyUtilizationVarianceValidator {
    return LegacyUtilizationVarianceValidator(
        mutableListOf<LegacyUtilizationVarianceRule>(
            LegacyUtilizationVarianceRequiredReviewerRule("legacy utilization variance needs this field"),
            LegacyUtilizationVarianceDepotLengthRule(1000),
            LegacyUtilizationVarianceBudgetRangeRule(0.1, 2186.0),
            LegacyUtilizationVarianceEndsAtRangeRule(64, 1000),
            LegacyUtilizationVarianceRequiredTitleRule("legacy utilization variance needs this field"),
            LegacyUtilizationVarianceTitleLengthRule(255),
        ),
    )
}
