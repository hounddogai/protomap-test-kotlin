package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.text.normalize

/** A rule a fallback rate card variance must pass. Check returns its problems, or none. */
interface FallbackRateCardVarianceRule {
    fun check(item: FallbackRateCardVariance): MutableList<String>
}

data class FallbackRateCardVarianceCodePrefixRule(var prefix: String) : FallbackRateCardVarianceRule {
    override fun check(item: FallbackRateCardVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardVarianceAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackRateCardVarianceRule {
    override fun check(item: FallbackRateCardVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardVarianceStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRateCardVarianceRule {
    override fun check(item: FallbackRateCardVariance): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardVarianceRetriesRangeRule(var min: Int, var max: Int) : FallbackRateCardVarianceRule {
    override fun check(item: FallbackRateCardVariance): MutableList<String> {
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

/** Runs every rule of a fallback rate card variance. */
data class FallbackRateCardVarianceValidator(var rules: MutableList<FallbackRateCardVarianceRule>) {
    fun validate(item: FallbackRateCardVariance): MutableList<String> {
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

    fun requireValid(item: FallbackRateCardVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRateCardVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRateCardVarianceValidator(): FallbackRateCardVarianceValidator {
    return FallbackRateCardVarianceValidator(
        mutableListOf<FallbackRateCardVarianceRule>(
            FallbackRateCardVarianceCodePrefixRule("Q"),
            FallbackRateCardVarianceAllowedCodeRule(mutableListOf<String>("strict", "batched", "capped")),
            FallbackRateCardVarianceStartsAtRangeRule(64, 3600),
            FallbackRateCardVarianceRetriesRangeRule(3600, 3600),
        ),
    )
}
