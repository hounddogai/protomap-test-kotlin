package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.text.normalize

/** A rule a adaptive dwell metric must pass. Check returns its problems, or none. */
interface AdaptiveDwellMetricRule {
    fun check(item: AdaptiveDwellMetric): MutableList<String>
}

data class AdaptiveDwellMetricRequiredDepotRule(var message: String) : AdaptiveDwellMetricRule {
    override fun check(item: AdaptiveDwellMetric): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDwellMetricEndsAtRangeRule(var min: Int, var max: Int) : AdaptiveDwellMetricRule {
    override fun check(item: AdaptiveDwellMetric): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveDwellMetricAllowedCodeRule(var allowed: MutableList<String>) : AdaptiveDwellMetricRule {
    override fun check(item: AdaptiveDwellMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dwell metric. */
data class AdaptiveDwellMetricValidator(var rules: MutableList<AdaptiveDwellMetricRule>) {
    fun validate(item: AdaptiveDwellMetric): MutableList<String> {
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

    fun requireValid(item: AdaptiveDwellMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDwellMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDwellMetricValidator(): AdaptiveDwellMetricValidator {
    return AdaptiveDwellMetricValidator(
        mutableListOf<AdaptiveDwellMetricRule>(
            AdaptiveDwellMetricRequiredDepotRule("adaptive dwell metric needs a value"),
            AdaptiveDwellMetricEndsAtRangeRule(1000, 3600),
            AdaptiveDwellMetricAllowedCodeRule(mutableListOf<String>("lenient", "legacy")),
        ),
    )
}
