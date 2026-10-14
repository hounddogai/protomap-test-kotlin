package com.acme.transport.crewing.swap.metric

import com.acme.transport.kit.text.normalize

/** A rule a manual swap metric must pass. Check returns its problems, or none. */
interface ManualSwapMetricRule {
    fun check(item: ManualSwapMetric): MutableList<String>
}

data class ManualSwapMetricRequiredCodeRule(var message: String) : ManualSwapMetricRule {
    override fun check(item: ManualSwapMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapMetricDepotLengthRule(var maxLength: Int) : ManualSwapMetricRule {
    override fun check(item: ManualSwapMetric): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapMetricCodePrefixRule(var prefix: String) : ManualSwapMetricRule {
    override fun check(item: ManualSwapMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapMetricRequiredLabelRule(var message: String) : ManualSwapMetricRule {
    override fun check(item: ManualSwapMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapMetricAllowedLaneRule(var allowed: MutableList<String>) : ManualSwapMetricRule {
    override fun check(item: ManualSwapMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapMetricBudgetRangeRule(var min: Double, var max: Double) : ManualSwapMetricRule {
    override fun check(item: ManualSwapMetric): MutableList<String> {
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

/** Runs every rule of a manual swap metric. */
data class ManualSwapMetricValidator(var rules: MutableList<ManualSwapMetricRule>) {
    fun validate(item: ManualSwapMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualSwapMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualSwapMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultManualSwapMetricValidator(): ManualSwapMetricValidator {
    return ManualSwapMetricValidator(
        mutableListOf<ManualSwapMetricRule>(
            ManualSwapMetricRequiredCodeRule("manual swap metric needs an entry"),
            ManualSwapMetricDepotLengthRule(64),
            ManualSwapMetricCodePrefixRule("Q"),
            ManualSwapMetricRequiredLabelRule("manual swap metric needs a value"),
            ManualSwapMetricAllowedLaneRule(mutableListOf<String>("seasonal")),
            ManualSwapMetricBudgetRangeRule(1.2, 4080.9),
        ),
    )
}
