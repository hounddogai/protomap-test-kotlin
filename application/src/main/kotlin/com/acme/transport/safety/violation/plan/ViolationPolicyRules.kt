package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.normalize

/** A rule a violation policy must pass. Check returns its problems, or none. */
interface ViolationPolicyRule {
    fun check(item: ViolationPolicy): MutableList<String>
}

data class ViolationPolicyRevisionRangeRule(var min: Int, var max: Int) : ViolationPolicyRule {
    override fun check(item: ViolationPolicy): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ViolationPolicyStopCountRangeRule(var min: Int, var max: Int) : ViolationPolicyRule {
    override fun check(item: ViolationPolicy): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationPolicyTitleLengthRule(var maxLength: Int) : ViolationPolicyRule {
    override fun check(item: ViolationPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationPolicyAllowedTitleRule(var allowed: MutableList<String>) : ViolationPolicyRule {
    override fun check(item: ViolationPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ViolationPolicySummaryLengthRule(var maxLength: Int) : ViolationPolicyRule {
    override fun check(item: ViolationPolicy): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationPolicyVehicleIdLengthRule(var maxLength: Int) : ViolationPolicyRule {
    override fun check(item: ViolationPolicy): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation policy. */
data class ViolationPolicyValidator(var rules: MutableList<ViolationPolicyRule>) {
    fun validate(item: ViolationPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationPolicyValidator(): ViolationPolicyValidator {
    return ViolationPolicyValidator(
        mutableListOf<ViolationPolicyRule>(
            ViolationPolicyRevisionRangeRule(64, 80),
            ViolationPolicyStopCountRangeRule(3600, 86400),
            ViolationPolicyTitleLengthRule(16),
            ViolationPolicyAllowedTitleRule(mutableListOf<String>("capped", "weighted")),
            ViolationPolicySummaryLengthRule(64),
            ViolationPolicyVehicleIdLengthRule(1000),
        ),
    )
}
