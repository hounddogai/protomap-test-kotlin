package com.acme.transport.fleetops.tire.claim

/** A rule a tire metric must pass. Check returns its problems, or none. */
interface TireMetricRule {
    fun check(item: TireMetric): MutableList<String>
}

data class TireMetricCodeLengthRule(var maxLength: Int) : TireMetricRule {
    override fun check(item: TireMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireMetricExpiresAtRangeRule(var min: Int, var max: Int) : TireMetricRule {
    override fun check(item: TireMetric): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class TireMetricMarginRangeRule(var min: Double, var max: Double) : TireMetricRule {
    override fun check(item: TireMetric): MutableList<String> {
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

/** Runs every rule of a tire metric. */
data class TireMetricValidator(var rules: MutableList<TireMetricRule>) {
    fun validate(item: TireMetric): MutableList<String> {
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

    fun requireValid(item: TireMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTireMetricValidator(): TireMetricValidator {
    return TireMetricValidator(
        mutableListOf<TireMetricRule>(
            TireMetricCodeLengthRule(80),
            TireMetricExpiresAtRangeRule(80, 1000),
            TireMetricMarginRangeRule(9.4, 1601.8),
        ),
    )
}
