package com.acme.transport.network.zone.zonequota

/** A rule a primary zone allocation must pass. Check returns its problems, or none. */
interface PrimaryZoneAllocationRule {
    fun check(item: PrimaryZoneAllocation): MutableList<String>
}

data class PrimaryZoneAllocationCodeLengthRule(var maxLength: Int) : PrimaryZoneAllocationRule {
    override fun check(item: PrimaryZoneAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneAllocationVisibleGuardRule(var limit: Int) : PrimaryZoneAllocationRule {
    override fun check(item: PrimaryZoneAllocation): MutableList<String> {
        if (item.visible && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneAllocationRequiredCodeRule(var message: String) : PrimaryZoneAllocationRule {
    override fun check(item: PrimaryZoneAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary zone allocation. */
data class PrimaryZoneAllocationValidator(var rules: MutableList<PrimaryZoneAllocationRule>) {
    fun validate(item: PrimaryZoneAllocation): MutableList<String> {
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

    fun requireValid(item: PrimaryZoneAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryZoneAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryZoneAllocationValidator(): PrimaryZoneAllocationValidator {
    return PrimaryZoneAllocationValidator(
        mutableListOf<PrimaryZoneAllocationRule>(
            PrimaryZoneAllocationCodeLengthRule(3600),
            PrimaryZoneAllocationVisibleGuardRule(255),
            PrimaryZoneAllocationRequiredCodeRule("primary zone allocation needs this field"),
        ),
    )
}
