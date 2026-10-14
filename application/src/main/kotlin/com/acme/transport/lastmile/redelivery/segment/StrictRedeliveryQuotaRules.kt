package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.text.normalize

/** A rule a strict redelivery quota must pass. Check returns its problems, or none. */
interface StrictRedeliveryQuotaRule {
    fun check(item: StrictRedeliveryQuota): MutableList<String>
}

data class StrictRedeliveryQuotaTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : StrictRedeliveryQuotaRule {
    override fun check(item: StrictRedeliveryQuota): MutableList<String> {
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

data class StrictRedeliveryQuotaAllowedTitleRule(var allowed: MutableList<String>) : StrictRedeliveryQuotaRule {
    override fun check(item: StrictRedeliveryQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryQuotaActiveGuardRule(var limit: Double) : StrictRedeliveryQuotaRule {
    override fun check(item: StrictRedeliveryQuota): MutableList<String> {
        if (item.active && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryQuotaRequiredTitleRule(var message: String) : StrictRedeliveryQuotaRule {
    override fun check(item: StrictRedeliveryQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryQuotaTitleLengthRule(var maxLength: Int) : StrictRedeliveryQuotaRule {
    override fun check(item: StrictRedeliveryQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict redelivery quota. */
data class StrictRedeliveryQuotaValidator(var rules: MutableList<StrictRedeliveryQuotaRule>) {
    fun validate(item: StrictRedeliveryQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRedeliveryQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRedeliveryQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRedeliveryQuotaValidator(): StrictRedeliveryQuotaValidator {
    return StrictRedeliveryQuotaValidator(
        mutableListOf<StrictRedeliveryQuotaRule>(
            StrictRedeliveryQuotaTagsLimitRule(32, mutableListOf<String>()),
            StrictRedeliveryQuotaAllowedTitleRule(mutableListOf<String>("lenient", "manual", "tiered")),
            StrictRedeliveryQuotaActiveGuardRule(9.7),
            StrictRedeliveryQuotaRequiredTitleRule("strict redelivery quota needs an entry"),
            StrictRedeliveryQuotaTitleLengthRule(64),
        ),
    )
}
