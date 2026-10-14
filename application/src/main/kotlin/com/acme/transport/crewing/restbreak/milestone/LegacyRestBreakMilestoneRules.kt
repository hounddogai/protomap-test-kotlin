package com.acme.transport.crewing.restbreak.milestone

/** A rule a legacy rest break milestone must pass. Check returns its problems, or none. */
interface LegacyRestBreakMilestoneRule {
    fun check(item: LegacyRestBreakMilestone): MutableList<String>
}

data class LegacyRestBreakMilestoneActiveGuardRule(var limit: Int) : LegacyRestBreakMilestoneRule {
    override fun check(item: LegacyRestBreakMilestone): MutableList<String> {
        if (item.active && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakMilestoneDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyRestBreakMilestoneRule {
    override fun check(item: LegacyRestBreakMilestone): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakMilestoneRequiredCodeRule(var message: String) : LegacyRestBreakMilestoneRule {
    override fun check(item: LegacyRestBreakMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakMilestoneRequiredNameRule(var message: String) : LegacyRestBreakMilestoneRule {
    override fun check(item: LegacyRestBreakMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rest break milestone. */
data class LegacyRestBreakMilestoneValidator(var rules: MutableList<LegacyRestBreakMilestoneRule>) {
    fun validate(item: LegacyRestBreakMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRestBreakMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRestBreakMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRestBreakMilestoneValidator(): LegacyRestBreakMilestoneValidator {
    return LegacyRestBreakMilestoneValidator(
        mutableListOf<LegacyRestBreakMilestoneRule>(
            LegacyRestBreakMilestoneActiveGuardRule(3600),
            LegacyRestBreakMilestoneDurationMinutesRangeRule(64, 1000),
            LegacyRestBreakMilestoneRequiredCodeRule("legacy rest break milestone needs a value"),
            LegacyRestBreakMilestoneRequiredNameRule("legacy rest break milestone needs a value"),
        ),
    )
}
