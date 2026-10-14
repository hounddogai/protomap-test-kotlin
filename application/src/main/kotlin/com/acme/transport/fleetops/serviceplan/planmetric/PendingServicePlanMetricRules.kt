package com.acme.transport.fleetops.serviceplan.planmetric

/** A rule a pending service plan metric must pass. Check returns its problems, or none. */
interface PendingServicePlanMetricRule {
    fun check(item: PendingServicePlanMetric): MutableList<String>
}

data class PendingServicePlanMetricReviewerLengthRule(var maxLength: Int) : PendingServicePlanMetricRule {
    override fun check(item: PendingServicePlanMetric): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanMetricNameLengthRule(var maxLength: Int) : PendingServicePlanMetricRule {
    override fun check(item: PendingServicePlanMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanMetricWeightRangeRule(
    var min: Double,
    var max: Double,
) : PendingServicePlanMetricRule {
    override fun check(item: PendingServicePlanMetric): MutableList<String> {
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

/** Runs every rule of a pending service plan metric. */
data class PendingServicePlanMetricValidator(var rules: MutableList<PendingServicePlanMetricRule>) {
    fun validate(item: PendingServicePlanMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingServicePlanMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServicePlanMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServicePlanMetricValidator(): PendingServicePlanMetricValidator {
    return PendingServicePlanMetricValidator(
        mutableListOf<PendingServicePlanMetricRule>(
            PendingServicePlanMetricReviewerLengthRule(120),
            PendingServicePlanMetricNameLengthRule(32),
            PendingServicePlanMetricWeightRangeRule(4.2, 468.9),
        ),
    )
}
