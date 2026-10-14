package com.acme.transport.network.transittime.threshold

/** A rule a transit time review must pass. Check returns its problems, or none. */
interface TransitTimeReviewRule {
    fun check(item: TransitTimeReview): MutableList<String>
}

data class TransitTimeReviewRequiredChecksumRule(var message: String) : TransitTimeReviewRule {
    override fun check(item: TransitTimeReview): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeReviewChecksumLengthRule(var maxLength: Int) : TransitTimeReviewRule {
    override fun check(item: TransitTimeReview): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeReviewCodeLengthRule(var maxLength: Int) : TransitTimeReviewRule {
    override fun check(item: TransitTimeReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeReviewCodePrefixRule(var prefix: String) : TransitTimeReviewRule {
    override fun check(item: TransitTimeReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeReviewRatioRangeRule(var min: Double, var max: Double) : TransitTimeReviewRule {
    override fun check(item: TransitTimeReview): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeReviewMarginRangeRule(var min: Double, var max: Double) : TransitTimeReviewRule {
    override fun check(item: TransitTimeReview): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time review. */
data class TransitTimeReviewValidator(var rules: MutableList<TransitTimeReviewRule>) {
    fun validate(item: TransitTimeReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeReviewValidator(): TransitTimeReviewValidator {
    return TransitTimeReviewValidator(
        mutableListOf<TransitTimeReviewRule>(
            TransitTimeReviewRequiredChecksumRule("transit time review needs an entry"),
            TransitTimeReviewChecksumLengthRule(16),
            TransitTimeReviewCodeLengthRule(64),
            TransitTimeReviewCodePrefixRule("ACM"),
            TransitTimeReviewRatioRangeRule(8.3, 4352.3),
            TransitTimeReviewMarginRangeRule(7.2, 1688.5),
        ),
    )
}
