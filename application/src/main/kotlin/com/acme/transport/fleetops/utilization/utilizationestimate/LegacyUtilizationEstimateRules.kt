package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.normalize

/** A rule a legacy utilization estimate must pass. Check returns its problems, or none. */
interface LegacyUtilizationEstimateRule {
    fun check(item: LegacyUtilizationEstimate): MutableList<String>
}

data class LegacyUtilizationEstimateEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyUtilizationEstimateRule {
    override fun check(item: LegacyUtilizationEstimate): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationEstimateRequiredTitleRule(var message: String) : LegacyUtilizationEstimateRule {
    override fun check(item: LegacyUtilizationEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationEstimateAllowedTitleRule(
    var allowed: MutableList<String>,
) : LegacyUtilizationEstimateRule {
    override fun check(item: LegacyUtilizationEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationEstimateDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyUtilizationEstimateRule {
    override fun check(item: LegacyUtilizationEstimate): MutableList<String> {
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

/** Runs every rule of a legacy utilization estimate. */
data class LegacyUtilizationEstimateValidator(var rules: MutableList<LegacyUtilizationEstimateRule>) {
    fun validate(item: LegacyUtilizationEstimate): MutableList<String> {
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

    fun requireValid(item: LegacyUtilizationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyUtilizationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyUtilizationEstimateValidator(): LegacyUtilizationEstimateValidator {
    return LegacyUtilizationEstimateValidator(
        mutableListOf<LegacyUtilizationEstimateRule>(
            LegacyUtilizationEstimateEndsAtRangeRule(3600, 3600),
            LegacyUtilizationEstimateRequiredTitleRule("legacy utilization estimate needs this field"),
            LegacyUtilizationEstimateAllowedTitleRule(mutableListOf<String>("adaptive", "pending", "scheduled")),
            LegacyUtilizationEstimateDiscountRangeRule(5.2, 294.3),
        ),
    )
}
