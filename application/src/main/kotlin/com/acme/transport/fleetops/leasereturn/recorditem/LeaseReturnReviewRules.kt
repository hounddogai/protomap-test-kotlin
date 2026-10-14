package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a lease return review must pass. Check returns its problems, or none. */
interface LeaseReturnReviewRule {
    fun check(item: LeaseReturnReview): MutableList<String>
}

data class LeaseReturnReviewRequiredTitleRule(var message: String) : LeaseReturnReviewRule {
    override fun check(item: LeaseReturnReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnReviewRequiredCodeRule(var message: String) : LeaseReturnReviewRule {
    override fun check(item: LeaseReturnReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnReviewAllowedSegmentRule(var allowed: MutableList<String>) : LeaseReturnReviewRule {
    override fun check(item: LeaseReturnReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnReviewTitleLengthRule(var maxLength: Int) : LeaseReturnReviewRule {
    override fun check(item: LeaseReturnReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnReviewCodePrefixRule(var prefix: String) : LeaseReturnReviewRule {
    override fun check(item: LeaseReturnReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnReviewTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LeaseReturnReviewRule {
    override fun check(item: LeaseReturnReview): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a lease return review. */
data class LeaseReturnReviewValidator(var rules: MutableList<LeaseReturnReviewRule>) {
    fun validate(item: LeaseReturnReview): MutableList<String> {
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

    fun requireValid(item: LeaseReturnReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnReviewValidator(): LeaseReturnReviewValidator {
    return LeaseReturnReviewValidator(
        mutableListOf<LeaseReturnReviewRule>(
            LeaseReturnReviewRequiredTitleRule("lease return review needs an entry"),
            LeaseReturnReviewRequiredCodeRule("lease return review needs an entry"),
            LeaseReturnReviewAllowedSegmentRule(mutableListOf<String>("default", "global")),
            LeaseReturnReviewTitleLengthRule(16),
            LeaseReturnReviewCodePrefixRule("REF"),
            LeaseReturnReviewTagsLimitRule(1000, mutableListOf<String>("lenient", "fallback", "global")),
        ),
    )
}
