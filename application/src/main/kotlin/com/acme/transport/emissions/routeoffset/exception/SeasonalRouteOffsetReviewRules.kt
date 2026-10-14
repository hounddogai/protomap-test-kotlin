package com.acme.transport.emissions.routeoffset.exception

/** A rule a seasonal route offset review must pass. Check returns its problems, or none. */
interface SeasonalRouteOffsetReviewRule {
    fun check(item: SeasonalRouteOffsetReview): MutableList<String>
}

data class SeasonalRouteOffsetReviewCeilingRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRouteOffsetReviewRule {
    override fun check(item: SeasonalRouteOffsetReview): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class SeasonalRouteOffsetReviewRequiredCodeRule(var message: String) : SeasonalRouteOffsetReviewRule {
    override fun check(item: SeasonalRouteOffsetReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetReviewShiftCodeLengthRule(var maxLength: Int) : SeasonalRouteOffsetReviewRule {
    override fun check(item: SeasonalRouteOffsetReview): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal route offset review. */
data class SeasonalRouteOffsetReviewValidator(var rules: MutableList<SeasonalRouteOffsetReviewRule>) {
    fun validate(item: SeasonalRouteOffsetReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRouteOffsetReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRouteOffsetReview>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRouteOffsetReviewValidator(): SeasonalRouteOffsetReviewValidator {
    return SeasonalRouteOffsetReviewValidator(
        mutableListOf<SeasonalRouteOffsetReviewRule>(
            SeasonalRouteOffsetReviewCeilingRangeRule(6.2, 1764.2),
            SeasonalRouteOffsetReviewRequiredCodeRule("seasonal route offset review needs this field"),
            SeasonalRouteOffsetReviewShiftCodeLengthRule(80),
        ),
    )
}
