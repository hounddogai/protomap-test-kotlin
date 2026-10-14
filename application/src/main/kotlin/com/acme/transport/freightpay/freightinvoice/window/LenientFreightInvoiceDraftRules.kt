package com.acme.transport.freightpay.freightinvoice.window

/** A rule a lenient freight invoice draft must pass. Check returns its problems, or none. */
interface LenientFreightInvoiceDraftRule {
    fun check(item: LenientFreightInvoiceDraft): MutableList<String>
}

data class LenientFreightInvoiceDraftCodePrefixRule(var prefix: String) : LenientFreightInvoiceDraftRule {
    override fun check(item: LenientFreightInvoiceDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceDraftRequiredSummaryRule(var message: String) : LenientFreightInvoiceDraftRule {
    override fun check(item: LenientFreightInvoiceDraft): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceDraftRequiredLabelRule(var message: String) : LenientFreightInvoiceDraftRule {
    override fun check(item: LenientFreightInvoiceDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient freight invoice draft. */
data class LenientFreightInvoiceDraftValidator(var rules: MutableList<LenientFreightInvoiceDraftRule>) {
    fun validate(item: LenientFreightInvoiceDraft): MutableList<String> {
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

    fun requireValid(item: LenientFreightInvoiceDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFreightInvoiceDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFreightInvoiceDraftValidator(): LenientFreightInvoiceDraftValidator {
    return LenientFreightInvoiceDraftValidator(
        mutableListOf<LenientFreightInvoiceDraftRule>(
            LenientFreightInvoiceDraftCodePrefixRule("X"),
            LenientFreightInvoiceDraftRequiredSummaryRule("lenient freight invoice draft needs this field"),
            LenientFreightInvoiceDraftRequiredLabelRule("lenient freight invoice draft needs this field"),
        ),
    )
}
