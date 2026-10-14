package com.acme.transport.customs.hold.milestone

/** A rule a scheduled hold milestone must pass. Check returns its problems, or none. */
interface ScheduledHoldMilestoneRule {
    fun check(item: ScheduledHoldMilestone): MutableList<String>
}

data class ScheduledHoldMilestoneStartsAtRangeRule(var min: Int, var max: Int) : ScheduledHoldMilestoneRule {
    override fun check(item: ScheduledHoldMilestone): MutableList<String> {
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

data class ScheduledHoldMilestoneDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledHoldMilestoneRule {
    override fun check(item: ScheduledHoldMilestone): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldMilestoneActiveGuardRule(var limit: Int) : ScheduledHoldMilestoneRule {
    override fun check(item: ScheduledHoldMilestone): MutableList<String> {
        if (item.active && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldMilestoneCodePrefixRule(var prefix: String) : ScheduledHoldMilestoneRule {
    override fun check(item: ScheduledHoldMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldMilestoneRequiredCodeRule(var message: String) : ScheduledHoldMilestoneRule {
    override fun check(item: ScheduledHoldMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hold milestone. */
data class ScheduledHoldMilestoneValidator(var rules: MutableList<ScheduledHoldMilestoneRule>) {
    fun validate(item: ScheduledHoldMilestone): MutableList<String> {
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

    fun requireValid(item: ScheduledHoldMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldMilestoneValidator(): ScheduledHoldMilestoneValidator {
    return ScheduledHoldMilestoneValidator(
        mutableListOf<ScheduledHoldMilestoneRule>(
            ScheduledHoldMilestoneStartsAtRangeRule(32, 80),
            ScheduledHoldMilestoneDistanceKmRangeRule(0.5, 3170.1),
            ScheduledHoldMilestoneActiveGuardRule(3600),
            ScheduledHoldMilestoneCodePrefixRule("ACM"),
            ScheduledHoldMilestoneRequiredCodeRule("scheduled hold milestone needs an entry"),
        ),
    )
}
