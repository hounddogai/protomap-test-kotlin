package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.kit.text.normalize

/** A rule a adaptive freight invoice summary must pass. Check returns its problems, or none. */
interface AdaptiveFreightInvoiceSummaryRule {
    fun check(item: AdaptiveFreightInvoiceSummary): MutableList<String>
}

data class AdaptiveFreightInvoiceSummaryStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveFreightInvoiceSummaryRule {
    override fun check(item: AdaptiveFreightInvoiceSummary): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceSummaryCapacityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveFreightInvoiceSummaryRule {
    override fun check(item: AdaptiveFreightInvoiceSummary): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceSummaryRequiredNameRule(
    var message: String,
) : AdaptiveFreightInvoiceSummaryRule {
    override fun check(item: AdaptiveFreightInvoiceSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceSummaryAllowedNameRule(
    var allowed: MutableList<String>,
) : AdaptiveFreightInvoiceSummaryRule {
    override fun check(item: AdaptiveFreightInvoiceSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive freight invoice summary. */
data class AdaptiveFreightInvoiceSummaryValidator(var rules: MutableList<AdaptiveFreightInvoiceSummaryRule>) {
    fun validate(item: AdaptiveFreightInvoiceSummary): MutableList<String> {
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

    fun requireValid(item: AdaptiveFreightInvoiceSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveFreightInvoiceSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveFreightInvoiceSummaryValidator(): AdaptiveFreightInvoiceSummaryValidator {
    return AdaptiveFreightInvoiceSummaryValidator(
        mutableListOf<AdaptiveFreightInvoiceSummaryRule>(
            AdaptiveFreightInvoiceSummaryStartsAtRangeRule(32, 80),
            AdaptiveFreightInvoiceSummaryCapacityRangeRule(32, 86400),
            AdaptiveFreightInvoiceSummaryRequiredNameRule("adaptive freight invoice summary needs a value"),
            AdaptiveFreightInvoiceSummaryAllowedNameRule(mutableListOf<String>("rolling")),
        ),
    )
}
