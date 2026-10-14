package com.acme.transport.crewing.restbreak.legacybreakentry

/** A rule a rolling rest break milestone must pass. Check returns its problems, or none. */
interface RollingRestBreakMilestoneRule {
    fun check(item: RollingRestBreakMilestone): MutableList<String>
}

data class RollingRestBreakMilestoneRequiredTimezoneRule(var message: String) : RollingRestBreakMilestoneRule {
    override fun check(item: RollingRestBreakMilestone): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRestBreakMilestoneTimezoneLengthRule(var maxLength: Int) : RollingRestBreakMilestoneRule {
    override fun check(item: RollingRestBreakMilestone): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRestBreakMilestoneQuantityRangeRule(
    var min: Int,
    var max: Int,
) : RollingRestBreakMilestoneRule {
    override fun check(item: RollingRestBreakMilestone): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling rest break milestone. */
data class RollingRestBreakMilestoneValidator(var rules: MutableList<RollingRestBreakMilestoneRule>) {
    fun validate(item: RollingRestBreakMilestone): MutableList<String> {
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

    fun requireValid(item: RollingRestBreakMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRestBreakMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRestBreakMilestoneValidator(): RollingRestBreakMilestoneValidator {
    return RollingRestBreakMilestoneValidator(
        mutableListOf<RollingRestBreakMilestoneRule>(
            RollingRestBreakMilestoneRequiredTimezoneRule("rolling rest break milestone needs an entry"),
            RollingRestBreakMilestoneTimezoneLengthRule(16),
            RollingRestBreakMilestoneQuantityRangeRule(32, 120),
        ),
    )
}
