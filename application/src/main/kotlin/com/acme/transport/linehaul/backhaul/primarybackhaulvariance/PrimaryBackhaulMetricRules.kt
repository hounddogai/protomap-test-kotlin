package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.normalize

/** A rule a primary backhaul metric must pass. Check returns its problems, or none. */
interface PrimaryBackhaulMetricRule {
    fun check(item: PrimaryBackhaulMetric): MutableList<String>
}

data class PrimaryBackhaulMetricLabelLengthRule(var maxLength: Int) : PrimaryBackhaulMetricRule {
    override fun check(item: PrimaryBackhaulMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMetricAllowedLabelRule(var allowed: MutableList<String>) : PrimaryBackhaulMetricRule {
    override fun check(item: PrimaryBackhaulMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMetricAmountRangeRule(var min: Double, var max: Double) : PrimaryBackhaulMetricRule {
    override fun check(item: PrimaryBackhaulMetric): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMetricCreatedAtRangeRule(var min: Int, var max: Int) : PrimaryBackhaulMetricRule {
    override fun check(item: PrimaryBackhaulMetric): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMetricRequiredCodeRule(var message: String) : PrimaryBackhaulMetricRule {
    override fun check(item: PrimaryBackhaulMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMetricCodeLengthRule(var maxLength: Int) : PrimaryBackhaulMetricRule {
    override fun check(item: PrimaryBackhaulMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary backhaul metric. */
data class PrimaryBackhaulMetricValidator(var rules: MutableList<PrimaryBackhaulMetricRule>) {
    fun validate(item: PrimaryBackhaulMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryBackhaulMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBackhaulMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBackhaulMetricValidator(): PrimaryBackhaulMetricValidator {
    return PrimaryBackhaulMetricValidator(
        mutableListOf<PrimaryBackhaulMetricRule>(
            PrimaryBackhaulMetricLabelLengthRule(32),
            PrimaryBackhaulMetricAllowedLabelRule(mutableListOf<String>("default")),
            PrimaryBackhaulMetricAmountRangeRule(1.4, 3856.8),
            PrimaryBackhaulMetricCreatedAtRangeRule(255, 1000),
            PrimaryBackhaulMetricRequiredCodeRule("primary backhaul metric needs a value"),
            PrimaryBackhaulMetricCodeLengthRule(120),
        ),
    )
}
