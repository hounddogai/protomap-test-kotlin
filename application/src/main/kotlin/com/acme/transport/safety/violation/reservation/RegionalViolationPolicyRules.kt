package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.normalize

/** A rule a regional violation policy must pass. Check returns its problems, or none. */
interface RegionalViolationPolicyRule {
    fun check(item: RegionalViolationPolicy): MutableList<String>
}

data class RegionalViolationPolicyCapacityRangeRule(var min: Int, var max: Int) : RegionalViolationPolicyRule {
    override fun check(item: RegionalViolationPolicy): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class RegionalViolationPolicyAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalViolationPolicyRule {
    override fun check(item: RegionalViolationPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationPolicyCodePrefixRule(var prefix: String) : RegionalViolationPolicyRule {
    override fun check(item: RegionalViolationPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationPolicyPalletsRangeRule(var min: Int, var max: Int) : RegionalViolationPolicyRule {
    override fun check(item: RegionalViolationPolicy): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationPolicyRequiredSourceRule(var message: String) : RegionalViolationPolicyRule {
    override fun check(item: RegionalViolationPolicy): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationPolicyRequiredCodeRule(var message: String) : RegionalViolationPolicyRule {
    override fun check(item: RegionalViolationPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional violation policy. */
data class RegionalViolationPolicyValidator(var rules: MutableList<RegionalViolationPolicyRule>) {
    fun validate(item: RegionalViolationPolicy): MutableList<String> {
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

    fun requireValid(item: RegionalViolationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalViolationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalViolationPolicyValidator(): RegionalViolationPolicyValidator {
    return RegionalViolationPolicyValidator(
        mutableListOf<RegionalViolationPolicyRule>(
            RegionalViolationPolicyCapacityRangeRule(1000, 86400),
            RegionalViolationPolicyAllowedCodeRule(mutableListOf<String>("scheduled")),
            RegionalViolationPolicyCodePrefixRule("Q"),
            RegionalViolationPolicyPalletsRangeRule(80, 1000),
            RegionalViolationPolicyRequiredSourceRule("regional violation policy needs an entry"),
            RegionalViolationPolicyRequiredCodeRule("regional violation policy needs an entry"),
        ),
    )
}
