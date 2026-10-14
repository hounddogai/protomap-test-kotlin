package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.text.normalize

/** A rule a lenient dispute policy must pass. Check returns its problems, or none. */
interface LenientDisputePolicyRule {
    fun check(item: LenientDisputePolicy): MutableList<String>
}

data class LenientDisputePolicyCodePrefixRule(var prefix: String) : LenientDisputePolicyRule {
    override fun check(item: LenientDisputePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputePolicyTitleLengthRule(var maxLength: Int) : LenientDisputePolicyRule {
    override fun check(item: LenientDisputePolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputePolicyAllowedDockRule(var allowed: MutableList<String>) : LenientDisputePolicyRule {
    override fun check(item: LenientDisputePolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputePolicyRevisionRangeRule(var min: Int, var max: Int) : LenientDisputePolicyRule {
    override fun check(item: LenientDisputePolicy): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dispute policy. */
data class LenientDisputePolicyValidator(var rules: MutableList<LenientDisputePolicyRule>) {
    fun validate(item: LenientDisputePolicy): MutableList<String> {
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

    fun requireValid(item: LenientDisputePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDisputePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDisputePolicyValidator(): LenientDisputePolicyValidator {
    return LenientDisputePolicyValidator(
        mutableListOf<LenientDisputePolicyRule>(
            LenientDisputePolicyCodePrefixRule("Q"),
            LenientDisputePolicyTitleLengthRule(3600),
            LenientDisputePolicyAllowedDockRule(mutableListOf<String>("seasonal")),
            LenientDisputePolicyRevisionRangeRule(32, 3600),
        ),
    )
}
