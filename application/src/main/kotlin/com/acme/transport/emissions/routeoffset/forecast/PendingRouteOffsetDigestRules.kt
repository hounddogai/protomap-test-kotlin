package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize

/** A rule a pending route offset digest must pass. Check returns its problems, or none. */
interface PendingRouteOffsetDigestRule {
    fun check(item: PendingRouteOffsetDigest): MutableList<String>
}

data class PendingRouteOffsetDigestRequiredCodeRule(var message: String) : PendingRouteOffsetDigestRule {
    override fun check(item: PendingRouteOffsetDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetDigestCodeLengthRule(var maxLength: Int) : PendingRouteOffsetDigestRule {
    override fun check(item: PendingRouteOffsetDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetDigestAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingRouteOffsetDigestRule {
    override fun check(item: PendingRouteOffsetDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetDigestAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : PendingRouteOffsetDigestRule {
    override fun check(item: PendingRouteOffsetDigest): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetDigestCodePrefixRule(var prefix: String) : PendingRouteOffsetDigestRule {
    override fun check(item: PendingRouteOffsetDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetDigestCurrencyLengthRule(var maxLength: Int) : PendingRouteOffsetDigestRule {
    override fun check(item: PendingRouteOffsetDigest): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending route offset digest. */
data class PendingRouteOffsetDigestValidator(var rules: MutableList<PendingRouteOffsetDigestRule>) {
    fun validate(item: PendingRouteOffsetDigest): MutableList<String> {
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

    fun requireValid(item: PendingRouteOffsetDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRouteOffsetDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRouteOffsetDigestValidator(): PendingRouteOffsetDigestValidator {
    return PendingRouteOffsetDigestValidator(
        mutableListOf<PendingRouteOffsetDigestRule>(
            PendingRouteOffsetDigestRequiredCodeRule("pending route offset digest needs an entry"),
            PendingRouteOffsetDigestCodeLengthRule(86400),
            PendingRouteOffsetDigestAllowedCodeRule(mutableListOf<String>("global", "rolling")),
            PendingRouteOffsetDigestAttemptsRangeRule(16, 16),
            PendingRouteOffsetDigestCodePrefixRule("ACM"),
            PendingRouteOffsetDigestCurrencyLengthRule(120),
        ),
    )
}
