package com.acme.transport.freightpay.dispute.review

/** A rule a dispute review must pass. Check returns its problems, or none. */
interface DisputeReviewRule {
    fun check(item: DisputeReview): MutableList<String>
}

data class DisputeReviewSummaryLengthRule(var maxLength: Int) : DisputeReviewRule {
    override fun check(item: DisputeReview): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeReviewStartsAtRangeRule(var min: Int, var max: Int) : DisputeReviewRule {
    override fun check(item: DisputeReview): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeReviewGraceDaysRangeRule(var min: Int, var max: Int) : DisputeReviewRule {
    override fun check(item: DisputeReview): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dispute review. */
data class DisputeReviewValidator(var rules: MutableList<DisputeReviewRule>) {
    fun validate(item: DisputeReview): MutableList<String> {
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

    fun requireValid(item: DisputeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeReviewValidator(): DisputeReviewValidator {
    return DisputeReviewValidator(
        mutableListOf<DisputeReviewRule>(
            DisputeReviewSummaryLengthRule(255),
            DisputeReviewStartsAtRangeRule(255, 1000),
            DisputeReviewGraceDaysRangeRule(64, 255),
        ),
    )
}
