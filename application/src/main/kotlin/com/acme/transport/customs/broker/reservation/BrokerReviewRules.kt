package com.acme.transport.customs.broker.reservation

/** A rule a broker review must pass. Check returns its problems, or none. */
interface BrokerReviewRule {
    fun check(item: BrokerReview): MutableList<String>
}

data class BrokerReviewCodePrefixRule(var prefix: String) : BrokerReviewRule {
    override fun check(item: BrokerReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerReviewTitleLengthRule(var maxLength: Int) : BrokerReviewRule {
    override fun check(item: BrokerReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerReviewExpiresAtRangeRule(var min: Int, var max: Int) : BrokerReviewRule {
    override fun check(item: BrokerReview): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker review. */
data class BrokerReviewValidator(var rules: MutableList<BrokerReviewRule>) {
    fun validate(item: BrokerReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerReview>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerReviewValidator(): BrokerReviewValidator {
    return BrokerReviewValidator(
        mutableListOf<BrokerReviewRule>(
            BrokerReviewCodePrefixRule("REF"),
            BrokerReviewTitleLengthRule(3600),
            BrokerReviewExpiresAtRangeRule(64, 80),
        ),
    )
}
