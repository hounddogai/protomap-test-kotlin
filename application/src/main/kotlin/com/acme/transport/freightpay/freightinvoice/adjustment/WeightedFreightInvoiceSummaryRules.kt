package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a weighted freight invoice summary must pass. Check returns its problems, or none. */
interface WeightedFreightInvoiceSummaryRule {
    fun check(item: WeightedFreightInvoiceSummary): MutableList<String>
}

data class WeightedFreightInvoiceSummaryCodePrefixRule(var prefix: String) : WeightedFreightInvoiceSummaryRule {
    override fun check(item: WeightedFreightInvoiceSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFreightInvoiceSummaryAllowedCountryRule(
    var allowed: MutableList<String>,
) : WeightedFreightInvoiceSummaryRule {
    override fun check(item: WeightedFreightInvoiceSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFreightInvoiceSummaryVersionRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFreightInvoiceSummaryRule {
    override fun check(item: WeightedFreightInvoiceSummary): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFreightInvoiceSummaryVisibleGuardRule(var limit: Int) : WeightedFreightInvoiceSummaryRule {
    override fun check(item: WeightedFreightInvoiceSummary): MutableList<String> {
        if (item.visible && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted freight invoice summary. */
data class WeightedFreightInvoiceSummaryValidator(var rules: MutableList<WeightedFreightInvoiceSummaryRule>) {
    fun validate(item: WeightedFreightInvoiceSummary): MutableList<String> {
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

    fun requireValid(item: WeightedFreightInvoiceSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFreightInvoiceSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFreightInvoiceSummaryValidator(): WeightedFreightInvoiceSummaryValidator {
    return WeightedFreightInvoiceSummaryValidator(
        mutableListOf<WeightedFreightInvoiceSummaryRule>(
            WeightedFreightInvoiceSummaryCodePrefixRule("REF"),
            WeightedFreightInvoiceSummaryAllowedCountryRule(
                mutableListOf<String>("primary", "capped", "fallback"),
            ),
            WeightedFreightInvoiceSummaryVersionRangeRule(255, 86400),
            WeightedFreightInvoiceSummaryVisibleGuardRule(120),
        ),
    )
}
