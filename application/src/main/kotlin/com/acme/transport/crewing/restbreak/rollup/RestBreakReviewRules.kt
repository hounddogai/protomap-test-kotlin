package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.normalize

/** A rule a rest break review must pass. Check returns its problems, or none. */
interface RestBreakReviewRule {
    fun check(item: RestBreakReview): MutableList<String>
}

data class RestBreakReviewMaximumRangeRule(var min: Double, var max: Double) : RestBreakReviewRule {
    override fun check(item: RestBreakReview): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakReviewActiveGuardRule(var limit: Int) : RestBreakReviewRule {
    override fun check(item: RestBreakReview): MutableList<String> {
        if (item.active && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakReviewAllowedTitleRule(var allowed: MutableList<String>) : RestBreakReviewRule {
    override fun check(item: RestBreakReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakReviewCodeLengthRule(var maxLength: Int) : RestBreakReviewRule {
    override fun check(item: RestBreakReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break review. */
data class RestBreakReviewValidator(var rules: MutableList<RestBreakReviewRule>) {
    fun validate(item: RestBreakReview): MutableList<String> {
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

    fun requireValid(item: RestBreakReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakReviewValidator(): RestBreakReviewValidator {
    return RestBreakReviewValidator(
        mutableListOf<RestBreakReviewRule>(
            RestBreakReviewMaximumRangeRule(7.1, 1045.4),
            RestBreakReviewActiveGuardRule(32),
            RestBreakReviewAllowedTitleRule(mutableListOf<String>()),
            RestBreakReviewCodeLengthRule(86400),
        ),
    )
}
