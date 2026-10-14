package com.acme.transport.safety.coaching.segment

/** A rule a coaching segment must pass. Check returns its problems, or none. */
interface CoachingSegmentRule {
    fun check(item: CoachingSegment): MutableList<String>
}

data class CoachingSegmentTitleLengthRule(var maxLength: Int) : CoachingSegmentRule {
    override fun check(item: CoachingSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSegmentRequiredTitleRule(var message: String) : CoachingSegmentRule {
    override fun check(item: CoachingSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingSegmentLoadKgRangeRule(var min: Double, var max: Double) : CoachingSegmentRule {
    override fun check(item: CoachingSegment): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class CoachingSegmentCodeLengthRule(var maxLength: Int) : CoachingSegmentRule {
    override fun check(item: CoachingSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSegmentWindowDaysRangeRule(var min: Int, var max: Int) : CoachingSegmentRule {
    override fun check(item: CoachingSegment): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSegmentThresholdRangeRule(var min: Double, var max: Double) : CoachingSegmentRule {
    override fun check(item: CoachingSegment): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a coaching segment. */
data class CoachingSegmentValidator(var rules: MutableList<CoachingSegmentRule>) {
    fun validate(item: CoachingSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingSegmentValidator(): CoachingSegmentValidator {
    return CoachingSegmentValidator(
        mutableListOf<CoachingSegmentRule>(
            CoachingSegmentTitleLengthRule(16),
            CoachingSegmentRequiredTitleRule("coaching segment needs this field"),
            CoachingSegmentLoadKgRangeRule(9.3, 3508.5),
            CoachingSegmentCodeLengthRule(120),
            CoachingSegmentWindowDaysRangeRule(32, 3600),
            CoachingSegmentThresholdRangeRule(7.1, 3374.8),
        ),
    )
}
