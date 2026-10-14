package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.normalize

/** A rule a adaptive tire claim must pass. Check returns its problems, or none. */
interface AdaptiveTireClaimRule {
    fun check(item: AdaptiveTireClaim): MutableList<String>
}

data class AdaptiveTireClaimCurrencyLengthRule(var maxLength: Int) : AdaptiveTireClaimRule {
    override fun check(item: AdaptiveTireClaim): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireClaimRequiredTitleRule(var message: String) : AdaptiveTireClaimRule {
    override fun check(item: AdaptiveTireClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireClaimAllowedTitleRule(var allowed: MutableList<String>) : AdaptiveTireClaimRule {
    override fun check(item: AdaptiveTireClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireClaimTitleLengthRule(var maxLength: Int) : AdaptiveTireClaimRule {
    override fun check(item: AdaptiveTireClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireClaimDockLengthRule(var maxLength: Int) : AdaptiveTireClaimRule {
    override fun check(item: AdaptiveTireClaim): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireClaimCodePrefixRule(var prefix: String) : AdaptiveTireClaimRule {
    override fun check(item: AdaptiveTireClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tire claim. */
data class AdaptiveTireClaimValidator(var rules: MutableList<AdaptiveTireClaimRule>) {
    fun validate(item: AdaptiveTireClaim): MutableList<String> {
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

    fun requireValid(item: AdaptiveTireClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTireClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTireClaimValidator(): AdaptiveTireClaimValidator {
    return AdaptiveTireClaimValidator(
        mutableListOf<AdaptiveTireClaimRule>(
            AdaptiveTireClaimCurrencyLengthRule(16),
            AdaptiveTireClaimRequiredTitleRule("adaptive tire claim needs an entry"),
            AdaptiveTireClaimAllowedTitleRule(mutableListOf<String>("batched")),
            AdaptiveTireClaimTitleLengthRule(64),
            AdaptiveTireClaimDockLengthRule(255),
            AdaptiveTireClaimCodePrefixRule("X"),
        ),
    )
}
