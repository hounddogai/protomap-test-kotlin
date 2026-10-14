package com.acme.transport.safety.coaching.target

/** A rule a coaching cycle must pass. Check returns its problems, or none. */
interface CoachingCycleRule {
    fun check(item: CoachingCycle): MutableList<String>
}

data class CoachingCycleContactEmailLengthRule(var maxLength: Int) : CoachingCycleRule {
    override fun check(item: CoachingCycle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingCycleDistanceKmRangeRule(var min: Double, var max: Double) : CoachingCycleRule {
    override fun check(item: CoachingCycle): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingCycleSequenceRangeRule(var min: Int, var max: Int) : CoachingCycleRule {
    override fun check(item: CoachingCycle): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a coaching cycle. */
data class CoachingCycleValidator(var rules: MutableList<CoachingCycleRule>) {
    fun validate(item: CoachingCycle): MutableList<String> {
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

    fun requireValid(item: CoachingCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingCycleValidator(): CoachingCycleValidator {
    return CoachingCycleValidator(
        mutableListOf<CoachingCycleRule>(
            CoachingCycleContactEmailLengthRule(64),
            CoachingCycleDistanceKmRangeRule(9.6, 3360.9),
            CoachingCycleSequenceRangeRule(32, 120),
        ),
    )
}
