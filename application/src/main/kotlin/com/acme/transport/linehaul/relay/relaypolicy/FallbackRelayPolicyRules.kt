package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.text.normalize

/** A rule a fallback relay policy must pass. Check returns its problems, or none. */
interface FallbackRelayPolicyRule {
    fun check(item: FallbackRelayPolicy): MutableList<String>
}

data class FallbackRelayPolicyTitleLengthRule(var maxLength: Int) : FallbackRelayPolicyRule {
    override fun check(item: FallbackRelayPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayPolicyPriorityRangeRule(var min: Int, var max: Int) : FallbackRelayPolicyRule {
    override fun check(item: FallbackRelayPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class FallbackRelayPolicyThresholdRangeRule(var min: Double, var max: Double) : FallbackRelayPolicyRule {
    override fun check(item: FallbackRelayPolicy): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayPolicyCeilingRangeRule(var min: Double, var max: Double) : FallbackRelayPolicyRule {
    override fun check(item: FallbackRelayPolicy): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class FallbackRelayPolicyAllowedPhoneRule(var allowed: MutableList<String>) : FallbackRelayPolicyRule {
    override fun check(item: FallbackRelayPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayPolicyCodeLengthRule(var maxLength: Int) : FallbackRelayPolicyRule {
    override fun check(item: FallbackRelayPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback relay policy. */
data class FallbackRelayPolicyValidator(var rules: MutableList<FallbackRelayPolicyRule>) {
    fun validate(item: FallbackRelayPolicy): MutableList<String> {
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

    fun requireValid(item: FallbackRelayPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRelayPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRelayPolicyValidator(): FallbackRelayPolicyValidator {
    return FallbackRelayPolicyValidator(
        mutableListOf<FallbackRelayPolicyRule>(
            FallbackRelayPolicyTitleLengthRule(86400),
            FallbackRelayPolicyPriorityRangeRule(1000, 86400),
            FallbackRelayPolicyThresholdRangeRule(9.8, 3457.9),
            FallbackRelayPolicyCeilingRangeRule(6.7, 189.2),
            FallbackRelayPolicyAllowedPhoneRule(mutableListOf<String>("fallback", "strict")),
            FallbackRelayPolicyCodeLengthRule(80),
        ),
    )
}
