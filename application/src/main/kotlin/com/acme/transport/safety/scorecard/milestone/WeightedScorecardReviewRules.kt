package com.acme.transport.safety.scorecard.milestone

/** A rule a weighted scorecard review must pass. Check returns its problems, or none. */
interface WeightedScorecardReviewRule {
    fun check(item: WeightedScorecardReview): MutableList<String>
}

data class WeightedScorecardReviewRequiredNotesRule(var message: String) : WeightedScorecardReviewRule {
    override fun check(item: WeightedScorecardReview): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardReviewRatioRangeRule(
    var min: Double,
    var max: Double,
) : WeightedScorecardReviewRule {
    override fun check(item: WeightedScorecardReview): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class WeightedScorecardReviewCarrierLengthRule(var maxLength: Int) : WeightedScorecardReviewRule {
    override fun check(item: WeightedScorecardReview): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted scorecard review. */
data class WeightedScorecardReviewValidator(var rules: MutableList<WeightedScorecardReviewRule>) {
    fun validate(item: WeightedScorecardReview): MutableList<String> {
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

    fun requireValid(item: WeightedScorecardReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedScorecardReview>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedScorecardReviewValidator(): WeightedScorecardReviewValidator {
    return WeightedScorecardReviewValidator(
        mutableListOf<WeightedScorecardReviewRule>(
            WeightedScorecardReviewRequiredNotesRule("weighted scorecard review needs an entry"),
            WeightedScorecardReviewRatioRangeRule(6.0, 3015.7),
            WeightedScorecardReviewCarrierLengthRule(1000),
        ),
    )
}
