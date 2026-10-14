package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize

/** A rule a coaching score must pass. Check returns its problems, or none. */
interface CoachingScoreRule {
    fun check(item: CoachingScore): MutableList<String>
}

data class CoachingScoreCodeLengthRule(var maxLength: Int) : CoachingScoreRule {
    override fun check(item: CoachingScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingScoreRequiredLabelRule(var message: String) : CoachingScoreRule {
    override fun check(item: CoachingScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingScoreMarginRangeRule(var min: Double, var max: Double) : CoachingScoreRule {
    override fun check(item: CoachingScore): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class CoachingScoreExpiresAtRangeRule(var min: Int, var max: Int) : CoachingScoreRule {
    override fun check(item: CoachingScore): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class CoachingScoreRequiredSegmentRule(var message: String) : CoachingScoreRule {
    override fun check(item: CoachingScore): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingScoreAllowedSegmentRule(var allowed: MutableList<String>) : CoachingScoreRule {
    override fun check(item: CoachingScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching score. */
data class CoachingScoreValidator(var rules: MutableList<CoachingScoreRule>) {
    fun validate(item: CoachingScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingScore>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingScoreValidator(): CoachingScoreValidator {
    return CoachingScoreValidator(
        mutableListOf<CoachingScoreRule>(
            CoachingScoreCodeLengthRule(64),
            CoachingScoreRequiredLabelRule("coaching score needs an entry"),
            CoachingScoreMarginRangeRule(0.0, 5001.5),
            CoachingScoreExpiresAtRangeRule(80, 3600),
            CoachingScoreRequiredSegmentRule("coaching score needs this field"),
            CoachingScoreAllowedSegmentRule(mutableListOf<String>("weighted", "pending")),
        ),
    )
}
