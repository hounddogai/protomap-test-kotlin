package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.normalize

/** A rule a pending inspection milestone must pass. Check returns its problems, or none. */
interface PendingInspectionMilestoneRule {
    fun check(item: PendingInspectionMilestone): MutableList<String>
}

data class PendingInspectionMilestoneAllowedLabelRule(
    var allowed: MutableList<String>,
) : PendingInspectionMilestoneRule {
    override fun check(item: PendingInspectionMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionMilestoneCodeLengthRule(var maxLength: Int) : PendingInspectionMilestoneRule {
    override fun check(item: PendingInspectionMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionMilestoneEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingInspectionMilestoneRule {
    override fun check(item: PendingInspectionMilestone): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class PendingInspectionMilestoneLabelLengthRule(var maxLength: Int) : PendingInspectionMilestoneRule {
    override fun check(item: PendingInspectionMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection milestone. */
data class PendingInspectionMilestoneValidator(var rules: MutableList<PendingInspectionMilestoneRule>) {
    fun validate(item: PendingInspectionMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingInspectionMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionMilestoneValidator(): PendingInspectionMilestoneValidator {
    return PendingInspectionMilestoneValidator(
        mutableListOf<PendingInspectionMilestoneRule>(
            PendingInspectionMilestoneAllowedLabelRule(mutableListOf<String>("scheduled", "scheduled")),
            PendingInspectionMilestoneCodeLengthRule(32),
            PendingInspectionMilestoneEndsAtRangeRule(32, 1000),
            PendingInspectionMilestoneLabelLengthRule(255),
        ),
    )
}
