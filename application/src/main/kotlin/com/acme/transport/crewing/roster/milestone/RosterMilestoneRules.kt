package com.acme.transport.crewing.roster.milestone

/** A rule a roster milestone must pass. Check returns its problems, or none. */
interface RosterMilestoneRule {
    fun check(item: RosterMilestone): MutableList<String>
}

data class RosterMilestoneLaneLengthRule(var maxLength: Int) : RosterMilestoneRule {
    override fun check(item: RosterMilestone): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterMilestoneDurationMinutesRangeRule(var min: Int, var max: Int) : RosterMilestoneRule {
    override fun check(item: RosterMilestone): MutableList<String> {
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

data class RosterMilestoneExpiresAtRangeRule(var min: Int, var max: Int) : RosterMilestoneRule {
    override fun check(item: RosterMilestone): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a roster milestone. */
data class RosterMilestoneValidator(var rules: MutableList<RosterMilestoneRule>) {
    fun validate(item: RosterMilestone): MutableList<String> {
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

    fun requireValid(item: RosterMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterMilestoneValidator(): RosterMilestoneValidator {
    return RosterMilestoneValidator(
        mutableListOf<RosterMilestoneRule>(
            RosterMilestoneLaneLengthRule(1000),
            RosterMilestoneDurationMinutesRangeRule(255, 1000),
            RosterMilestoneExpiresAtRangeRule(64, 255),
        ),
    )
}
