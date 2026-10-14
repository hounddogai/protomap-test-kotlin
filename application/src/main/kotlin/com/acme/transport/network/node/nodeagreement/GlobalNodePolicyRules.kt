package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.normalize

/** A rule a global node policy must pass. Check returns its problems, or none. */
interface GlobalNodePolicyRule {
    fun check(item: GlobalNodePolicy): MutableList<String>
}

data class GlobalNodePolicyCodeLengthRule(var maxLength: Int) : GlobalNodePolicyRule {
    override fun check(item: GlobalNodePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodePolicyLabelLengthRule(var maxLength: Int) : GlobalNodePolicyRule {
    override fun check(item: GlobalNodePolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodePolicyDiscountRangeRule(var min: Double, var max: Double) : GlobalNodePolicyRule {
    override fun check(item: GlobalNodePolicy): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class GlobalNodePolicyAllowedChannelRule(var allowed: MutableList<String>) : GlobalNodePolicyRule {
    override fun check(item: GlobalNodePolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodePolicyRequiredChecksumRule(var message: String) : GlobalNodePolicyRule {
    override fun check(item: GlobalNodePolicy): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global node policy. */
data class GlobalNodePolicyValidator(var rules: MutableList<GlobalNodePolicyRule>) {
    fun validate(item: GlobalNodePolicy): MutableList<String> {
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

    fun requireValid(item: GlobalNodePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalNodePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalNodePolicyValidator(): GlobalNodePolicyValidator {
    return GlobalNodePolicyValidator(
        mutableListOf<GlobalNodePolicyRule>(
            GlobalNodePolicyCodeLengthRule(255),
            GlobalNodePolicyLabelLengthRule(255),
            GlobalNodePolicyDiscountRangeRule(1.5, 1859.2),
            GlobalNodePolicyAllowedChannelRule(mutableListOf<String>("global", "legacy", "seasonal")),
            GlobalNodePolicyRequiredChecksumRule("global node policy needs this field"),
        ),
    )
}
