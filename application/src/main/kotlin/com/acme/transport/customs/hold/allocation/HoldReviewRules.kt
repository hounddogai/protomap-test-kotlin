package com.acme.transport.customs.hold.allocation

/** A rule a hold review must pass. Check returns its problems, or none. */
interface HoldReviewRule {
    fun check(item: HoldReview): MutableList<String>
}

data class HoldReviewCodeLengthRule(var maxLength: Int) : HoldReviewRule {
    override fun check(item: HoldReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldReviewRateRangeRule(var min: Double, var max: Double) : HoldReviewRule {
    override fun check(item: HoldReview): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldReviewRequiredTitleRule(var message: String) : HoldReviewRule {
    override fun check(item: HoldReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold review. */
data class HoldReviewValidator(var rules: MutableList<HoldReviewRule>) {
    fun validate(item: HoldReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldReview>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldReviewValidator(): HoldReviewValidator {
    return HoldReviewValidator(
        mutableListOf<HoldReviewRule>(
            HoldReviewCodeLengthRule(80),
            HoldReviewRateRangeRule(5.9, 1762.7),
            HoldReviewRequiredTitleRule("hold review needs an entry"),
        ),
    )
}
