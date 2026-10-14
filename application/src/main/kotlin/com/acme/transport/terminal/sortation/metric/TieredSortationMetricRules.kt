package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.normalize

/** A rule a tiered sortation metric must pass. Check returns its problems, or none. */
interface TieredSortationMetricRule {
    fun check(item: TieredSortationMetric): MutableList<String>
}

data class TieredSortationMetricAllowedCodeRule(var allowed: MutableList<String>) : TieredSortationMetricRule {
    override fun check(item: TieredSortationMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationMetricRatioRangeRule(var min: Double, var max: Double) : TieredSortationMetricRule {
    override fun check(item: TieredSortationMetric): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationMetricRequiredTitleRule(var message: String) : TieredSortationMetricRule {
    override fun check(item: TieredSortationMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationMetricRequiredCodeRule(var message: String) : TieredSortationMetricRule {
    override fun check(item: TieredSortationMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered sortation metric. */
data class TieredSortationMetricValidator(var rules: MutableList<TieredSortationMetricRule>) {
    fun validate(item: TieredSortationMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredSortationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationMetricValidator(): TieredSortationMetricValidator {
    return TieredSortationMetricValidator(
        mutableListOf<TieredSortationMetricRule>(
            TieredSortationMetricAllowedCodeRule(mutableListOf<String>()),
            TieredSortationMetricRatioRangeRule(3.4, 411.8),
            TieredSortationMetricRequiredTitleRule("tiered sortation metric needs an entry"),
            TieredSortationMetricRequiredCodeRule("tiered sortation metric needs this field"),
        ),
    )
}
