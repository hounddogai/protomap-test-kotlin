package com.acme.transport.safety.coaching.metric

/** A rule a coaching metric must pass. Check returns its problems, or none. */
interface CoachingMetricRule {
    fun check(item: CoachingMetric): MutableList<String>
}

data class CoachingMetricArchivedGuardRule(var limit: Double) : CoachingMetricRule {
    override fun check(item: CoachingMetric): MutableList<String> {
        if (item.archived && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMetricCodeLengthRule(var maxLength: Int) : CoachingMetricRule {
    override fun check(item: CoachingMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMetricUpdatedAtRangeRule(var min: Int, var max: Int) : CoachingMetricRule {
    override fun check(item: CoachingMetric): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a coaching metric. */
data class CoachingMetricValidator(var rules: MutableList<CoachingMetricRule>) {
    fun validate(item: CoachingMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingMetricValidator(): CoachingMetricValidator {
    return CoachingMetricValidator(
        mutableListOf<CoachingMetricRule>(
            CoachingMetricArchivedGuardRule(3.3),
            CoachingMetricCodeLengthRule(64),
            CoachingMetricUpdatedAtRangeRule(255, 255),
        ),
    )
}
