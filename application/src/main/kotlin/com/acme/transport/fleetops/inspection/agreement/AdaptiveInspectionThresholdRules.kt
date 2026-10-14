package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection threshold must pass. Check returns its problems, or none. */
interface AdaptiveInspectionThresholdRule {
    fun check(item: AdaptiveInspectionThreshold): MutableList<String>
}

data class AdaptiveInspectionThresholdCodeLengthRule(var maxLength: Int) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionThresholdExternalRefLengthRule(
    var maxLength: Int,
) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionThresholdCodePrefixRule(var prefix: String) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionThresholdRetriesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionThresholdAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection threshold. */
data class AdaptiveInspectionThresholdValidator(var rules: MutableList<AdaptiveInspectionThresholdRule>) {
    fun validate(item: AdaptiveInspectionThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionThresholdValidator(): AdaptiveInspectionThresholdValidator {
    return AdaptiveInspectionThresholdValidator(
        mutableListOf<AdaptiveInspectionThresholdRule>(
            AdaptiveInspectionThresholdCodeLengthRule(16),
            AdaptiveInspectionThresholdExternalRefLengthRule(80),
            AdaptiveInspectionThresholdCodePrefixRule("ACM"),
            AdaptiveInspectionThresholdRetriesRangeRule(16, 255),
            AdaptiveInspectionThresholdAllowedExternalRefRule(mutableListOf<String>("manual", "scheduled")),
        ),
    )
}
