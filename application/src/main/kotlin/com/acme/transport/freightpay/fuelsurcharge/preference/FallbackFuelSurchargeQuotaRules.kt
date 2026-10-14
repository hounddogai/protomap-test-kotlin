package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.normalize

/** A rule a fallback fuel surcharge quota must pass. Check returns its problems, or none. */
interface FallbackFuelSurchargeQuotaRule {
    fun check(item: FallbackFuelSurchargeQuota): MutableList<String>
}

data class FallbackFuelSurchargeQuotaAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : FallbackFuelSurchargeQuotaRule {
    override fun check(item: FallbackFuelSurchargeQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeQuotaPinnedGuardRule(var limit: Double) : FallbackFuelSurchargeQuotaRule {
    override fun check(item: FallbackFuelSurchargeQuota): MutableList<String> {
        if (item.pinned && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeQuotaRequiredTrailerIdRule(
    var message: String,
) : FallbackFuelSurchargeQuotaRule {
    override fun check(item: FallbackFuelSurchargeQuota): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeQuotaCodeLengthRule(var maxLength: Int) : FallbackFuelSurchargeQuotaRule {
    override fun check(item: FallbackFuelSurchargeQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeQuotaRequiredNameRule(var message: String) : FallbackFuelSurchargeQuotaRule {
    override fun check(item: FallbackFuelSurchargeQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeQuotaCodePrefixRule(var prefix: String) : FallbackFuelSurchargeQuotaRule {
    override fun check(item: FallbackFuelSurchargeQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback fuel surcharge quota. */
data class FallbackFuelSurchargeQuotaValidator(var rules: MutableList<FallbackFuelSurchargeQuotaRule>) {
    fun validate(item: FallbackFuelSurchargeQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackFuelSurchargeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFuelSurchargeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFuelSurchargeQuotaValidator(): FallbackFuelSurchargeQuotaValidator {
    return FallbackFuelSurchargeQuotaValidator(
        mutableListOf<FallbackFuelSurchargeQuotaRule>(
            FallbackFuelSurchargeQuotaAllowedCurrencyRule(mutableListOf<String>("batched")),
            FallbackFuelSurchargeQuotaPinnedGuardRule(0.4),
            FallbackFuelSurchargeQuotaRequiredTrailerIdRule("fallback fuel surcharge quota needs an entry"),
            FallbackFuelSurchargeQuotaCodeLengthRule(86400),
            FallbackFuelSurchargeQuotaRequiredNameRule("fallback fuel surcharge quota needs an entry"),
            FallbackFuelSurchargeQuotaCodePrefixRule("OPS"),
        ),
    )
}
