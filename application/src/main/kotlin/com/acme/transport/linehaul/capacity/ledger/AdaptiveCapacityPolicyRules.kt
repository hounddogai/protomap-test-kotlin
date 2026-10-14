package com.acme.transport.linehaul.capacity.ledger

/** A rule a adaptive capacity policy must pass. Check returns its problems, or none. */
interface AdaptiveCapacityPolicyRule {
    fun check(item: AdaptiveCapacityPolicy): MutableList<String>
}

data class AdaptiveCapacityPolicyCodeLengthRule(var maxLength: Int) : AdaptiveCapacityPolicyRule {
    override fun check(item: AdaptiveCapacityPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityPolicyPriorityRangeRule(var min: Int, var max: Int) : AdaptiveCapacityPolicyRule {
    override fun check(item: AdaptiveCapacityPolicy): MutableList<String> {
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

data class AdaptiveCapacityPolicyNameLengthRule(var maxLength: Int) : AdaptiveCapacityPolicyRule {
    override fun check(item: AdaptiveCapacityPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive capacity policy. */
data class AdaptiveCapacityPolicyValidator(var rules: MutableList<AdaptiveCapacityPolicyRule>) {
    fun validate(item: AdaptiveCapacityPolicy): MutableList<String> {
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

    fun requireValid(item: AdaptiveCapacityPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCapacityPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCapacityPolicyValidator(): AdaptiveCapacityPolicyValidator {
    return AdaptiveCapacityPolicyValidator(
        mutableListOf<AdaptiveCapacityPolicyRule>(
            AdaptiveCapacityPolicyCodeLengthRule(16),
            AdaptiveCapacityPolicyPriorityRangeRule(1000, 86400),
            AdaptiveCapacityPolicyNameLengthRule(3600),
        ),
    )
}
