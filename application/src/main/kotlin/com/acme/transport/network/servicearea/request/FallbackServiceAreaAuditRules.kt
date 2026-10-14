package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize

/** A rule a fallback service area audit must pass. Check returns its problems, or none. */
interface FallbackServiceAreaAuditRule {
    fun check(item: FallbackServiceAreaAudit): MutableList<String>
}

data class FallbackServiceAreaAuditAllowedNameRule(
    var allowed: MutableList<String>,
) : FallbackServiceAreaAuditRule {
    override fun check(item: FallbackServiceAreaAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaAuditRequiredCodeRule(var message: String) : FallbackServiceAreaAuditRule {
    override fun check(item: FallbackServiceAreaAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaAuditVersionRangeRule(var min: Int, var max: Int) : FallbackServiceAreaAuditRule {
    override fun check(item: FallbackServiceAreaAudit): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaAuditRequiredCurrencyRule(var message: String) : FallbackServiceAreaAuditRule {
    override fun check(item: FallbackServiceAreaAudit): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServiceAreaAuditCurrencyLengthRule(var maxLength: Int) : FallbackServiceAreaAuditRule {
    override fun check(item: FallbackServiceAreaAudit): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service area audit. */
data class FallbackServiceAreaAuditValidator(var rules: MutableList<FallbackServiceAreaAuditRule>) {
    fun validate(item: FallbackServiceAreaAudit): MutableList<String> {
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

    fun requireValid(item: FallbackServiceAreaAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServiceAreaAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServiceAreaAuditValidator(): FallbackServiceAreaAuditValidator {
    return FallbackServiceAreaAuditValidator(
        mutableListOf<FallbackServiceAreaAuditRule>(
            FallbackServiceAreaAuditAllowedNameRule(mutableListOf<String>("regional")),
            FallbackServiceAreaAuditRequiredCodeRule("fallback service area audit needs an entry"),
            FallbackServiceAreaAuditVersionRangeRule(16, 1000),
            FallbackServiceAreaAuditRequiredCurrencyRule("fallback service area audit needs an entry"),
            FallbackServiceAreaAuditCurrencyLengthRule(1000),
        ),
    )
}
