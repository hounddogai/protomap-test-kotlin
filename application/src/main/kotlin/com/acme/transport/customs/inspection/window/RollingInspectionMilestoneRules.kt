package com.acme.transport.customs.inspection.window

/** A rule a rolling inspection milestone must pass. Check returns its problems, or none. */
interface RollingInspectionMilestoneRule {
    fun check(item: RollingInspectionMilestone): MutableList<String>
}

data class RollingInspectionMilestoneShiftCodeLengthRule(var maxLength: Int) : RollingInspectionMilestoneRule {
    override fun check(item: RollingInspectionMilestone): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionMilestoneLabelLengthRule(var maxLength: Int) : RollingInspectionMilestoneRule {
    override fun check(item: RollingInspectionMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionMilestoneMaximumRangeRule(
    var min: Double,
    var max: Double,
) : RollingInspectionMilestoneRule {
    override fun check(item: RollingInspectionMilestone): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionMilestoneRequiredLabelRule(var message: String) : RollingInspectionMilestoneRule {
    override fun check(item: RollingInspectionMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionMilestoneCapacityRangeRule(
    var min: Int,
    var max: Int,
) : RollingInspectionMilestoneRule {
    override fun check(item: RollingInspectionMilestone): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionMilestoneArchivedGuardRule(var limit: Int) : RollingInspectionMilestoneRule {
    override fun check(item: RollingInspectionMilestone): MutableList<String> {
        if (item.archived && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling inspection milestone. */
data class RollingInspectionMilestoneValidator(var rules: MutableList<RollingInspectionMilestoneRule>) {
    fun validate(item: RollingInspectionMilestone): MutableList<String> {
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

    fun requireValid(item: RollingInspectionMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingInspectionMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingInspectionMilestoneValidator(): RollingInspectionMilestoneValidator {
    return RollingInspectionMilestoneValidator(
        mutableListOf<RollingInspectionMilestoneRule>(
            RollingInspectionMilestoneShiftCodeLengthRule(86400),
            RollingInspectionMilestoneLabelLengthRule(32),
            RollingInspectionMilestoneMaximumRangeRule(5.6, 2395.3),
            RollingInspectionMilestoneRequiredLabelRule("rolling inspection milestone needs this field"),
            RollingInspectionMilestoneCapacityRangeRule(16, 120),
            RollingInspectionMilestoneArchivedGuardRule(64),
        ),
    )
}
