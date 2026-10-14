package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a manual backhaul allocation must pass. Check returns its problems, or none. */
interface ManualBackhaulAllocationRule {
    fun check(item: ManualBackhaulAllocation): MutableList<String>
}

data class ManualBackhaulAllocationNameLengthRule(var maxLength: Int) : ManualBackhaulAllocationRule {
    override fun check(item: ManualBackhaulAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulAllocationStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualBackhaulAllocationRule {
    override fun check(item: ManualBackhaulAllocation): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ManualBackhaulAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualBackhaulAllocationRule {
    override fun check(item: ManualBackhaulAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual backhaul allocation. */
data class ManualBackhaulAllocationValidator(var rules: MutableList<ManualBackhaulAllocationRule>) {
    fun validate(item: ManualBackhaulAllocation): MutableList<String> {
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

    fun requireValid(item: ManualBackhaulAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBackhaulAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBackhaulAllocationValidator(): ManualBackhaulAllocationValidator {
    return ManualBackhaulAllocationValidator(
        mutableListOf<ManualBackhaulAllocationRule>(
            ManualBackhaulAllocationNameLengthRule(80),
            ManualBackhaulAllocationStartsAtRangeRule(1000, 3600),
            ManualBackhaulAllocationAllowedCodeRule(mutableListOf<String>("global", "primary")),
        ),
    )
}
