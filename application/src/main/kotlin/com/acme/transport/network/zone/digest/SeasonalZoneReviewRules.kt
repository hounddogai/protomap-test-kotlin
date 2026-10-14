package com.acme.transport.network.zone.digest

/** A rule a seasonal zone review must pass. Check returns its problems, or none. */
interface SeasonalZoneReviewRule {
    fun check(item: SeasonalZoneReview): MutableList<String>
}

data class SeasonalZoneReviewMarginRangeRule(var min: Double, var max: Double) : SeasonalZoneReviewRule {
    override fun check(item: SeasonalZoneReview): MutableList<String> {
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

data class SeasonalZoneReviewAttemptsRangeRule(var min: Int, var max: Int) : SeasonalZoneReviewRule {
    override fun check(item: SeasonalZoneReview): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZoneReviewRequiredNameRule(var message: String) : SeasonalZoneReviewRule {
    override fun check(item: SeasonalZoneReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZoneReviewRequiredCodeRule(var message: String) : SeasonalZoneReviewRule {
    override fun check(item: SeasonalZoneReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZoneReviewRegionLengthRule(var maxLength: Int) : SeasonalZoneReviewRule {
    override fun check(item: SeasonalZoneReview): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZoneReviewMaximumRangeRule(var min: Double, var max: Double) : SeasonalZoneReviewRule {
    override fun check(item: SeasonalZoneReview): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal zone review. */
data class SeasonalZoneReviewValidator(var rules: MutableList<SeasonalZoneReviewRule>) {
    fun validate(item: SeasonalZoneReview): MutableList<String> {
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

    fun requireValid(item: SeasonalZoneReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalZoneReview>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalZoneReviewValidator(): SeasonalZoneReviewValidator {
    return SeasonalZoneReviewValidator(
        mutableListOf<SeasonalZoneReviewRule>(
            SeasonalZoneReviewMarginRangeRule(4.4, 3673.9),
            SeasonalZoneReviewAttemptsRangeRule(64, 3600),
            SeasonalZoneReviewRequiredNameRule("seasonal zone review needs an entry"),
            SeasonalZoneReviewRequiredCodeRule("seasonal zone review needs a value"),
            SeasonalZoneReviewRegionLengthRule(80),
            SeasonalZoneReviewMaximumRangeRule(7.3, 3336.6),
        ),
    )
}
