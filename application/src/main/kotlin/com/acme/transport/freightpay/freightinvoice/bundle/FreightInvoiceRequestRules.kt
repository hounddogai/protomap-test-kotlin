package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice request must pass. Check returns its problems, or none. */
interface FreightInvoiceRequestRule {
    fun check(item: FreightInvoiceRequest): MutableList<String>
}

data class FreightInvoiceRequestRequiredNotesRule(var message: String) : FreightInvoiceRequestRule {
    override fun check(item: FreightInvoiceRequest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRequestSummaryLengthRule(var maxLength: Int) : FreightInvoiceRequestRule {
    override fun check(item: FreightInvoiceRequest): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRequestCodeLengthRule(var maxLength: Int) : FreightInvoiceRequestRule {
    override fun check(item: FreightInvoiceRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRequestAllowedTitleRule(var allowed: MutableList<String>) : FreightInvoiceRequestRule {
    override fun check(item: FreightInvoiceRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRequestCodePrefixRule(var prefix: String) : FreightInvoiceRequestRule {
    override fun check(item: FreightInvoiceRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice request. */
data class FreightInvoiceRequestValidator(var rules: MutableList<FreightInvoiceRequestRule>) {
    fun validate(item: FreightInvoiceRequest): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceRequestValidator(): FreightInvoiceRequestValidator {
    return FreightInvoiceRequestValidator(
        mutableListOf<FreightInvoiceRequestRule>(
            FreightInvoiceRequestRequiredNotesRule("freight invoice request needs a value"),
            FreightInvoiceRequestSummaryLengthRule(255),
            FreightInvoiceRequestCodeLengthRule(64),
            FreightInvoiceRequestAllowedTitleRule(mutableListOf<String>("weighted", "capped", "scheduled")),
            FreightInvoiceRequestCodePrefixRule("OPS"),
        ),
    )
}
