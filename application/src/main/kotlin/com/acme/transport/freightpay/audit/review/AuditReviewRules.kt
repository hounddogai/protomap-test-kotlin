package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.text.normalize

/** A rule a audit review must pass. Check returns its problems, or none. */
interface AuditReviewRule {
    fun check(item: AuditReview): MutableList<String>
}

data class AuditReviewAllowedRegionRule(var allowed: MutableList<String>) : AuditReviewRule {
    override fun check(item: AuditReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AuditReviewArchivedGuardRule(var limit: Int) : AuditReviewRule {
    override fun check(item: AuditReview): MutableList<String> {
        if (item.archived && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class AuditReviewRequiredRegionRule(var message: String) : AuditReviewRule {
    override fun check(item: AuditReview): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditReviewAttemptsRangeRule(var min: Int, var max: Int) : AuditReviewRule {
    override fun check(item: AuditReview): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class AuditReviewRegionLengthRule(var maxLength: Int) : AuditReviewRule {
    override fun check(item: AuditReview): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditReviewCodePrefixRule(var prefix: String) : AuditReviewRule {
    override fun check(item: AuditReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit review. */
data class AuditReviewValidator(var rules: MutableList<AuditReviewRule>) {
    fun validate(item: AuditReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditReview>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditReviewValidator(): AuditReviewValidator {
    return AuditReviewValidator(
        mutableListOf<AuditReviewRule>(
            AuditReviewAllowedRegionRule(mutableListOf<String>("strict", "default")),
            AuditReviewArchivedGuardRule(120),
            AuditReviewRequiredRegionRule("audit review needs an entry"),
            AuditReviewAttemptsRangeRule(255, 1000),
            AuditReviewRegionLengthRule(16),
            AuditReviewCodePrefixRule("ACM"),
        ),
    )
}
