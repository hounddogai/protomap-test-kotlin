package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.text.normalize

/** A rule a global coaching baseline must pass. Check returns its problems, or none. */
interface GlobalCoachingBaselineRule {
    fun check(item: GlobalCoachingBaseline): MutableList<String>
}

data class GlobalCoachingBaselineEndsAtRangeRule(var min: Int, var max: Int) : GlobalCoachingBaselineRule {
    override fun check(item: GlobalCoachingBaseline): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class GlobalCoachingBaselineThresholdRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCoachingBaselineRule {
    override fun check(item: GlobalCoachingBaseline): MutableList<String> {
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

data class GlobalCoachingBaselineAllowedReviewerRule(
    var allowed: MutableList<String>,
) : GlobalCoachingBaselineRule {
    override fun check(item: GlobalCoachingBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global coaching baseline. */
data class GlobalCoachingBaselineValidator(var rules: MutableList<GlobalCoachingBaselineRule>) {
    fun validate(item: GlobalCoachingBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCoachingBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCoachingBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCoachingBaselineValidator(): GlobalCoachingBaselineValidator {
    return GlobalCoachingBaselineValidator(
        mutableListOf<GlobalCoachingBaselineRule>(
            GlobalCoachingBaselineEndsAtRangeRule(3600, 86400),
            GlobalCoachingBaselineThresholdRangeRule(1.5, 841.3),
            GlobalCoachingBaselineAllowedReviewerRule(mutableListOf<String>("seasonal", "batched")),
        ),
    )
}
