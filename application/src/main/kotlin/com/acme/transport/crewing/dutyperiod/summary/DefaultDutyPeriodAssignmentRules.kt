package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.text.normalize

/** A rule a default duty period assignment must pass. Check returns its problems, or none. */
interface DefaultDutyPeriodAssignmentRule {
    fun check(item: DefaultDutyPeriodAssignment): MutableList<String>
}

data class DefaultDutyPeriodAssignmentRetriesRangeRule(
    var min: Int,
    var max: Int,
) : DefaultDutyPeriodAssignmentRule {
    override fun check(item: DefaultDutyPeriodAssignment): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class DefaultDutyPeriodAssignmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : DefaultDutyPeriodAssignmentRule {
    override fun check(item: DefaultDutyPeriodAssignment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodAssignmentAllowedReviewerRule(
    var allowed: MutableList<String>,
) : DefaultDutyPeriodAssignmentRule {
    override fun check(item: DefaultDutyPeriodAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default duty period assignment. */
data class DefaultDutyPeriodAssignmentValidator(var rules: MutableList<DefaultDutyPeriodAssignmentRule>) {
    fun validate(item: DefaultDutyPeriodAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDutyPeriodAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDutyPeriodAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDutyPeriodAssignmentValidator(): DefaultDutyPeriodAssignmentValidator {
    return DefaultDutyPeriodAssignmentValidator(
        mutableListOf<DefaultDutyPeriodAssignmentRule>(
            DefaultDutyPeriodAssignmentRetriesRangeRule(32, 255),
            DefaultDutyPeriodAssignmentDistanceKmRangeRule(5.7, 1044.6),
            DefaultDutyPeriodAssignmentAllowedReviewerRule(
                mutableListOf<String>("fallback", "global", "strict"),
            ),
        ),
    )
}
