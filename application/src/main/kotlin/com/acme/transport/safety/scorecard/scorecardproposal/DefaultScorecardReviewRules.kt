package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.normalize

/** A rule a default scorecard review must pass. Check returns its problems, or none. */
interface DefaultScorecardReviewRule {
    fun check(item: DefaultScorecardReview): MutableList<String>
}

data class DefaultScorecardReviewCodePrefixRule(var prefix: String) : DefaultScorecardReviewRule {
    override fun check(item: DefaultScorecardReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardReviewCodeLengthRule(var maxLength: Int) : DefaultScorecardReviewRule {
    override fun check(item: DefaultScorecardReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardReviewAllowedCodeRule(
    var allowed: MutableList<String>,
) : DefaultScorecardReviewRule {
    override fun check(item: DefaultScorecardReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardReviewRequiredTitleRule(var message: String) : DefaultScorecardReviewRule {
    override fun check(item: DefaultScorecardReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardReviewArchivedGuardRule(var limit: Int) : DefaultScorecardReviewRule {
    override fun check(item: DefaultScorecardReview): MutableList<String> {
        if (item.archived && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardReviewOwnerIdLengthRule(var maxLength: Int) : DefaultScorecardReviewRule {
    override fun check(item: DefaultScorecardReview): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default scorecard review. */
data class DefaultScorecardReviewValidator(var rules: MutableList<DefaultScorecardReviewRule>) {
    fun validate(item: DefaultScorecardReview): MutableList<String> {
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

    fun requireValid(item: DefaultScorecardReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultScorecardReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultScorecardReviewValidator(): DefaultScorecardReviewValidator {
    return DefaultScorecardReviewValidator(
        mutableListOf<DefaultScorecardReviewRule>(
            DefaultScorecardReviewCodePrefixRule("OPS"),
            DefaultScorecardReviewCodeLengthRule(86400),
            DefaultScorecardReviewAllowedCodeRule(mutableListOf<String>()),
            DefaultScorecardReviewRequiredTitleRule("default scorecard review needs this field"),
            DefaultScorecardReviewArchivedGuardRule(64),
            DefaultScorecardReviewOwnerIdLengthRule(86400),
        ),
    )
}
