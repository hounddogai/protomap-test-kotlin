package com.acme.transport.safety.coaching.schedule

/** A rule a coaching schedule must pass. Check returns its problems, or none. */
interface CoachingScheduleRule {
    fun check(item: CoachingSchedule): MutableList<String>
}

data class CoachingScheduleCarrierLengthRule(var maxLength: Int) : CoachingScheduleRule {
    override fun check(item: CoachingSchedule): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSchedulePhoneLengthRule(var maxLength: Int) : CoachingScheduleRule {
    override fun check(item: CoachingSchedule): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingScheduleStartsAtRangeRule(var min: Int, var max: Int) : CoachingScheduleRule {
    override fun check(item: CoachingSchedule): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingScheduleTitleLengthRule(var maxLength: Int) : CoachingScheduleRule {
    override fun check(item: CoachingSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSchedulePriorityRangeRule(var min: Int, var max: Int) : CoachingScheduleRule {
    override fun check(item: CoachingSchedule): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a coaching schedule. */
data class CoachingScheduleValidator(var rules: MutableList<CoachingScheduleRule>) {
    fun validate(item: CoachingSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingScheduleValidator(): CoachingScheduleValidator {
    return CoachingScheduleValidator(
        mutableListOf<CoachingScheduleRule>(
            CoachingScheduleCarrierLengthRule(3600),
            CoachingSchedulePhoneLengthRule(64),
            CoachingScheduleStartsAtRangeRule(255, 1000),
            CoachingScheduleTitleLengthRule(1000),
            CoachingSchedulePriorityRangeRule(255, 1000),
        ),
    )
}
