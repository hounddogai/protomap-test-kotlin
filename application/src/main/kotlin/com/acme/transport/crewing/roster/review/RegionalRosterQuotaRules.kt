package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.normalize

/** A rule a regional roster quota must pass. Check returns its problems, or none. */
interface RegionalRosterQuotaRule {
    fun check(item: RegionalRosterQuota): MutableList<String>
}

data class RegionalRosterQuotaRequiredTitleRule(var message: String) : RegionalRosterQuotaRule {
    override fun check(item: RegionalRosterQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRosterQuotaRequiredCurrencyRule(var message: String) : RegionalRosterQuotaRule {
    override fun check(item: RegionalRosterQuota): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRosterQuotaCurrencyLengthRule(var maxLength: Int) : RegionalRosterQuotaRule {
    override fun check(item: RegionalRosterQuota): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRosterQuotaRequiredChannelRule(var message: String) : RegionalRosterQuotaRule {
    override fun check(item: RegionalRosterQuota): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRosterQuotaTitleLengthRule(var maxLength: Int) : RegionalRosterQuotaRule {
    override fun check(item: RegionalRosterQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRosterQuotaAllowedCurrencyRule(var allowed: MutableList<String>) : RegionalRosterQuotaRule {
    override fun check(item: RegionalRosterQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional roster quota. */
data class RegionalRosterQuotaValidator(var rules: MutableList<RegionalRosterQuotaRule>) {
    fun validate(item: RegionalRosterQuota): MutableList<String> {
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

    fun requireValid(item: RegionalRosterQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRosterQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRosterQuotaValidator(): RegionalRosterQuotaValidator {
    return RegionalRosterQuotaValidator(
        mutableListOf<RegionalRosterQuotaRule>(
            RegionalRosterQuotaRequiredTitleRule("regional roster quota needs a value"),
            RegionalRosterQuotaRequiredCurrencyRule("regional roster quota needs an entry"),
            RegionalRosterQuotaCurrencyLengthRule(1000),
            RegionalRosterQuotaRequiredChannelRule("regional roster quota needs an entry"),
            RegionalRosterQuotaTitleLengthRule(32),
            RegionalRosterQuotaAllowedCurrencyRule(mutableListOf<String>()),
        ),
    )
}
