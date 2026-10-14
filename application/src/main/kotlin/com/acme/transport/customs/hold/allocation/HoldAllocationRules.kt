package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.normalize

/** A rule a hold allocation must pass. Check returns its problems, or none. */
interface HoldAllocationRule {
    fun check(item: HoldAllocation): MutableList<String>
}

data class HoldAllocationTitleLengthRule(var maxLength: Int) : HoldAllocationRule {
    override fun check(item: HoldAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldAllocationLaneLengthRule(var maxLength: Int) : HoldAllocationRule {
    override fun check(item: HoldAllocation): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldAllocationAllowedSegmentRule(var allowed: MutableList<String>) : HoldAllocationRule {
    override fun check(item: HoldAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class HoldAllocationRequiredLaneRule(var message: String) : HoldAllocationRule {
    override fun check(item: HoldAllocation): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold allocation. */
data class HoldAllocationValidator(var rules: MutableList<HoldAllocationRule>) {
    fun validate(item: HoldAllocation): MutableList<String> {
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

    fun requireValid(item: HoldAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldAllocationValidator(): HoldAllocationValidator {
    return HoldAllocationValidator(
        mutableListOf<HoldAllocationRule>(
            HoldAllocationTitleLengthRule(16),
            HoldAllocationLaneLengthRule(1000),
            HoldAllocationAllowedSegmentRule(mutableListOf<String>("adaptive", "fallback", "weighted")),
            HoldAllocationRequiredLaneRule("hold allocation needs this field"),
        ),
    )
}
