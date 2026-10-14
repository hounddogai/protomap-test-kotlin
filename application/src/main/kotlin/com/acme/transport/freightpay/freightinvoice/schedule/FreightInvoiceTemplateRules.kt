package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice template must pass. Check returns its problems, or none. */
interface FreightInvoiceTemplateRule {
    fun check(item: FreightInvoiceTemplate): MutableList<String>
}

data class FreightInvoiceTemplateReviewerLengthRule(var maxLength: Int) : FreightInvoiceTemplateRule {
    override fun check(item: FreightInvoiceTemplate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceTemplateRequiredTitleRule(var message: String) : FreightInvoiceTemplateRule {
    override fun check(item: FreightInvoiceTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceTemplateRequiredNameRule(var message: String) : FreightInvoiceTemplateRule {
    override fun check(item: FreightInvoiceTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceTemplateAllowedTitleRule(
    var allowed: MutableList<String>,
) : FreightInvoiceTemplateRule {
    override fun check(item: FreightInvoiceTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceTemplateCodePrefixRule(var prefix: String) : FreightInvoiceTemplateRule {
    override fun check(item: FreightInvoiceTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice template. */
data class FreightInvoiceTemplateValidator(var rules: MutableList<FreightInvoiceTemplateRule>) {
    fun validate(item: FreightInvoiceTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceTemplateValidator(): FreightInvoiceTemplateValidator {
    return FreightInvoiceTemplateValidator(
        mutableListOf<FreightInvoiceTemplateRule>(
            FreightInvoiceTemplateReviewerLengthRule(86400),
            FreightInvoiceTemplateRequiredTitleRule("freight invoice template needs an entry"),
            FreightInvoiceTemplateRequiredNameRule("freight invoice template needs an entry"),
            FreightInvoiceTemplateAllowedTitleRule(mutableListOf<String>()),
            FreightInvoiceTemplateCodePrefixRule("Q"),
        ),
    )
}
