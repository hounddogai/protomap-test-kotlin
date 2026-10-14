package com.acme.transport.linehaul.trailer.milestone

/** A rule a trailer review must pass. Check returns its problems, or none. */
interface TrailerReviewRule {
    fun check(item: TrailerReview): MutableList<String>
}

data class TrailerReviewRequiredCodeRule(var message: String) : TrailerReviewRule {
    override fun check(item: TrailerReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerReviewCodePrefixRule(var prefix: String) : TrailerReviewRule {
    override fun check(item: TrailerReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerReviewChecksumLengthRule(var maxLength: Int) : TrailerReviewRule {
    override fun check(item: TrailerReview): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer review. */
data class TrailerReviewValidator(var rules: MutableList<TrailerReviewRule>) {
    fun validate(item: TrailerReview): MutableList<String> {
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

    fun requireValid(item: TrailerReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerReview>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerReviewValidator(): TrailerReviewValidator {
    return TrailerReviewValidator(
        mutableListOf<TrailerReviewRule>(
            TrailerReviewRequiredCodeRule("trailer review needs this field"),
            TrailerReviewCodePrefixRule("ACM"),
            TrailerReviewChecksumLengthRule(64),
        ),
    )
}
