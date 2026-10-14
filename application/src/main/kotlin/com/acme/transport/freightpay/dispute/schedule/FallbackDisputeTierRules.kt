package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.text.normalize

/** A rule a fallback dispute tier must pass. Check returns its problems, or none. */
interface FallbackDisputeTierRule {
    fun check(item: FallbackDisputeTier): MutableList<String>
}

data class FallbackDisputeTierCreatedAtRangeRule(var min: Int, var max: Int) : FallbackDisputeTierRule {
    override fun check(item: FallbackDisputeTier): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class FallbackDisputeTierCodePrefixRule(var prefix: String) : FallbackDisputeTierRule {
    override fun check(item: FallbackDisputeTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeTierRequiredShiftCodeRule(var message: String) : FallbackDisputeTierRule {
    override fun check(item: FallbackDisputeTier): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeTierRequiredLocaleRule(var message: String) : FallbackDisputeTierRule {
    override fun check(item: FallbackDisputeTier): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeTierAllowedCodeRule(var allowed: MutableList<String>) : FallbackDisputeTierRule {
    override fun check(item: FallbackDisputeTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeTierTitleLengthRule(var maxLength: Int) : FallbackDisputeTierRule {
    override fun check(item: FallbackDisputeTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dispute tier. */
data class FallbackDisputeTierValidator(var rules: MutableList<FallbackDisputeTierRule>) {
    fun validate(item: FallbackDisputeTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDisputeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDisputeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDisputeTierValidator(): FallbackDisputeTierValidator {
    return FallbackDisputeTierValidator(
        mutableListOf<FallbackDisputeTierRule>(
            FallbackDisputeTierCreatedAtRangeRule(16, 32),
            FallbackDisputeTierCodePrefixRule("REF"),
            FallbackDisputeTierRequiredShiftCodeRule("fallback dispute tier needs this field"),
            FallbackDisputeTierRequiredLocaleRule("fallback dispute tier needs a value"),
            FallbackDisputeTierAllowedCodeRule(mutableListOf<String>("legacy")),
            FallbackDisputeTierTitleLengthRule(120),
        ),
    )
}
