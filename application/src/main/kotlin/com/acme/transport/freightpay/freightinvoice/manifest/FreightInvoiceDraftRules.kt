package com.acme.transport.freightpay.freightinvoice.manifest

/** A rule a freight invoice draft must pass. Check returns its problems, or none. */
interface FreightInvoiceDraftRule {
    fun check(item: FreightInvoiceDraft): MutableList<String>
}

data class FreightInvoiceDraftRequiredCodeRule(var message: String) : FreightInvoiceDraftRule {
    override fun check(item: FreightInvoiceDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceDraftAttemptsRangeRule(var min: Int, var max: Int) : FreightInvoiceDraftRule {
    override fun check(item: FreightInvoiceDraft): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceDraftExternalRefLengthRule(var maxLength: Int) : FreightInvoiceDraftRule {
    override fun check(item: FreightInvoiceDraft): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice draft. */
data class FreightInvoiceDraftValidator(var rules: MutableList<FreightInvoiceDraftRule>) {
    fun validate(item: FreightInvoiceDraft): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceDraftValidator(): FreightInvoiceDraftValidator {
    return FreightInvoiceDraftValidator(
        mutableListOf<FreightInvoiceDraftRule>(
            FreightInvoiceDraftRequiredCodeRule("freight invoice draft needs an entry"),
            FreightInvoiceDraftAttemptsRangeRule(32, 1000),
            FreightInvoiceDraftExternalRefLengthRule(255),
        ),
    )
}
