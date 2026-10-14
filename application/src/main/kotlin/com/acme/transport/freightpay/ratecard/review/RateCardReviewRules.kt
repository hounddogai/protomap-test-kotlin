package com.acme.transport.freightpay.ratecard.review

/** A rule a rate card review must pass. Check returns its problems, or none. */
interface RateCardReviewRule {
    fun check(item: RateCardReview): MutableList<String>
}

data class RateCardReviewCodePrefixRule(var prefix: String) : RateCardReviewRule {
    override fun check(item: RateCardReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardReviewDockLengthRule(var maxLength: Int) : RateCardReviewRule {
    override fun check(item: RateCardReview): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardReviewTitleLengthRule(var maxLength: Int) : RateCardReviewRule {
    override fun check(item: RateCardReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardReviewRequiredDockRule(var message: String) : RateCardReviewRule {
    override fun check(item: RateCardReview): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card review. */
data class RateCardReviewValidator(var rules: MutableList<RateCardReviewRule>) {
    fun validate(item: RateCardReview): MutableList<String> {
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

    fun requireValid(item: RateCardReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardReviewValidator(): RateCardReviewValidator {
    return RateCardReviewValidator(
        mutableListOf<RateCardReviewRule>(
            RateCardReviewCodePrefixRule("ACM"),
            RateCardReviewDockLengthRule(32),
            RateCardReviewTitleLengthRule(16),
            RateCardReviewRequiredDockRule("rate card review needs a value"),
        ),
    )
}
