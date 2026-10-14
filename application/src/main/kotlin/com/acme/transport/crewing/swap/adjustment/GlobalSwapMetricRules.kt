package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a global swap metric must pass. Check returns its problems, or none. */
interface GlobalSwapMetricRule {
    fun check(item: GlobalSwapMetric): MutableList<String>
}

data class GlobalSwapMetricWeightRangeRule(var min: Double, var max: Double) : GlobalSwapMetricRule {
    override fun check(item: GlobalSwapMetric): MutableList<String> {
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

data class GlobalSwapMetricRequiredLaneRule(var message: String) : GlobalSwapMetricRule {
    override fun check(item: GlobalSwapMetric): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapMetricAllowedCodeRule(var allowed: MutableList<String>) : GlobalSwapMetricRule {
    override fun check(item: GlobalSwapMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global swap metric. */
data class GlobalSwapMetricValidator(var rules: MutableList<GlobalSwapMetricRule>) {
    fun validate(item: GlobalSwapMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalSwapMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalSwapMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalSwapMetricValidator(): GlobalSwapMetricValidator {
    return GlobalSwapMetricValidator(
        mutableListOf<GlobalSwapMetricRule>(
            GlobalSwapMetricWeightRangeRule(4.7, 3448.3),
            GlobalSwapMetricRequiredLaneRule("global swap metric needs a value"),
            GlobalSwapMetricAllowedCodeRule(mutableListOf<String>("rolling", "global", "regional")),
        ),
    )
}
