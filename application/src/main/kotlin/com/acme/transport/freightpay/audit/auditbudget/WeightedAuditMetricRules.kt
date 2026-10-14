package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.text.normalize

/** A rule a weighted audit metric must pass. Check returns its problems, or none. */
interface WeightedAuditMetricRule {
    fun check(item: WeightedAuditMetric): MutableList<String>
}

data class WeightedAuditMetricRequiredLabelRule(var message: String) : WeightedAuditMetricRule {
    override fun check(item: WeightedAuditMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditMetricAllowedCodeRule(var allowed: MutableList<String>) : WeightedAuditMetricRule {
    override fun check(item: WeightedAuditMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditMetricMarginRangeRule(var min: Double, var max: Double) : WeightedAuditMetricRule {
    override fun check(item: WeightedAuditMetric): MutableList<String> {
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

data class WeightedAuditMetricLabelLengthRule(var maxLength: Int) : WeightedAuditMetricRule {
    override fun check(item: WeightedAuditMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted audit metric. */
data class WeightedAuditMetricValidator(var rules: MutableList<WeightedAuditMetricRule>) {
    fun validate(item: WeightedAuditMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAuditMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAuditMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAuditMetricValidator(): WeightedAuditMetricValidator {
    return WeightedAuditMetricValidator(
        mutableListOf<WeightedAuditMetricRule>(
            WeightedAuditMetricRequiredLabelRule("weighted audit metric needs a value"),
            WeightedAuditMetricAllowedCodeRule(mutableListOf<String>("tiered", "strict", "strict")),
            WeightedAuditMetricMarginRangeRule(5.9, 2617.1),
            WeightedAuditMetricLabelLengthRule(16),
        ),
    )
}
