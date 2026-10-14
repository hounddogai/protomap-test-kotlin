package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice quota must pass. Check returns its problems, or none. */
interface FreightInvoiceQuotaRule {
    fun check(item: FreightInvoiceQuota): MutableList<String>
}

data class FreightInvoiceQuotaCreatedAtRangeRule(var min: Int, var max: Int) : FreightInvoiceQuotaRule {
    override fun check(item: FreightInvoiceQuota): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceQuotaEndsAtRangeRule(var min: Int, var max: Int) : FreightInvoiceQuotaRule {
    override fun check(item: FreightInvoiceQuota): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceQuotaAllowedCodeRule(var allowed: MutableList<String>) : FreightInvoiceQuotaRule {
    override fun check(item: FreightInvoiceQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceQuotaRequiredCodeRule(var message: String) : FreightInvoiceQuotaRule {
    override fun check(item: FreightInvoiceQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceQuotaCodePrefixRule(var prefix: String) : FreightInvoiceQuotaRule {
    override fun check(item: FreightInvoiceQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceQuotaMinimumRangeRule(var min: Double, var max: Double) : FreightInvoiceQuotaRule {
    override fun check(item: FreightInvoiceQuota): MutableList<String> {
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

/** Runs every rule of a freight invoice quota. */
data class FreightInvoiceQuotaValidator(var rules: MutableList<FreightInvoiceQuotaRule>) {
    fun validate(item: FreightInvoiceQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceQuotaValidator(): FreightInvoiceQuotaValidator {
    return FreightInvoiceQuotaValidator(
        mutableListOf<FreightInvoiceQuotaRule>(
            FreightInvoiceQuotaCreatedAtRangeRule(64, 1000),
            FreightInvoiceQuotaEndsAtRangeRule(255, 1000),
            FreightInvoiceQuotaAllowedCodeRule(mutableListOf<String>()),
            FreightInvoiceQuotaRequiredCodeRule("freight invoice quota needs this field"),
            FreightInvoiceQuotaCodePrefixRule("REF"),
            FreightInvoiceQuotaMinimumRangeRule(0.3, 4106.3),
        ),
    )
}
