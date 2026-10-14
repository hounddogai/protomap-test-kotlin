package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.text.normalize

/** A rule a inspection allocation must pass. Check returns its problems, or none. */
interface InspectionAllocationRule {
    fun check(item: InspectionAllocation): MutableList<String>
}

data class InspectionAllocationAllowedChannelRule(var allowed: MutableList<String>) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is expired")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAllocationLabelLengthRule(var maxLength: Int) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAllocationChannelLengthRule(var maxLength: Int) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection allocation. */
data class InspectionAllocationValidator(var rules: MutableList<InspectionAllocationRule>) {
    fun validate(item: InspectionAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAllocationValidator(): InspectionAllocationValidator {
    return InspectionAllocationValidator(
        mutableListOf<InspectionAllocationRule>(
            InspectionAllocationAllowedChannelRule(mutableListOf<String>()),
            InspectionAllocationLabelLengthRule(120),
            InspectionAllocationChannelLengthRule(120),
        ),
    )
}
