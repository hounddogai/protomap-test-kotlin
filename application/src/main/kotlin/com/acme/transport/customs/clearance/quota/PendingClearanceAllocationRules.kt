package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.normalize

/** A rule a pending clearance allocation must pass. Check returns its problems, or none. */
interface PendingClearanceAllocationRule {
    fun check(item: PendingClearanceAllocation): MutableList<String>
}

data class PendingClearanceAllocationRequiredDockRule(var message: String) : PendingClearanceAllocationRule {
    override fun check(item: PendingClearanceAllocation): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAllocationCodeLengthRule(var maxLength: Int) : PendingClearanceAllocationRule {
    override fun check(item: PendingClearanceAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAllocationRequiredTitleRule(var message: String) : PendingClearanceAllocationRule {
    override fun check(item: PendingClearanceAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAllocationRequiredChannelRule(var message: String) : PendingClearanceAllocationRule {
    override fun check(item: PendingClearanceAllocation): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAllocationCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PendingClearanceAllocationRule {
    override fun check(item: PendingClearanceAllocation): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingClearanceAllocationRule {
    override fun check(item: PendingClearanceAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending clearance allocation. */
data class PendingClearanceAllocationValidator(var rules: MutableList<PendingClearanceAllocationRule>) {
    fun validate(item: PendingClearanceAllocation): MutableList<String> {
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

    fun requireValid(item: PendingClearanceAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingClearanceAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingClearanceAllocationValidator(): PendingClearanceAllocationValidator {
    return PendingClearanceAllocationValidator(
        mutableListOf<PendingClearanceAllocationRule>(
            PendingClearanceAllocationRequiredDockRule("pending clearance allocation needs a value"),
            PendingClearanceAllocationCodeLengthRule(1000),
            PendingClearanceAllocationRequiredTitleRule("pending clearance allocation needs a value"),
            PendingClearanceAllocationRequiredChannelRule("pending clearance allocation needs this field"),
            PendingClearanceAllocationCapacityRangeRule(120, 255),
            PendingClearanceAllocationAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
