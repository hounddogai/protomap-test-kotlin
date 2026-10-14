package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard review must pass. Check returns its problems, or none. */
interface TrailerYardReviewRule {
    fun check(item: TrailerYardReview): MutableList<String>
}

data class TrailerYardReviewRequiredLocaleRule(var message: String) : TrailerYardReviewRule {
    override fun check(item: TrailerYardReview): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardReviewRequiredLabelRule(var message: String) : TrailerYardReviewRule {
    override fun check(item: TrailerYardReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardReviewCodePrefixRule(var prefix: String) : TrailerYardReviewRule {
    override fun check(item: TrailerYardReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardReviewLocaleLengthRule(var maxLength: Int) : TrailerYardReviewRule {
    override fun check(item: TrailerYardReview): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardReviewAllowedSegmentRule(var allowed: MutableList<String>) : TrailerYardReviewRule {
    override fun check(item: TrailerYardReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard review. */
data class TrailerYardReviewValidator(var rules: MutableList<TrailerYardReviewRule>) {
    fun validate(item: TrailerYardReview): MutableList<String> {
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

    fun requireValid(item: TrailerYardReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardReview>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardReviewValidator(): TrailerYardReviewValidator {
    return TrailerYardReviewValidator(
        mutableListOf<TrailerYardReviewRule>(
            TrailerYardReviewRequiredLocaleRule("trailer yard review needs this field"),
            TrailerYardReviewRequiredLabelRule("trailer yard review needs an entry"),
            TrailerYardReviewCodePrefixRule("ACM"),
            TrailerYardReviewLocaleLengthRule(64),
            TrailerYardReviewAllowedSegmentRule(mutableListOf<String>("regional", "seasonal", "legacy")),
        ),
    )
}
