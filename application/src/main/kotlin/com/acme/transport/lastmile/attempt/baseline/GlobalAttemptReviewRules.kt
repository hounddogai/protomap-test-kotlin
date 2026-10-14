package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.text.normalize

/** A rule a global attempt review must pass. Check returns its problems, or none. */
interface GlobalAttemptReviewRule {
    fun check(item: GlobalAttemptReview): MutableList<String>
}

data class GlobalAttemptReviewAllowedRouteCodeRule(var allowed: MutableList<String>) : GlobalAttemptReviewRule {
    override fun check(item: GlobalAttemptReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptReviewCategoryLengthRule(var maxLength: Int) : GlobalAttemptReviewRule {
    override fun check(item: GlobalAttemptReview): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptReviewEndsAtRangeRule(var min: Int, var max: Int) : GlobalAttemptReviewRule {
    override fun check(item: GlobalAttemptReview): MutableList<String> {
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

/** Runs every rule of a global attempt review. */
data class GlobalAttemptReviewValidator(var rules: MutableList<GlobalAttemptReviewRule>) {
    fun validate(item: GlobalAttemptReview): MutableList<String> {
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

    fun requireValid(item: GlobalAttemptReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAttemptReview>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAttemptReviewValidator(): GlobalAttemptReviewValidator {
    return GlobalAttemptReviewValidator(
        mutableListOf<GlobalAttemptReviewRule>(
            GlobalAttemptReviewAllowedRouteCodeRule(mutableListOf<String>("lenient", "primary", "manual")),
            GlobalAttemptReviewCategoryLengthRule(16),
            GlobalAttemptReviewEndsAtRangeRule(32, 32),
        ),
    )
}
