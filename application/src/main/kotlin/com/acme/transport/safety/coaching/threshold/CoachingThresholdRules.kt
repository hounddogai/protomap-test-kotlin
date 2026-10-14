package com.acme.transport.safety.coaching.threshold

/** A rule a coaching threshold must pass. Check returns its problems, or none. */
interface CoachingThresholdRule {
    fun check(item: CoachingThreshold): MutableList<String>
}

data class CoachingThresholdCreatedAtRangeRule(var min: Int, var max: Int) : CoachingThresholdRule {
    override fun check(item: CoachingThreshold): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingThresholdDurationMinutesRangeRule(var min: Int, var max: Int) : CoachingThresholdRule {
    override fun check(item: CoachingThreshold): MutableList<String> {
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

data class CoachingThresholdRequiredTrailerIdRule(var message: String) : CoachingThresholdRule {
    override fun check(item: CoachingThreshold): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingThresholdRequiredNameRule(var message: String) : CoachingThresholdRule {
    override fun check(item: CoachingThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingThresholdNameLengthRule(var maxLength: Int) : CoachingThresholdRule {
    override fun check(item: CoachingThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching threshold. */
data class CoachingThresholdValidator(var rules: MutableList<CoachingThresholdRule>) {
    fun validate(item: CoachingThreshold): MutableList<String> {
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

    fun requireValid(item: CoachingThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingThresholdValidator(): CoachingThresholdValidator {
    return CoachingThresholdValidator(
        mutableListOf<CoachingThresholdRule>(
            CoachingThresholdCreatedAtRangeRule(255, 1000),
            CoachingThresholdDurationMinutesRangeRule(120, 120),
            CoachingThresholdRequiredTrailerIdRule("coaching threshold needs a value"),
            CoachingThresholdRequiredNameRule("coaching threshold needs an entry"),
            CoachingThresholdNameLengthRule(80),
        ),
    )
}
