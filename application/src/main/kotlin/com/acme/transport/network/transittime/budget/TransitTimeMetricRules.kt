package com.acme.transport.network.transittime.budget

/** A rule a transit time metric must pass. Check returns its problems, or none. */
interface TransitTimeMetricRule {
    fun check(item: TransitTimeMetric): MutableList<String>
}

data class TransitTimeMetricRegionLengthRule(var maxLength: Int) : TransitTimeMetricRule {
    override fun check(item: TransitTimeMetric): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMetricPriorityRangeRule(var min: Int, var max: Int) : TransitTimeMetricRule {
    override fun check(item: TransitTimeMetric): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeMetricVersionRangeRule(var min: Int, var max: Int) : TransitTimeMetricRule {
    override fun check(item: TransitTimeMetric): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a transit time metric. */
data class TransitTimeMetricValidator(var rules: MutableList<TransitTimeMetricRule>) {
    fun validate(item: TransitTimeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeMetricValidator(): TransitTimeMetricValidator {
    return TransitTimeMetricValidator(
        mutableListOf<TransitTimeMetricRule>(
            TransitTimeMetricRegionLengthRule(16),
            TransitTimeMetricPriorityRangeRule(120, 3600),
            TransitTimeMetricVersionRangeRule(64, 64),
        ),
    )
}
