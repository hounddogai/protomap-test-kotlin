package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.text.normalize

/** A rule a transit time quota must pass. Check returns its problems, or none. */
interface TransitTimeQuotaRule {
    fun check(item: TransitTimeQuota): MutableList<String>
}

data class TransitTimeQuotaCodeLengthRule(var maxLength: Int) : TransitTimeQuotaRule {
    override fun check(item: TransitTimeQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeQuotaUpdatedAtRangeRule(var min: Int, var max: Int) : TransitTimeQuotaRule {
    override fun check(item: TransitTimeQuota): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class TransitTimeQuotaAllowedCodeRule(var allowed: MutableList<String>) : TransitTimeQuotaRule {
    override fun check(item: TransitTimeQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeQuotaRateRangeRule(var min: Double, var max: Double) : TransitTimeQuotaRule {
    override fun check(item: TransitTimeQuota): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeQuotaRequiredCodeRule(var message: String) : TransitTimeQuotaRule {
    override fun check(item: TransitTimeQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeQuotaAmountRangeRule(var min: Double, var max: Double) : TransitTimeQuotaRule {
    override fun check(item: TransitTimeQuota): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a transit time quota. */
data class TransitTimeQuotaValidator(var rules: MutableList<TransitTimeQuotaRule>) {
    fun validate(item: TransitTimeQuota): MutableList<String> {
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

    fun requireValid(item: TransitTimeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeQuotaValidator(): TransitTimeQuotaValidator {
    return TransitTimeQuotaValidator(
        mutableListOf<TransitTimeQuotaRule>(
            TransitTimeQuotaCodeLengthRule(3600),
            TransitTimeQuotaUpdatedAtRangeRule(16, 64),
            TransitTimeQuotaAllowedCodeRule(mutableListOf<String>("manual", "primary", "strict")),
            TransitTimeQuotaRateRangeRule(5.1, 4232.0),
            TransitTimeQuotaRequiredCodeRule("transit time quota needs a value"),
            TransitTimeQuotaAmountRangeRule(7.6, 2102.6),
        ),
    )
}
