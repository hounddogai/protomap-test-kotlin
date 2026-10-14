package com.acme.transport.emissions.evcharge.review

/** A rule a adaptive ev charge review must pass. Check returns its problems, or none. */
interface AdaptiveEvChargeReviewRule {
    fun check(item: AdaptiveEvChargeReview): MutableList<String>
}

data class AdaptiveEvChargeReviewEndsAtRangeRule(var min: Int, var max: Int) : AdaptiveEvChargeReviewRule {
    override fun check(item: AdaptiveEvChargeReview): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveEvChargeReviewRetriesRangeRule(var min: Int, var max: Int) : AdaptiveEvChargeReviewRule {
    override fun check(item: AdaptiveEvChargeReview): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeReviewRequiredSegmentRule(var message: String) : AdaptiveEvChargeReviewRule {
    override fun check(item: AdaptiveEvChargeReview): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeReviewCodePrefixRule(var prefix: String) : AdaptiveEvChargeReviewRule {
    override fun check(item: AdaptiveEvChargeReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive ev charge review. */
data class AdaptiveEvChargeReviewValidator(var rules: MutableList<AdaptiveEvChargeReviewRule>) {
    fun validate(item: AdaptiveEvChargeReview): MutableList<String> {
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

    fun requireValid(item: AdaptiveEvChargeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEvChargeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEvChargeReviewValidator(): AdaptiveEvChargeReviewValidator {
    return AdaptiveEvChargeReviewValidator(
        mutableListOf<AdaptiveEvChargeReviewRule>(
            AdaptiveEvChargeReviewEndsAtRangeRule(32, 3600),
            AdaptiveEvChargeReviewRetriesRangeRule(32, 255),
            AdaptiveEvChargeReviewRequiredSegmentRule("adaptive ev charge review needs this field"),
            AdaptiveEvChargeReviewCodePrefixRule("REF"),
        ),
    )
}
