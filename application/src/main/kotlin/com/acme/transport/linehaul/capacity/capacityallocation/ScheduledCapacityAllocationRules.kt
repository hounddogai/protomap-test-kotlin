package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.normalize

/** A rule a scheduled capacity allocation must pass. Check returns its problems, or none. */
interface ScheduledCapacityAllocationRule {
    fun check(item: ScheduledCapacityAllocation): MutableList<String>
}

data class ScheduledCapacityAllocationSummaryLengthRule(var maxLength: Int) : ScheduledCapacityAllocationRule {
    override fun check(item: ScheduledCapacityAllocation): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityAllocationRequiredDriverIdRule(
    var message: String,
) : ScheduledCapacityAllocationRule {
    override fun check(item: ScheduledCapacityAllocation): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityAllocationAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : ScheduledCapacityAllocationRule {
    override fun check(item: ScheduledCapacityAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled capacity allocation. */
data class ScheduledCapacityAllocationValidator(var rules: MutableList<ScheduledCapacityAllocationRule>) {
    fun validate(item: ScheduledCapacityAllocation): MutableList<String> {
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

    fun requireValid(item: ScheduledCapacityAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCapacityAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCapacityAllocationValidator(): ScheduledCapacityAllocationValidator {
    return ScheduledCapacityAllocationValidator(
        mutableListOf<ScheduledCapacityAllocationRule>(
            ScheduledCapacityAllocationSummaryLengthRule(80),
            ScheduledCapacityAllocationRequiredDriverIdRule("scheduled capacity allocation needs an entry"),
            ScheduledCapacityAllocationAllowedExternalRefRule(mutableListOf<String>("global")),
        ),
    )
}
