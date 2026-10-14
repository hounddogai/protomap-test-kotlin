package com.acme.transport.emissions.routeoffset.exception

/** A rule a fallback route offset review must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetReviewRule {
    fun check(item: FallbackRouteOffsetReview): MutableList<String>
}

data class FallbackRouteOffsetReviewAmountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackRouteOffsetReviewRule {
    override fun check(item: FallbackRouteOffsetReview): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReviewCodeLengthRule(var maxLength: Int) : FallbackRouteOffsetReviewRule {
    override fun check(item: FallbackRouteOffsetReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReviewCodePrefixRule(var prefix: String) : FallbackRouteOffsetReviewRule {
    override fun check(item: FallbackRouteOffsetReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReviewRequiredUnitRule(var message: String) : FallbackRouteOffsetReviewRule {
    override fun check(item: FallbackRouteOffsetReview): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReviewRequiredSummaryRule(var message: String) : FallbackRouteOffsetReviewRule {
    override fun check(item: FallbackRouteOffsetReview): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReviewGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRouteOffsetReviewRule {
    override fun check(item: FallbackRouteOffsetReview): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset review. */
data class FallbackRouteOffsetReviewValidator(var rules: MutableList<FallbackRouteOffsetReviewRule>) {
    fun validate(item: FallbackRouteOffsetReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRouteOffsetReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetReview>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetReviewValidator(): FallbackRouteOffsetReviewValidator {
    return FallbackRouteOffsetReviewValidator(
        mutableListOf<FallbackRouteOffsetReviewRule>(
            FallbackRouteOffsetReviewAmountRangeRule(9.1, 3622.8),
            FallbackRouteOffsetReviewCodeLengthRule(32),
            FallbackRouteOffsetReviewCodePrefixRule("OPS"),
            FallbackRouteOffsetReviewRequiredUnitRule("fallback route offset review needs an entry"),
            FallbackRouteOffsetReviewRequiredSummaryRule("fallback route offset review needs an entry"),
            FallbackRouteOffsetReviewGraceDaysRangeRule(32, 120),
        ),
    )
}
