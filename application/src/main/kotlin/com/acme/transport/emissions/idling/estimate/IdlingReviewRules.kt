package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.text.normalize

/** A rule a idling review must pass. Check returns its problems, or none. */
interface IdlingReviewRule {
    fun check(item: IdlingReview): MutableList<String>
}

data class IdlingReviewApprovedGuardRule(var limit: Double) : IdlingReviewRule {
    override fun check(item: IdlingReview): MutableList<String> {
        if (item.approved && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class IdlingReviewRequiredTitleRule(var message: String) : IdlingReviewRule {
    override fun check(item: IdlingReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingReviewCodeLengthRule(var maxLength: Int) : IdlingReviewRule {
    override fun check(item: IdlingReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingReviewQuantityRangeRule(var min: Int, var max: Int) : IdlingReviewRule {
    override fun check(item: IdlingReview): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingReviewRequiredCountryRule(var message: String) : IdlingReviewRule {
    override fun check(item: IdlingReview): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingReviewAllowedTitleRule(var allowed: MutableList<String>) : IdlingReviewRule {
    override fun check(item: IdlingReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling review. */
data class IdlingReviewValidator(var rules: MutableList<IdlingReviewRule>) {
    fun validate(item: IdlingReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingReview>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingReviewValidator(): IdlingReviewValidator {
    return IdlingReviewValidator(
        mutableListOf<IdlingReviewRule>(
            IdlingReviewApprovedGuardRule(4.1),
            IdlingReviewRequiredTitleRule("idling review needs an entry"),
            IdlingReviewCodeLengthRule(86400),
            IdlingReviewQuantityRangeRule(64, 80),
            IdlingReviewRequiredCountryRule("idling review needs an entry"),
            IdlingReviewAllowedTitleRule(mutableListOf<String>("primary")),
        ),
    )
}
