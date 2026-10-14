package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.text.normalize

/** A rule a legacy coaching review must pass. Check returns its problems, or none. */
interface LegacyCoachingReviewRule {
    fun check(item: LegacyCoachingReview): MutableList<String>
}

data class LegacyCoachingReviewTitleLengthRule(var maxLength: Int) : LegacyCoachingReviewRule {
    override fun check(item: LegacyCoachingReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingReviewAllowedTitleRule(var allowed: MutableList<String>) : LegacyCoachingReviewRule {
    override fun check(item: LegacyCoachingReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingReviewDurationMinutesRangeRule(var min: Int, var max: Int) : LegacyCoachingReviewRule {
    override fun check(item: LegacyCoachingReview): MutableList<String> {
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

data class LegacyCoachingReviewRequiredCodeRule(var message: String) : LegacyCoachingReviewRule {
    override fun check(item: LegacyCoachingReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingReviewCodeLengthRule(var maxLength: Int) : LegacyCoachingReviewRule {
    override fun check(item: LegacyCoachingReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy coaching review. */
data class LegacyCoachingReviewValidator(var rules: MutableList<LegacyCoachingReviewRule>) {
    fun validate(item: LegacyCoachingReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCoachingReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCoachingReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCoachingReviewValidator(): LegacyCoachingReviewValidator {
    return LegacyCoachingReviewValidator(
        mutableListOf<LegacyCoachingReviewRule>(
            LegacyCoachingReviewTitleLengthRule(3600),
            LegacyCoachingReviewAllowedTitleRule(mutableListOf<String>("regional", "batched")),
            LegacyCoachingReviewDurationMinutesRangeRule(32, 1000),
            LegacyCoachingReviewRequiredCodeRule("legacy coaching review needs an entry"),
            LegacyCoachingReviewCodeLengthRule(80),
        ),
    )
}
