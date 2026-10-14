package com.acme.transport.safety.violation.primaryviolationquota

/** A rule a lenient violation quota must pass. Check returns its problems, or none. */
interface LenientViolationQuotaRule {
    fun check(item: LenientViolationQuota): MutableList<String>
}

data class LenientViolationQuotaRetriesRangeRule(var min: Int, var max: Int) : LenientViolationQuotaRule {
    override fun check(item: LenientViolationQuota): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class LenientViolationQuotaTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LenientViolationQuotaRule {
    override fun check(item: LenientViolationQuota): MutableList<String> {
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

data class LenientViolationQuotaAttemptsRangeRule(var min: Int, var max: Int) : LenientViolationQuotaRule {
    override fun check(item: LenientViolationQuota): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationQuotaRequiredCodeRule(var message: String) : LenientViolationQuotaRule {
    override fun check(item: LenientViolationQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationQuotaRequiredTitleRule(var message: String) : LenientViolationQuotaRule {
    override fun check(item: LenientViolationQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient violation quota. */
data class LenientViolationQuotaValidator(var rules: MutableList<LenientViolationQuotaRule>) {
    fun validate(item: LenientViolationQuota): MutableList<String> {
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

    fun requireValid(item: LenientViolationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientViolationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientViolationQuotaValidator(): LenientViolationQuotaValidator {
    return LenientViolationQuotaValidator(
        mutableListOf<LenientViolationQuotaRule>(
            LenientViolationQuotaRetriesRangeRule(16, 255),
            LenientViolationQuotaTagsLimitRule(32, mutableListOf<String>()),
            LenientViolationQuotaAttemptsRangeRule(32, 64),
            LenientViolationQuotaRequiredCodeRule("lenient violation quota needs this field"),
            LenientViolationQuotaRequiredTitleRule("lenient violation quota needs an entry"),
        ),
    )
}
