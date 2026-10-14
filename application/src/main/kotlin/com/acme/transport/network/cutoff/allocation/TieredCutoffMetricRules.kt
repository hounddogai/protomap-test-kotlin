package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.normalize

/** A rule a tiered cutoff metric must pass. Check returns its problems, or none. */
interface TieredCutoffMetricRule {
    fun check(item: TieredCutoffMetric): MutableList<String>
}

data class TieredCutoffMetricRequiredLabelRule(var message: String) : TieredCutoffMetricRule {
    override fun check(item: TieredCutoffMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffMetricAllowedLabelRule(var allowed: MutableList<String>) : TieredCutoffMetricRule {
    override fun check(item: TieredCutoffMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffMetricAmountRangeRule(var min: Double, var max: Double) : TieredCutoffMetricRule {
    override fun check(item: TieredCutoffMetric): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cutoff metric. */
data class TieredCutoffMetricValidator(var rules: MutableList<TieredCutoffMetricRule>) {
    fun validate(item: TieredCutoffMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCutoffMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCutoffMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCutoffMetricValidator(): TieredCutoffMetricValidator {
    return TieredCutoffMetricValidator(
        mutableListOf<TieredCutoffMetricRule>(
            TieredCutoffMetricRequiredLabelRule("tiered cutoff metric needs an entry"),
            TieredCutoffMetricAllowedLabelRule(mutableListOf<String>("capped", "fallback", "legacy")),
            TieredCutoffMetricAmountRangeRule(3.7, 4748.5),
        ),
    )
}
