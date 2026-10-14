package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize

/** A rule a coaching review must pass. Check returns its problems, or none. */
interface CoachingReviewRule {
    fun check(item: CoachingReview): MutableList<String>
}

data class CoachingReviewTitleLengthRule(var maxLength: Int) : CoachingReviewRule {
    override fun check(item: CoachingReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingReviewCurrencyLengthRule(var maxLength: Int) : CoachingReviewRule {
    override fun check(item: CoachingReview): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingReviewCodeLengthRule(var maxLength: Int) : CoachingReviewRule {
    override fun check(item: CoachingReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingReviewAllowedCurrencyRule(var allowed: MutableList<String>) : CoachingReviewRule {
    override fun check(item: CoachingReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is expired")
        }
        return mutableListOf<String>()
    }
}

data class CoachingReviewSequenceRangeRule(var min: Int, var max: Int) : CoachingReviewRule {
    override fun check(item: CoachingReview): MutableList<String> {
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

data class CoachingReviewPalletsRangeRule(var min: Int, var max: Int) : CoachingReviewRule {
    override fun check(item: CoachingReview): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a coaching review. */
data class CoachingReviewValidator(var rules: MutableList<CoachingReviewRule>) {
    fun validate(item: CoachingReview): MutableList<String> {
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

    fun requireValid(item: CoachingReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingReview>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingReviewValidator(): CoachingReviewValidator {
    return CoachingReviewValidator(
        mutableListOf<CoachingReviewRule>(
            CoachingReviewTitleLengthRule(16),
            CoachingReviewCurrencyLengthRule(86400),
            CoachingReviewCodeLengthRule(1000),
            CoachingReviewAllowedCurrencyRule(mutableListOf<String>("legacy", "capped", "weighted")),
            CoachingReviewSequenceRangeRule(32, 120),
            CoachingReviewPalletsRangeRule(16, 120),
        ),
    )
}
