package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.normalize

/** A rule a capped tender claim must pass. Check returns its problems, or none. */
interface CappedTenderClaimRule {
    fun check(item: CappedTenderClaim): MutableList<String>
}

data class CappedTenderClaimCodePrefixRule(var prefix: String) : CappedTenderClaimRule {
    override fun check(item: CappedTenderClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderClaimAllowedCodeRule(var allowed: MutableList<String>) : CappedTenderClaimRule {
    override fun check(item: CappedTenderClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderClaimRequiredNameRule(var message: String) : CappedTenderClaimRule {
    override fun check(item: CappedTenderClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderClaimMarginRangeRule(var min: Double, var max: Double) : CappedTenderClaimRule {
    override fun check(item: CappedTenderClaim): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class CappedTenderClaimRequiredCodeRule(var message: String) : CappedTenderClaimRule {
    override fun check(item: CappedTenderClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderClaimEtaMinutesRangeRule(var min: Int, var max: Int) : CappedTenderClaimRule {
    override fun check(item: CappedTenderClaim): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tender claim. */
data class CappedTenderClaimValidator(var rules: MutableList<CappedTenderClaimRule>) {
    fun validate(item: CappedTenderClaim): MutableList<String> {
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

    fun requireValid(item: CappedTenderClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTenderClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTenderClaimValidator(): CappedTenderClaimValidator {
    return CappedTenderClaimValidator(
        mutableListOf<CappedTenderClaimRule>(
            CappedTenderClaimCodePrefixRule("Q"),
            CappedTenderClaimAllowedCodeRule(mutableListOf<String>("regional")),
            CappedTenderClaimRequiredNameRule("capped tender claim needs an entry"),
            CappedTenderClaimMarginRangeRule(4.6, 1066.5),
            CappedTenderClaimRequiredCodeRule("capped tender claim needs an entry"),
            CappedTenderClaimEtaMinutesRangeRule(32, 3600),
        ),
    )
}
