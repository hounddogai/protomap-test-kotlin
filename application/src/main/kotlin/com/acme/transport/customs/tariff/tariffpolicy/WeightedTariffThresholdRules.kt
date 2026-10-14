package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize

/** A rule a weighted tariff threshold must pass. Check returns its problems, or none. */
interface WeightedTariffThresholdRule {
    fun check(item: WeightedTariffThreshold): MutableList<String>
}

data class WeightedTariffThresholdAllowedLabelRule(
    var allowed: MutableList<String>,
) : WeightedTariffThresholdRule {
    override fun check(item: WeightedTariffThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffThresholdLabelLengthRule(var maxLength: Int) : WeightedTariffThresholdRule {
    override fun check(item: WeightedTariffThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffThresholdCodePrefixRule(var prefix: String) : WeightedTariffThresholdRule {
    override fun check(item: WeightedTariffThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffThresholdStartsAtRangeRule(var min: Int, var max: Int) : WeightedTariffThresholdRule {
    override fun check(item: WeightedTariffThreshold): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted tariff threshold. */
data class WeightedTariffThresholdValidator(var rules: MutableList<WeightedTariffThresholdRule>) {
    fun validate(item: WeightedTariffThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTariffThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTariffThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTariffThresholdValidator(): WeightedTariffThresholdValidator {
    return WeightedTariffThresholdValidator(
        mutableListOf<WeightedTariffThresholdRule>(
            WeightedTariffThresholdAllowedLabelRule(mutableListOf<String>("batched", "batched", "pending")),
            WeightedTariffThresholdLabelLengthRule(120),
            WeightedTariffThresholdCodePrefixRule("X"),
            WeightedTariffThresholdStartsAtRangeRule(255, 3600),
        ),
    )
}
