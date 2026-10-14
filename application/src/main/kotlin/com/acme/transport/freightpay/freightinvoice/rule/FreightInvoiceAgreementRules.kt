package com.acme.transport.freightpay.freightinvoice.rule

/** A rule a freight invoice agreement must pass. Check returns its problems, or none. */
interface FreightInvoiceAgreementRule {
    fun check(item: FreightInvoiceAgreement): MutableList<String>
}

data class FreightInvoiceAgreementCategoryLengthRule(var maxLength: Int) : FreightInvoiceAgreementRule {
    override fun check(item: FreightInvoiceAgreement): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAgreementRequiredCodeRule(var message: String) : FreightInvoiceAgreementRule {
    override fun check(item: FreightInvoiceAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAgreementRequiredCategoryRule(var message: String) : FreightInvoiceAgreementRule {
    override fun check(item: FreightInvoiceAgreement): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAgreementRequiredCarrierRule(var message: String) : FreightInvoiceAgreementRule {
    override fun check(item: FreightInvoiceAgreement): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice agreement. */
data class FreightInvoiceAgreementValidator(var rules: MutableList<FreightInvoiceAgreementRule>) {
    fun validate(item: FreightInvoiceAgreement): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceAgreementValidator(): FreightInvoiceAgreementValidator {
    return FreightInvoiceAgreementValidator(
        mutableListOf<FreightInvoiceAgreementRule>(
            FreightInvoiceAgreementCategoryLengthRule(255),
            FreightInvoiceAgreementRequiredCodeRule("freight invoice agreement needs an entry"),
            FreightInvoiceAgreementRequiredCategoryRule("freight invoice agreement needs this field"),
            FreightInvoiceAgreementRequiredCarrierRule("freight invoice agreement needs an entry"),
        ),
    )
}
