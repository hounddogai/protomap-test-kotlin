package com.acme.transport.safety.coaching.schedule

/** A rule a coaching record must pass. Check returns its problems, or none. */
interface CoachingRecordRule {
    fun check(item: CoachingRecord): MutableList<String>
}

data class CoachingRecordRequiredReviewerRule(var message: String) : CoachingRecordRule {
    override fun check(item: CoachingRecord): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingRecordRatioRangeRule(var min: Double, var max: Double) : CoachingRecordRule {
    override fun check(item: CoachingRecord): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingRecordRetriesRangeRule(var min: Int, var max: Int) : CoachingRecordRule {
    override fun check(item: CoachingRecord): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching record. */
data class CoachingRecordValidator(var rules: MutableList<CoachingRecordRule>) {
    fun validate(item: CoachingRecord): MutableList<String> {
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

    fun requireValid(item: CoachingRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingRecordValidator(): CoachingRecordValidator {
    return CoachingRecordValidator(
        mutableListOf<CoachingRecordRule>(
            CoachingRecordRequiredReviewerRule("coaching record needs an entry"),
            CoachingRecordRatioRangeRule(8.4, 1385.5),
            CoachingRecordRetriesRangeRule(80, 1000),
        ),
    )
}
