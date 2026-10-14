package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.text.normalize

/** A rule a availability policy must pass. Check returns its problems, or none. */
interface AvailabilityPolicyRule {
    fun check(item: AvailabilityPolicy): MutableList<String>
}

data class AvailabilityPolicyRequiredRegionRule(var message: String) : AvailabilityPolicyRule {
    override fun check(item: AvailabilityPolicy): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPolicyAllowedTitleRule(var allowed: MutableList<String>) : AvailabilityPolicyRule {
    override fun check(item: AvailabilityPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPolicyTitleLengthRule(var maxLength: Int) : AvailabilityPolicyRule {
    override fun check(item: AvailabilityPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPolicySequenceRangeRule(var min: Int, var max: Int) : AvailabilityPolicyRule {
    override fun check(item: AvailabilityPolicy): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class AvailabilityPolicyCodeLengthRule(var maxLength: Int) : AvailabilityPolicyRule {
    override fun check(item: AvailabilityPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability policy. */
data class AvailabilityPolicyValidator(var rules: MutableList<AvailabilityPolicyRule>) {
    fun validate(item: AvailabilityPolicy): MutableList<String> {
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

    fun requireValid(item: AvailabilityPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityPolicyValidator(): AvailabilityPolicyValidator {
    return AvailabilityPolicyValidator(
        mutableListOf<AvailabilityPolicyRule>(
            AvailabilityPolicyRequiredRegionRule("availability policy needs this field"),
            AvailabilityPolicyAllowedTitleRule(mutableListOf<String>("primary", "batched")),
            AvailabilityPolicyTitleLengthRule(80),
            AvailabilityPolicySequenceRangeRule(64, 120),
            AvailabilityPolicyCodeLengthRule(120),
        ),
    )
}
