package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.text.normalize

/** A rule a lease return metric must pass. Check returns its problems, or none. */
interface LeaseReturnMetricRule {
    fun check(item: LeaseReturnMetric): MutableList<String>
}

data class LeaseReturnMetricPriorityRangeRule(var min: Int, var max: Int) : LeaseReturnMetricRule {
    override fun check(item: LeaseReturnMetric): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnMetricCodeLengthRule(var maxLength: Int) : LeaseReturnMetricRule {
    override fun check(item: LeaseReturnMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMetricRequiredTitleRule(var message: String) : LeaseReturnMetricRule {
    override fun check(item: LeaseReturnMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMetricAllowedRegionRule(var allowed: MutableList<String>) : LeaseReturnMetricRule {
    override fun check(item: LeaseReturnMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return metric. */
data class LeaseReturnMetricValidator(var rules: MutableList<LeaseReturnMetricRule>) {
    fun validate(item: LeaseReturnMetric): MutableList<String> {
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

    fun requireValid(item: LeaseReturnMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnMetricValidator(): LeaseReturnMetricValidator {
    return LeaseReturnMetricValidator(
        mutableListOf<LeaseReturnMetricRule>(
            LeaseReturnMetricPriorityRangeRule(16, 120),
            LeaseReturnMetricCodeLengthRule(64),
            LeaseReturnMetricRequiredTitleRule("lease return metric needs a value"),
            LeaseReturnMetricAllowedRegionRule(mutableListOf<String>("legacy", "manual", "default")),
        ),
    )
}
