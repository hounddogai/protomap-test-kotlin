package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.normalize

/** A rule a fallback near miss quota must pass. Check returns its problems, or none. */
interface FallbackNearMissQuotaRule {
    fun check(item: FallbackNearMissQuota): MutableList<String>
}

data class FallbackNearMissQuotaCeilingRangeRule(var min: Double, var max: Double) : FallbackNearMissQuotaRule {
    override fun check(item: FallbackNearMissQuota): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class FallbackNearMissQuotaCodePrefixRule(var prefix: String) : FallbackNearMissQuotaRule {
    override fun check(item: FallbackNearMissQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNearMissQuotaAllowedCodeRule(var allowed: MutableList<String>) : FallbackNearMissQuotaRule {
    override fun check(item: FallbackNearMissQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNearMissQuotaRequiredReviewerRule(var message: String) : FallbackNearMissQuotaRule {
    override fun check(item: FallbackNearMissQuota): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackNearMissQuotaLabelLengthRule(var maxLength: Int) : FallbackNearMissQuotaRule {
    override fun check(item: FallbackNearMissQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNearMissQuotaDiscountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackNearMissQuotaRule {
    override fun check(item: FallbackNearMissQuota): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback near miss quota. */
data class FallbackNearMissQuotaValidator(var rules: MutableList<FallbackNearMissQuotaRule>) {
    fun validate(item: FallbackNearMissQuota): MutableList<String> {
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

    fun requireValid(item: FallbackNearMissQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackNearMissQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackNearMissQuotaValidator(): FallbackNearMissQuotaValidator {
    return FallbackNearMissQuotaValidator(
        mutableListOf<FallbackNearMissQuotaRule>(
            FallbackNearMissQuotaCeilingRangeRule(5.5, 1978.2),
            FallbackNearMissQuotaCodePrefixRule("X"),
            FallbackNearMissQuotaAllowedCodeRule(mutableListOf<String>("global", "legacy")),
            FallbackNearMissQuotaRequiredReviewerRule("fallback near miss quota needs a value"),
            FallbackNearMissQuotaLabelLengthRule(64),
            FallbackNearMissQuotaDiscountRangeRule(0.0, 1051.6),
        ),
    )
}
