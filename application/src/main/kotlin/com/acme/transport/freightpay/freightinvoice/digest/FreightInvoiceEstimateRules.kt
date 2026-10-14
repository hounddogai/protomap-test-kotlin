package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice estimate must pass. Check returns its problems, or none. */
interface FreightInvoiceEstimateRule {
    fun check(item: FreightInvoiceEstimate): MutableList<String>
}

data class FreightInvoiceEstimateCodeLengthRule(var maxLength: Int) : FreightInvoiceEstimateRule {
    override fun check(item: FreightInvoiceEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceEstimateRequiredNameRule(var message: String) : FreightInvoiceEstimateRule {
    override fun check(item: FreightInvoiceEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceEstimateRequiredExternalRefRule(var message: String) : FreightInvoiceEstimateRule {
    override fun check(item: FreightInvoiceEstimate): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceEstimateAllowedCodeRule(
    var allowed: MutableList<String>,
) : FreightInvoiceEstimateRule {
    override fun check(item: FreightInvoiceEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice estimate. */
data class FreightInvoiceEstimateValidator(var rules: MutableList<FreightInvoiceEstimateRule>) {
    fun validate(item: FreightInvoiceEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceEstimateValidator(): FreightInvoiceEstimateValidator {
    return FreightInvoiceEstimateValidator(
        mutableListOf<FreightInvoiceEstimateRule>(
            FreightInvoiceEstimateCodeLengthRule(3600),
            FreightInvoiceEstimateRequiredNameRule("freight invoice estimate needs a value"),
            FreightInvoiceEstimateRequiredExternalRefRule("freight invoice estimate needs an entry"),
            FreightInvoiceEstimateAllowedCodeRule(mutableListOf<String>("lenient")),
        ),
    )
}
