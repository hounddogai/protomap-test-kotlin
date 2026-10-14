package com.acme.transport.safety.scorecard.scorecardbudget

/** A rule a scheduled scorecard schedule must pass. Check returns its problems, or none. */
interface ScheduledScorecardScheduleRule {
    fun check(item: ScheduledScorecardSchedule): MutableList<String>
}

data class ScheduledScorecardScheduleSourceLengthRule(var maxLength: Int) : ScheduledScorecardScheduleRule {
    override fun check(item: ScheduledScorecardSchedule): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardScheduleScoreRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledScorecardScheduleRule {
    override fun check(item: ScheduledScorecardSchedule): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ScheduledScorecardScheduleReviewerLengthRule(var maxLength: Int) : ScheduledScorecardScheduleRule {
    override fun check(item: ScheduledScorecardSchedule): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardScheduleCodeLengthRule(var maxLength: Int) : ScheduledScorecardScheduleRule {
    override fun check(item: ScheduledScorecardSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardScheduleDiscountRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledScorecardScheduleRule {
    override fun check(item: ScheduledScorecardSchedule): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class ScheduledScorecardScheduleGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledScorecardScheduleRule {
    override fun check(item: ScheduledScorecardSchedule): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled scorecard schedule. */
data class ScheduledScorecardScheduleValidator(var rules: MutableList<ScheduledScorecardScheduleRule>) {
    fun validate(item: ScheduledScorecardSchedule): MutableList<String> {
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

    fun requireValid(item: ScheduledScorecardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledScorecardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledScorecardScheduleValidator(): ScheduledScorecardScheduleValidator {
    return ScheduledScorecardScheduleValidator(
        mutableListOf<ScheduledScorecardScheduleRule>(
            ScheduledScorecardScheduleSourceLengthRule(255),
            ScheduledScorecardScheduleScoreRangeRule(7.5, 260.7),
            ScheduledScorecardScheduleReviewerLengthRule(120),
            ScheduledScorecardScheduleCodeLengthRule(32),
            ScheduledScorecardScheduleDiscountRangeRule(4.3, 2591.8),
            ScheduledScorecardScheduleGraceDaysRangeRule(16, 255),
        ),
    )
}
