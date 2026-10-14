package com.acme.transport.fleetops.utilization.milestone

/** A rule a strict utilization review must pass. Check returns its problems, or none. */
interface StrictUtilizationReviewRule {
    fun check(item: StrictUtilizationReview): MutableList<String>
}

data class StrictUtilizationReviewRequiredTitleRule(var message: String) : StrictUtilizationReviewRule {
    override fun check(item: StrictUtilizationReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictUtilizationReviewTimezoneLengthRule(var maxLength: Int) : StrictUtilizationReviewRule {
    override fun check(item: StrictUtilizationReview): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictUtilizationReviewRateRangeRule(
    var min: Double,
    var max: Double,
) : StrictUtilizationReviewRule {
    override fun check(item: StrictUtilizationReview): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictUtilizationReviewCodeLengthRule(var maxLength: Int) : StrictUtilizationReviewRule {
    override fun check(item: StrictUtilizationReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict utilization review. */
data class StrictUtilizationReviewValidator(var rules: MutableList<StrictUtilizationReviewRule>) {
    fun validate(item: StrictUtilizationReview): MutableList<String> {
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

    fun requireValid(item: StrictUtilizationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictUtilizationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictUtilizationReviewValidator(): StrictUtilizationReviewValidator {
    return StrictUtilizationReviewValidator(
        mutableListOf<StrictUtilizationReviewRule>(
            StrictUtilizationReviewRequiredTitleRule("strict utilization review needs a value"),
            StrictUtilizationReviewTimezoneLengthRule(86400),
            StrictUtilizationReviewRateRangeRule(7.1, 2252.4),
            StrictUtilizationReviewCodeLengthRule(3600),
        ),
    )
}
