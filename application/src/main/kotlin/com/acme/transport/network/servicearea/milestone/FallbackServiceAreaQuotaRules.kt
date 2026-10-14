package com.acme.transport.network.servicearea.milestone

/** A rule a fallback service area quota must pass. Check returns its problems, or none. */
interface FallbackServiceAreaQuotaRule {
    fun check(item: FallbackServiceAreaQuota): MutableList<String>
}

data class FallbackServiceAreaQuotaVisibleGuardRule(var limit: Int) : FallbackServiceAreaQuotaRule {
    override fun check(item: FallbackServiceAreaQuota): MutableList<String> {
        if (item.visible && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaQuotaCurrencyLengthRule(var maxLength: Int) : FallbackServiceAreaQuotaRule {
    override fun check(item: FallbackServiceAreaQuota): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaQuotaRequiredTitleRule(var message: String) : FallbackServiceAreaQuotaRule {
    override fun check(item: FallbackServiceAreaQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service area quota. */
data class FallbackServiceAreaQuotaValidator(var rules: MutableList<FallbackServiceAreaQuotaRule>) {
    fun validate(item: FallbackServiceAreaQuota): MutableList<String> {
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

    fun requireValid(item: FallbackServiceAreaQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServiceAreaQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServiceAreaQuotaValidator(): FallbackServiceAreaQuotaValidator {
    return FallbackServiceAreaQuotaValidator(
        mutableListOf<FallbackServiceAreaQuotaRule>(
            FallbackServiceAreaQuotaVisibleGuardRule(80),
            FallbackServiceAreaQuotaCurrencyLengthRule(16),
            FallbackServiceAreaQuotaRequiredTitleRule("fallback service area quota needs a value"),
        ),
    )
}
