package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice policy must pass. Check returns its problems, or none. */
interface FreightInvoicePolicyRule {
    fun check(item: FreightInvoicePolicy): MutableList<String>
}

data class FreightInvoicePolicyRequiredTimezoneRule(var message: String) : FreightInvoicePolicyRule {
    override fun check(item: FreightInvoicePolicy): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoicePolicyAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : FreightInvoicePolicyRule {
    override fun check(item: FreightInvoicePolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoicePolicyRequiredCodeRule(var message: String) : FreightInvoicePolicyRule {
    override fun check(item: FreightInvoicePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoicePolicyCodePrefixRule(var prefix: String) : FreightInvoicePolicyRule {
    override fun check(item: FreightInvoicePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoicePolicyLabelLengthRule(var maxLength: Int) : FreightInvoicePolicyRule {
    override fun check(item: FreightInvoicePolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice policy. */
data class FreightInvoicePolicyValidator(var rules: MutableList<FreightInvoicePolicyRule>) {
    fun validate(item: FreightInvoicePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoicePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoicePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoicePolicyValidator(): FreightInvoicePolicyValidator {
    return FreightInvoicePolicyValidator(
        mutableListOf<FreightInvoicePolicyRule>(
            FreightInvoicePolicyRequiredTimezoneRule("freight invoice policy needs a value"),
            FreightInvoicePolicyAllowedTimezoneRule(mutableListOf<String>()),
            FreightInvoicePolicyRequiredCodeRule("freight invoice policy needs a value"),
            FreightInvoicePolicyCodePrefixRule("REF"),
            FreightInvoicePolicyLabelLengthRule(1000),
        ),
    )
}
