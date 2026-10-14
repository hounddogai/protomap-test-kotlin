package com.acme.transport.freightpay.audit.rule

import com.acme.transport.kit.text.normalize

/** A rule a fallback audit review must pass. Check returns its problems, or none. */
interface FallbackAuditReviewRule {
    fun check(item: FallbackAuditReview): MutableList<String>
}

data class FallbackAuditReviewDescriptionLengthRule(var maxLength: Int) : FallbackAuditReviewRule {
    override fun check(item: FallbackAuditReview): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditReviewRequiredTitleRule(var message: String) : FallbackAuditReviewRule {
    override fun check(item: FallbackAuditReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditReviewAllowedTitleRule(var allowed: MutableList<String>) : FallbackAuditReviewRule {
    override fun check(item: FallbackAuditReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditReviewTitleLengthRule(var maxLength: Int) : FallbackAuditReviewRule {
    override fun check(item: FallbackAuditReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditReviewCodeLengthRule(var maxLength: Int) : FallbackAuditReviewRule {
    override fun check(item: FallbackAuditReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback audit review. */
data class FallbackAuditReviewValidator(var rules: MutableList<FallbackAuditReviewRule>) {
    fun validate(item: FallbackAuditReview): MutableList<String> {
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

    fun requireValid(item: FallbackAuditReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAuditReview>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAuditReviewValidator(): FallbackAuditReviewValidator {
    return FallbackAuditReviewValidator(
        mutableListOf<FallbackAuditReviewRule>(
            FallbackAuditReviewDescriptionLengthRule(255),
            FallbackAuditReviewRequiredTitleRule("fallback audit review needs this field"),
            FallbackAuditReviewAllowedTitleRule(mutableListOf<String>("adaptive", "lenient")),
            FallbackAuditReviewTitleLengthRule(86400),
            FallbackAuditReviewCodeLengthRule(3600),
        ),
    )
}
