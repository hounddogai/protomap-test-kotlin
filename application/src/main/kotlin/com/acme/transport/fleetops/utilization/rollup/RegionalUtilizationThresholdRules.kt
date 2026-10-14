package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.normalize

/** A rule a regional utilization threshold must pass. Check returns its problems, or none. */
interface RegionalUtilizationThresholdRule {
    fun check(item: RegionalUtilizationThreshold): MutableList<String>
}

data class RegionalUtilizationThresholdReviewerLengthRule(
    var maxLength: Int,
) : RegionalUtilizationThresholdRule {
    override fun check(item: RegionalUtilizationThreshold): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationThresholdCodePrefixRule(var prefix: String) : RegionalUtilizationThresholdRule {
    override fun check(item: RegionalUtilizationThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationThresholdRequiredTimezoneRule(
    var message: String,
) : RegionalUtilizationThresholdRule {
    override fun check(item: RegionalUtilizationThreshold): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationThresholdAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalUtilizationThresholdRule {
    override fun check(item: RegionalUtilizationThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationThresholdFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RegionalUtilizationThresholdRule {
    override fun check(item: RegionalUtilizationThreshold): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional utilization threshold. */
data class RegionalUtilizationThresholdValidator(var rules: MutableList<RegionalUtilizationThresholdRule>) {
    fun validate(item: RegionalUtilizationThreshold): MutableList<String> {
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

    fun requireValid(item: RegionalUtilizationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalUtilizationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalUtilizationThresholdValidator(): RegionalUtilizationThresholdValidator {
    return RegionalUtilizationThresholdValidator(
        mutableListOf<RegionalUtilizationThresholdRule>(
            RegionalUtilizationThresholdReviewerLengthRule(1000),
            RegionalUtilizationThresholdCodePrefixRule("ACM"),
            RegionalUtilizationThresholdRequiredTimezoneRule("regional utilization threshold needs this field"),
            RegionalUtilizationThresholdAllowedCodeRule(mutableListOf<String>()),
            RegionalUtilizationThresholdFuelLitersRangeRule(7.3, 2694.9),
        ),
    )
}
