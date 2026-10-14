package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.normalize

/** A rule a adaptive utilization rollup must pass. Check returns its problems, or none. */
interface AdaptiveUtilizationRollupRule {
    fun check(item: AdaptiveUtilizationRollup): MutableList<String>
}

data class AdaptiveUtilizationRollupRequiredSegmentRule(var message: String) : AdaptiveUtilizationRollupRule {
    override fun check(item: AdaptiveUtilizationRollup): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationRollupAllowedNameRule(
    var allowed: MutableList<String>,
) : AdaptiveUtilizationRollupRule {
    override fun check(item: AdaptiveUtilizationRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationRollupRequiredCodeRule(var message: String) : AdaptiveUtilizationRollupRule {
    override fun check(item: AdaptiveUtilizationRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationRollupSegmentLengthRule(var maxLength: Int) : AdaptiveUtilizationRollupRule {
    override fun check(item: AdaptiveUtilizationRollup): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationRollupCodePrefixRule(var prefix: String) : AdaptiveUtilizationRollupRule {
    override fun check(item: AdaptiveUtilizationRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationRollupQuantityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveUtilizationRollupRule {
    override fun check(item: AdaptiveUtilizationRollup): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive utilization rollup. */
data class AdaptiveUtilizationRollupValidator(var rules: MutableList<AdaptiveUtilizationRollupRule>) {
    fun validate(item: AdaptiveUtilizationRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveUtilizationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveUtilizationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveUtilizationRollupValidator(): AdaptiveUtilizationRollupValidator {
    return AdaptiveUtilizationRollupValidator(
        mutableListOf<AdaptiveUtilizationRollupRule>(
            AdaptiveUtilizationRollupRequiredSegmentRule("adaptive utilization rollup needs a value"),
            AdaptiveUtilizationRollupAllowedNameRule(mutableListOf<String>()),
            AdaptiveUtilizationRollupRequiredCodeRule("adaptive utilization rollup needs an entry"),
            AdaptiveUtilizationRollupSegmentLengthRule(32),
            AdaptiveUtilizationRollupCodePrefixRule("X"),
            AdaptiveUtilizationRollupQuantityRangeRule(120, 86400),
        ),
    )
}
