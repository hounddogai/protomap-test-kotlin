package com.acme.transport.linehaul.tender.tenderforecast

/** A rule a scheduled tender milestone must pass. Check returns its problems, or none. */
interface ScheduledTenderMilestoneRule {
    fun check(item: ScheduledTenderMilestone): MutableList<String>
}

data class ScheduledTenderMilestoneRegionLengthRule(var maxLength: Int) : ScheduledTenderMilestoneRule {
    override fun check(item: ScheduledTenderMilestone): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTenderMilestoneActiveGuardRule(var limit: Int) : ScheduledTenderMilestoneRule {
    override fun check(item: ScheduledTenderMilestone): MutableList<String> {
        if (item.active && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTenderMilestoneRequiredCodeRule(var message: String) : ScheduledTenderMilestoneRule {
    override fun check(item: ScheduledTenderMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled tender milestone. */
data class ScheduledTenderMilestoneValidator(var rules: MutableList<ScheduledTenderMilestoneRule>) {
    fun validate(item: ScheduledTenderMilestone): MutableList<String> {
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

    fun requireValid(item: ScheduledTenderMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTenderMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTenderMilestoneValidator(): ScheduledTenderMilestoneValidator {
    return ScheduledTenderMilestoneValidator(
        mutableListOf<ScheduledTenderMilestoneRule>(
            ScheduledTenderMilestoneRegionLengthRule(32),
            ScheduledTenderMilestoneActiveGuardRule(1000),
            ScheduledTenderMilestoneRequiredCodeRule("scheduled tender milestone needs an entry"),
        ),
    )
}
