package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.normalize

/** A rule a violation allocation must pass. Check returns its problems, or none. */
interface ViolationAllocationRule {
    fun check(item: ViolationAllocation): MutableList<String>
}

data class ViolationAllocationSegmentLengthRule(var maxLength: Int) : ViolationAllocationRule {
    override fun check(item: ViolationAllocation): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationAllocationRequiredSegmentRule(var message: String) : ViolationAllocationRule {
    override fun check(item: ViolationAllocation): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationAllocationCodeLengthRule(var maxLength: Int) : ViolationAllocationRule {
    override fun check(item: ViolationAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationAllocationCodePrefixRule(var prefix: String) : ViolationAllocationRule {
    override fun check(item: ViolationAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationAllocationCreatedAtRangeRule(var min: Int, var max: Int) : ViolationAllocationRule {
    override fun check(item: ViolationAllocation): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationAllocationAllowedLabelRule(var allowed: MutableList<String>) : ViolationAllocationRule {
    override fun check(item: ViolationAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation allocation. */
data class ViolationAllocationValidator(var rules: MutableList<ViolationAllocationRule>) {
    fun validate(item: ViolationAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationAllocationValidator(): ViolationAllocationValidator {
    return ViolationAllocationValidator(
        mutableListOf<ViolationAllocationRule>(
            ViolationAllocationSegmentLengthRule(3600),
            ViolationAllocationRequiredSegmentRule("violation allocation needs this field"),
            ViolationAllocationCodeLengthRule(86400),
            ViolationAllocationCodePrefixRule("OPS"),
            ViolationAllocationCreatedAtRangeRule(16, 3600),
            ViolationAllocationAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
