package com.acme.transport.lastmile.attempt.attemptmetric

/** A rule a regional attempt metric must pass. Check returns its problems, or none. */
interface RegionalAttemptMetricRule {
    fun check(item: RegionalAttemptMetric): MutableList<String>
}

data class RegionalAttemptMetricCostRangeRule(var min: Double, var max: Double) : RegionalAttemptMetricRule {
    override fun check(item: RegionalAttemptMetric): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class RegionalAttemptMetricDiscountRangeRule(
    var min: Double,
    var max: Double,
) : RegionalAttemptMetricRule {
    override fun check(item: RegionalAttemptMetric): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAttemptMetricRequiredUnitRule(var message: String) : RegionalAttemptMetricRule {
    override fun check(item: RegionalAttemptMetric): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional attempt metric. */
data class RegionalAttemptMetricValidator(var rules: MutableList<RegionalAttemptMetricRule>) {
    fun validate(item: RegionalAttemptMetric): MutableList<String> {
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

    fun requireValid(item: RegionalAttemptMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAttemptMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAttemptMetricValidator(): RegionalAttemptMetricValidator {
    return RegionalAttemptMetricValidator(
        mutableListOf<RegionalAttemptMetricRule>(
            RegionalAttemptMetricCostRangeRule(4.5, 3518.6),
            RegionalAttemptMetricDiscountRangeRule(6.7, 884.5),
            RegionalAttemptMetricRequiredUnitRule("regional attempt metric needs an entry"),
        ),
    )
}
