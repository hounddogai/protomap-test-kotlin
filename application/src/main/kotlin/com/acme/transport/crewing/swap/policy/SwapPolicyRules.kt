package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize

/** A rule a swap policy must pass. Check returns its problems, or none. */
interface SwapPolicyRule {
    fun check(item: SwapPolicy): MutableList<String>
}

data class SwapPolicyStartsAtRangeRule(var min: Int, var max: Int) : SwapPolicyRule {
    override fun check(item: SwapPolicy): MutableList<String> {
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

data class SwapPolicyCodePrefixRule(var prefix: String) : SwapPolicyRule {
    override fun check(item: SwapPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapPolicyRequiredLabelRule(var message: String) : SwapPolicyRule {
    override fun check(item: SwapPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapPolicyAllowedCodeRule(var allowed: MutableList<String>) : SwapPolicyRule {
    override fun check(item: SwapPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class SwapPolicyCapacityRangeRule(var min: Int, var max: Int) : SwapPolicyRule {
    override fun check(item: SwapPolicy): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapPolicyRequiredCodeRule(var message: String) : SwapPolicyRule {
    override fun check(item: SwapPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap policy. */
data class SwapPolicyValidator(var rules: MutableList<SwapPolicyRule>) {
    fun validate(item: SwapPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapPolicyValidator(): SwapPolicyValidator {
    return SwapPolicyValidator(
        mutableListOf<SwapPolicyRule>(
            SwapPolicyStartsAtRangeRule(16, 86400),
            SwapPolicyCodePrefixRule("X"),
            SwapPolicyRequiredLabelRule("swap policy needs an entry"),
            SwapPolicyAllowedCodeRule(mutableListOf<String>("pending")),
            SwapPolicyCapacityRangeRule(64, 80),
            SwapPolicyRequiredCodeRule("swap policy needs this field"),
        ),
    )
}
