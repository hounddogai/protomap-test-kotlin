package com.acme.transport.crewing.restbreak.breakmilestone

/** A rule a scheduled rest break milestone must pass. Check returns its problems, or none. */
interface ScheduledRestBreakMilestoneRule {
    fun check(item: ScheduledRestBreakMilestone): MutableList<String>
}

data class ScheduledRestBreakMilestonePinnedGuardRule(var limit: Int) : ScheduledRestBreakMilestoneRule {
    override fun check(item: ScheduledRestBreakMilestone): MutableList<String> {
        if (item.pinned && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakMilestoneRequiredCodeRule(var message: String) : ScheduledRestBreakMilestoneRule {
    override fun check(item: ScheduledRestBreakMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakMilestoneCodeLengthRule(var maxLength: Int) : ScheduledRestBreakMilestoneRule {
    override fun check(item: ScheduledRestBreakMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakMilestoneDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRestBreakMilestoneRule {
    override fun check(item: ScheduledRestBreakMilestone): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled rest break milestone. */
data class ScheduledRestBreakMilestoneValidator(var rules: MutableList<ScheduledRestBreakMilestoneRule>) {
    fun validate(item: ScheduledRestBreakMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRestBreakMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRestBreakMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRestBreakMilestoneValidator(): ScheduledRestBreakMilestoneValidator {
    return ScheduledRestBreakMilestoneValidator(
        mutableListOf<ScheduledRestBreakMilestoneRule>(
            ScheduledRestBreakMilestonePinnedGuardRule(3600),
            ScheduledRestBreakMilestoneRequiredCodeRule("scheduled rest break milestone needs a value"),
            ScheduledRestBreakMilestoneCodeLengthRule(120),
            ScheduledRestBreakMilestoneDurationMinutesRangeRule(80, 120),
        ),
    )
}
