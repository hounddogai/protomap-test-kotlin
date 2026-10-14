package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize

/** A rule a pending capacity metric must pass. Check returns its problems, or none. */
interface PendingCapacityMetricRule {
    fun check(item: PendingCapacityMetric): MutableList<String>
}

data class PendingCapacityMetricTitleLengthRule(var maxLength: Int) : PendingCapacityMetricRule {
    override fun check(item: PendingCapacityMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityMetricRequiredCodeRule(var message: String) : PendingCapacityMetricRule {
    override fun check(item: PendingCapacityMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityMetricDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : PendingCapacityMetricRule {
    override fun check(item: PendingCapacityMetric): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class PendingCapacityMetricAllowedCodeRule(var allowed: MutableList<String>) : PendingCapacityMetricRule {
    override fun check(item: PendingCapacityMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending capacity metric. */
data class PendingCapacityMetricValidator(var rules: MutableList<PendingCapacityMetricRule>) {
    fun validate(item: PendingCapacityMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCapacityMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCapacityMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCapacityMetricValidator(): PendingCapacityMetricValidator {
    return PendingCapacityMetricValidator(
        mutableListOf<PendingCapacityMetricRule>(
            PendingCapacityMetricTitleLengthRule(16),
            PendingCapacityMetricRequiredCodeRule("pending capacity metric needs this field"),
            PendingCapacityMetricDistanceKmRangeRule(0.3, 2639.8),
            PendingCapacityMetricAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
