package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.text.normalize

/** A rule a fallback inspection policy must pass. Check returns its problems, or none. */
interface FallbackInspectionPolicyRule {
    fun check(item: FallbackInspectionPolicy): MutableList<String>
}

data class FallbackInspectionPolicyCodeLengthRule(var maxLength: Int) : FallbackInspectionPolicyRule {
    override fun check(item: FallbackInspectionPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionPolicyRequiredChannelRule(var message: String) : FallbackInspectionPolicyRule {
    override fun check(item: FallbackInspectionPolicy): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionPolicyAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackInspectionPolicyRule {
    override fun check(item: FallbackInspectionPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionPolicyChannelLengthRule(var maxLength: Int) : FallbackInspectionPolicyRule {
    override fun check(item: FallbackInspectionPolicy): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionPolicyNameLengthRule(var maxLength: Int) : FallbackInspectionPolicyRule {
    override fun check(item: FallbackInspectionPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback inspection policy. */
data class FallbackInspectionPolicyValidator(var rules: MutableList<FallbackInspectionPolicyRule>) {
    fun validate(item: FallbackInspectionPolicy): MutableList<String> {
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

    fun requireValid(item: FallbackInspectionPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackInspectionPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackInspectionPolicyValidator(): FallbackInspectionPolicyValidator {
    return FallbackInspectionPolicyValidator(
        mutableListOf<FallbackInspectionPolicyRule>(
            FallbackInspectionPolicyCodeLengthRule(64),
            FallbackInspectionPolicyRequiredChannelRule("fallback inspection policy needs this field"),
            FallbackInspectionPolicyAllowedCodeRule(mutableListOf<String>("adaptive")),
            FallbackInspectionPolicyChannelLengthRule(1000),
            FallbackInspectionPolicyNameLengthRule(16),
        ),
    )
}
