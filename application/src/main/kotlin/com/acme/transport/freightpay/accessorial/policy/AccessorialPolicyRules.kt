package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize

/** A rule a accessorial policy must pass. Check returns its problems, or none. */
interface AccessorialPolicyRule {
    fun check(item: AccessorialPolicy): MutableList<String>
}

data class AccessorialPolicyCodeLengthRule(var maxLength: Int) : AccessorialPolicyRule {
    override fun check(item: AccessorialPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPolicyRegionLengthRule(var maxLength: Int) : AccessorialPolicyRule {
    override fun check(item: AccessorialPolicy): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPolicySegmentLengthRule(var maxLength: Int) : AccessorialPolicyRule {
    override fun check(item: AccessorialPolicy): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPolicyCodePrefixRule(var prefix: String) : AccessorialPolicyRule {
    override fun check(item: AccessorialPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPolicyAllowedRegionRule(var allowed: MutableList<String>) : AccessorialPolicyRule {
    override fun check(item: AccessorialPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial policy. */
data class AccessorialPolicyValidator(var rules: MutableList<AccessorialPolicyRule>) {
    fun validate(item: AccessorialPolicy): MutableList<String> {
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

    fun requireValid(item: AccessorialPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialPolicyValidator(): AccessorialPolicyValidator {
    return AccessorialPolicyValidator(
        mutableListOf<AccessorialPolicyRule>(
            AccessorialPolicyCodeLengthRule(120),
            AccessorialPolicyRegionLengthRule(120),
            AccessorialPolicySegmentLengthRule(1000),
            AccessorialPolicyCodePrefixRule("X"),
            AccessorialPolicyAllowedRegionRule(mutableListOf<String>("default")),
        ),
    )
}
