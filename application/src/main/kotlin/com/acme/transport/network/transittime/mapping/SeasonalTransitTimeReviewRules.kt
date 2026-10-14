package com.acme.transport.network.transittime.mapping

/** A rule a seasonal transit time review must pass. Check returns its problems, or none. */
interface SeasonalTransitTimeReviewRule {
    fun check(item: SeasonalTransitTimeReview): MutableList<String>
}

data class SeasonalTransitTimeReviewScoreRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTransitTimeReviewRule {
    override fun check(item: SeasonalTransitTimeReview): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeReviewLabelLengthRule(var maxLength: Int) : SeasonalTransitTimeReviewRule {
    override fun check(item: SeasonalTransitTimeReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeReviewTitleLengthRule(var maxLength: Int) : SeasonalTransitTimeReviewRule {
    override fun check(item: SeasonalTransitTimeReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeReviewRequiredLabelRule(var message: String) : SeasonalTransitTimeReviewRule {
    override fun check(item: SeasonalTransitTimeReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeReviewRequiredCodeRule(var message: String) : SeasonalTransitTimeReviewRule {
    override fun check(item: SeasonalTransitTimeReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeReviewActiveGuardRule(var limit: Double) : SeasonalTransitTimeReviewRule {
    override fun check(item: SeasonalTransitTimeReview): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal transit time review. */
data class SeasonalTransitTimeReviewValidator(var rules: MutableList<SeasonalTransitTimeReviewRule>) {
    fun validate(item: SeasonalTransitTimeReview): MutableList<String> {
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

    fun requireValid(item: SeasonalTransitTimeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTransitTimeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTransitTimeReviewValidator(): SeasonalTransitTimeReviewValidator {
    return SeasonalTransitTimeReviewValidator(
        mutableListOf<SeasonalTransitTimeReviewRule>(
            SeasonalTransitTimeReviewScoreRangeRule(5.5, 3373.9),
            SeasonalTransitTimeReviewLabelLengthRule(64),
            SeasonalTransitTimeReviewTitleLengthRule(86400),
            SeasonalTransitTimeReviewRequiredLabelRule("seasonal transit time review needs a value"),
            SeasonalTransitTimeReviewRequiredCodeRule("seasonal transit time review needs a value"),
            SeasonalTransitTimeReviewActiveGuardRule(9.6),
        ),
    )
}
