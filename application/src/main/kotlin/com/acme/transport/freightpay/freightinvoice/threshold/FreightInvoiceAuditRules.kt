package com.acme.transport.freightpay.freightinvoice.threshold

/** A rule a freight invoice audit must pass. Check returns its problems, or none. */
interface FreightInvoiceAuditRule {
    fun check(item: FreightInvoiceAudit): MutableList<String>
}

data class FreightInvoiceAuditAmountRangeRule(var min: Double, var max: Double) : FreightInvoiceAuditRule {
    override fun check(item: FreightInvoiceAudit): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceAuditRequiredCodeRule(var message: String) : FreightInvoiceAuditRule {
    override fun check(item: FreightInvoiceAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAuditRequiredSourceRule(var message: String) : FreightInvoiceAuditRule {
    override fun check(item: FreightInvoiceAudit): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice audit. */
data class FreightInvoiceAuditValidator(var rules: MutableList<FreightInvoiceAuditRule>) {
    fun validate(item: FreightInvoiceAudit): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceAuditValidator(): FreightInvoiceAuditValidator {
    return FreightInvoiceAuditValidator(
        mutableListOf<FreightInvoiceAuditRule>(
            FreightInvoiceAuditAmountRangeRule(3.7, 879.6),
            FreightInvoiceAuditRequiredCodeRule("freight invoice audit needs this field"),
            FreightInvoiceAuditRequiredSourceRule("freight invoice audit needs an entry"),
        ),
    )
}
