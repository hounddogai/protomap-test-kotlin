package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice metric must pass. Check returns its problems, or none. */
interface FreightInvoiceMetricRule {
    fun check(item: FreightInvoiceMetric): MutableList<String>
}

data class FreightInvoiceMetricCodeLengthRule(var maxLength: Int) : FreightInvoiceMetricRule {
    override fun check(item: FreightInvoiceMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceMetricAllowedCodeRule(var allowed: MutableList<String>) : FreightInvoiceMetricRule {
    override fun check(item: FreightInvoiceMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceMetricMinimumRangeRule(var min: Double, var max: Double) : FreightInvoiceMetricRule {
    override fun check(item: FreightInvoiceMetric): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceMetricRequiredCodeRule(var message: String) : FreightInvoiceMetricRule {
    override fun check(item: FreightInvoiceMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceMetricPalletsRangeRule(var min: Int, var max: Int) : FreightInvoiceMetricRule {
    override fun check(item: FreightInvoiceMetric): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceMetricRateRangeRule(var min: Double, var max: Double) : FreightInvoiceMetricRule {
    override fun check(item: FreightInvoiceMetric): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a freight invoice metric. */
data class FreightInvoiceMetricValidator(var rules: MutableList<FreightInvoiceMetricRule>) {
    fun validate(item: FreightInvoiceMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceMetricValidator(): FreightInvoiceMetricValidator {
    return FreightInvoiceMetricValidator(
        mutableListOf<FreightInvoiceMetricRule>(
            FreightInvoiceMetricCodeLengthRule(16),
            FreightInvoiceMetricAllowedCodeRule(mutableListOf<String>("seasonal", "adaptive")),
            FreightInvoiceMetricMinimumRangeRule(6.1, 1203.9),
            FreightInvoiceMetricRequiredCodeRule("freight invoice metric needs this field"),
            FreightInvoiceMetricPalletsRangeRule(120, 3600),
            FreightInvoiceMetricRateRangeRule(9.8, 4359.9),
        ),
    )
}
