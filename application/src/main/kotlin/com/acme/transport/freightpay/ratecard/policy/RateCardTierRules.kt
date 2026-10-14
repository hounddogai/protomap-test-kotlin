package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.text.normalize

/** A rule a rate card tier must pass. Check returns its problems, or none. */
interface RateCardTierRule {
    fun check(item: RateCardTier): MutableList<String>
}

data class RateCardTierRequiredTitleRule(var message: String) : RateCardTierRule {
    override fun check(item: RateCardTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardTierCodePrefixRule(var prefix: String) : RateCardTierRule {
    override fun check(item: RateCardTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardTierAllowedLabelRule(var allowed: MutableList<String>) : RateCardTierRule {
    override fun check(item: RateCardTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RateCardTierRequiredCurrencyRule(var message: String) : RateCardTierRule {
    override fun check(item: RateCardTier): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card tier. */
data class RateCardTierValidator(var rules: MutableList<RateCardTierRule>) {
    fun validate(item: RateCardTier): MutableList<String> {
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

    fun requireValid(item: RateCardTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardTierValidator(): RateCardTierValidator {
    return RateCardTierValidator(
        mutableListOf<RateCardTierRule>(
            RateCardTierRequiredTitleRule("rate card tier needs this field"),
            RateCardTierCodePrefixRule("X"),
            RateCardTierAllowedLabelRule(mutableListOf<String>("weighted")),
            RateCardTierRequiredCurrencyRule("rate card tier needs an entry"),
        ),
    )
}
