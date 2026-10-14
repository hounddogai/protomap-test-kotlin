package com.acme.transport.linehaul.backhaul.policy

/** A rule a rolling backhaul policy must pass. Check returns its problems, or none. */
interface RollingBackhaulPolicyRule {
    fun check(item: RollingBackhaulPolicy): MutableList<String>
}

data class RollingBackhaulPolicyCodeLengthRule(var maxLength: Int) : RollingBackhaulPolicyRule {
    override fun check(item: RollingBackhaulPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulPolicyCodePrefixRule(var prefix: String) : RollingBackhaulPolicyRule {
    override fun check(item: RollingBackhaulPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulPolicyQuantityRangeRule(var min: Int, var max: Int) : RollingBackhaulPolicyRule {
    override fun check(item: RollingBackhaulPolicy): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling backhaul policy. */
data class RollingBackhaulPolicyValidator(var rules: MutableList<RollingBackhaulPolicyRule>) {
    fun validate(item: RollingBackhaulPolicy): MutableList<String> {
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

    fun requireValid(item: RollingBackhaulPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBackhaulPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBackhaulPolicyValidator(): RollingBackhaulPolicyValidator {
    return RollingBackhaulPolicyValidator(
        mutableListOf<RollingBackhaulPolicyRule>(
            RollingBackhaulPolicyCodeLengthRule(255),
            RollingBackhaulPolicyCodePrefixRule("REF"),
            RollingBackhaulPolicyQuantityRangeRule(1000, 1000),
        ),
    )
}
