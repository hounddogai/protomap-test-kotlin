package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.text.normalize

/** A rule a adaptive utilization summary must pass. Check returns its problems, or none. */
interface AdaptiveUtilizationSummaryRule {
    fun check(item: AdaptiveUtilizationSummary): MutableList<String>
}

data class AdaptiveUtilizationSummaryDiscountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveUtilizationSummaryRule {
    override fun check(item: AdaptiveUtilizationSummary): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationSummaryAllowedNameRule(
    var allowed: MutableList<String>,
) : AdaptiveUtilizationSummaryRule {
    override fun check(item: AdaptiveUtilizationSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationSummaryCodePrefixRule(var prefix: String) : AdaptiveUtilizationSummaryRule {
    override fun check(item: AdaptiveUtilizationSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive utilization summary. */
data class AdaptiveUtilizationSummaryValidator(var rules: MutableList<AdaptiveUtilizationSummaryRule>) {
    fun validate(item: AdaptiveUtilizationSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveUtilizationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveUtilizationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveUtilizationSummaryValidator(): AdaptiveUtilizationSummaryValidator {
    return AdaptiveUtilizationSummaryValidator(
        mutableListOf<AdaptiveUtilizationSummaryRule>(
            AdaptiveUtilizationSummaryDiscountRangeRule(1.6, 2005.8),
            AdaptiveUtilizationSummaryAllowedNameRule(mutableListOf<String>("primary", "lenient", "pending")),
            AdaptiveUtilizationSummaryCodePrefixRule("REF"),
        ),
    )
}
