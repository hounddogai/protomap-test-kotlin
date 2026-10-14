package com.acme.transport.crewing.availability.score

/** A rule a lenient availability review must pass. Check returns its problems, or none. */
interface LenientAvailabilityReviewRule {
    fun check(item: LenientAvailabilityReview): MutableList<String>
}

data class LenientAvailabilityReviewFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : LenientAvailabilityReviewRule {
    override fun check(item: LenientAvailabilityReview): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityReviewRequiredCodeRule(var message: String) : LenientAvailabilityReviewRule {
    override fun check(item: LenientAvailabilityReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityReviewCodeLengthRule(var maxLength: Int) : LenientAvailabilityReviewRule {
    override fun check(item: LenientAvailabilityReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient availability review. */
data class LenientAvailabilityReviewValidator(var rules: MutableList<LenientAvailabilityReviewRule>) {
    fun validate(item: LenientAvailabilityReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAvailabilityReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAvailabilityReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAvailabilityReviewValidator(): LenientAvailabilityReviewValidator {
    return LenientAvailabilityReviewValidator(
        mutableListOf<LenientAvailabilityReviewRule>(
            LenientAvailabilityReviewFloorValueRangeRule(0.5, 4584.8),
            LenientAvailabilityReviewRequiredCodeRule("lenient availability review needs a value"),
            LenientAvailabilityReviewCodeLengthRule(64),
        ),
    )
}
