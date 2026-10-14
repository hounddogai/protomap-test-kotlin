package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a availability allocation must pass. Check returns its problems, or none. */
interface AvailabilityAllocationRule {
    fun check(item: AvailabilityAllocation): MutableList<String>
}

data class AvailabilityAllocationCodeLengthRule(var maxLength: Int) : AvailabilityAllocationRule {
    override fun check(item: AvailabilityAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAllocationCodePrefixRule(var prefix: String) : AvailabilityAllocationRule {
    override fun check(item: AvailabilityAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAllocationTitleLengthRule(var maxLength: Int) : AvailabilityAllocationRule {
    override fun check(item: AvailabilityAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAllocationAllowedPhoneRule(
    var allowed: MutableList<String>,
) : AvailabilityAllocationRule {
    override fun check(item: AvailabilityAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability allocation. */
data class AvailabilityAllocationValidator(var rules: MutableList<AvailabilityAllocationRule>) {
    fun validate(item: AvailabilityAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityAllocationValidator(): AvailabilityAllocationValidator {
    return AvailabilityAllocationValidator(
        mutableListOf<AvailabilityAllocationRule>(
            AvailabilityAllocationCodeLengthRule(255),
            AvailabilityAllocationCodePrefixRule("REF"),
            AvailabilityAllocationTitleLengthRule(32),
            AvailabilityAllocationAllowedPhoneRule(mutableListOf<String>("regional", "adaptive")),
        ),
    )
}
