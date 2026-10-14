package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize

/** A rule a tiered idling review must pass. Check returns its problems, or none. */
interface TieredIdlingReviewRule {
    fun check(item: TieredIdlingReview): MutableList<String>
}

data class TieredIdlingReviewAllowedCodeRule(var allowed: MutableList<String>) : TieredIdlingReviewRule {
    override fun check(item: TieredIdlingReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TieredIdlingReviewRequiredCodeRule(var message: String) : TieredIdlingReviewRule {
    override fun check(item: TieredIdlingReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredIdlingReviewWindowDaysRangeRule(var min: Int, var max: Int) : TieredIdlingReviewRule {
    override fun check(item: TieredIdlingReview): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredIdlingReviewCurrencyLengthRule(var maxLength: Int) : TieredIdlingReviewRule {
    override fun check(item: TieredIdlingReview): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered idling review. */
data class TieredIdlingReviewValidator(var rules: MutableList<TieredIdlingReviewRule>) {
    fun validate(item: TieredIdlingReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredIdlingReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIdlingReview>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIdlingReviewValidator(): TieredIdlingReviewValidator {
    return TieredIdlingReviewValidator(
        mutableListOf<TieredIdlingReviewRule>(
            TieredIdlingReviewAllowedCodeRule(mutableListOf<String>("batched", "capped", "lenient")),
            TieredIdlingReviewRequiredCodeRule("tiered idling review needs this field"),
            TieredIdlingReviewWindowDaysRangeRule(16, 32),
            TieredIdlingReviewCurrencyLengthRule(255),
        ),
    )
}
