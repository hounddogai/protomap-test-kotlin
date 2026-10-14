package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.normalize

/** A rule a primary sortation milestone must pass. Check returns its problems, or none. */
interface PrimarySortationMilestoneRule {
    fun check(item: PrimarySortationMilestone): MutableList<String>
}

data class PrimarySortationMilestoneAllowedSegmentRule(
    var allowed: MutableList<String>,
) : PrimarySortationMilestoneRule {
    override fun check(item: PrimarySortationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationMilestoneNameLengthRule(var maxLength: Int) : PrimarySortationMilestoneRule {
    override fun check(item: PrimarySortationMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationMilestoneUnitLengthRule(var maxLength: Int) : PrimarySortationMilestoneRule {
    override fun check(item: PrimarySortationMilestone): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationMilestoneCodeLengthRule(var maxLength: Int) : PrimarySortationMilestoneRule {
    override fun check(item: PrimarySortationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary sortation milestone. */
data class PrimarySortationMilestoneValidator(var rules: MutableList<PrimarySortationMilestoneRule>) {
    fun validate(item: PrimarySortationMilestone): MutableList<String> {
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

    fun requireValid(item: PrimarySortationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimarySortationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimarySortationMilestoneValidator(): PrimarySortationMilestoneValidator {
    return PrimarySortationMilestoneValidator(
        mutableListOf<PrimarySortationMilestoneRule>(
            PrimarySortationMilestoneAllowedSegmentRule(mutableListOf<String>("regional", "lenient")),
            PrimarySortationMilestoneNameLengthRule(16),
            PrimarySortationMilestoneUnitLengthRule(255),
            PrimarySortationMilestoneCodeLengthRule(255),
        ),
    )
}
