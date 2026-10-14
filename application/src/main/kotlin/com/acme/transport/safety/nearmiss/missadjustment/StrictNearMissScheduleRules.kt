package com.acme.transport.safety.nearmiss.missadjustment

/** A rule a strict near miss schedule must pass. Check returns its problems, or none. */
interface StrictNearMissScheduleRule {
    fun check(item: StrictNearMissSchedule): MutableList<String>
}

data class StrictNearMissScheduleChannelLengthRule(var maxLength: Int) : StrictNearMissScheduleRule {
    override fun check(item: StrictNearMissSchedule): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissScheduleCreatedAtRangeRule(var min: Int, var max: Int) : StrictNearMissScheduleRule {
    override fun check(item: StrictNearMissSchedule): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissScheduleRequiredCategoryRule(var message: String) : StrictNearMissScheduleRule {
    override fun check(item: StrictNearMissSchedule): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict near miss schedule. */
data class StrictNearMissScheduleValidator(var rules: MutableList<StrictNearMissScheduleRule>) {
    fun validate(item: StrictNearMissSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictNearMissSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNearMissSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNearMissScheduleValidator(): StrictNearMissScheduleValidator {
    return StrictNearMissScheduleValidator(
        mutableListOf<StrictNearMissScheduleRule>(
            StrictNearMissScheduleChannelLengthRule(120),
            StrictNearMissScheduleCreatedAtRangeRule(255, 1000),
            StrictNearMissScheduleRequiredCategoryRule("strict near miss schedule needs an entry"),
        ),
    )
}
