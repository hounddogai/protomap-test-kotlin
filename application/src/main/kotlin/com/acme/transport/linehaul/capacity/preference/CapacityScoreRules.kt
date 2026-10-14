package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.normalize

/** A rule a capacity score must pass. Check returns its problems, or none. */
interface CapacityScoreRule {
    fun check(item: CapacityScore): MutableList<String>
}

data class CapacityScoreCodePrefixRule(var prefix: String) : CapacityScoreRule {
    override fun check(item: CapacityScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityScoreExpiresAtRangeRule(var min: Int, var max: Int) : CapacityScoreRule {
    override fun check(item: CapacityScore): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityScoreAllowedLabelRule(var allowed: MutableList<String>) : CapacityScoreRule {
    override fun check(item: CapacityScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity score. */
data class CapacityScoreValidator(var rules: MutableList<CapacityScoreRule>) {
    fun validate(item: CapacityScore): MutableList<String> {
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

    fun requireValid(item: CapacityScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityScore>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityScoreValidator(): CapacityScoreValidator {
    return CapacityScoreValidator(
        mutableListOf<CapacityScoreRule>(
            CapacityScoreCodePrefixRule("ACM"),
            CapacityScoreExpiresAtRangeRule(255, 1000),
            CapacityScoreAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
