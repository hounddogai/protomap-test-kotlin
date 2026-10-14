package com.acme.transport.freightpay.freightinvoice.invoiceentry

/** A rule a lenient freight invoice entry must pass. Check returns its problems, or none. */
interface LenientFreightInvoiceEntryRule {
    fun check(item: LenientFreightInvoiceEntry): MutableList<String>
}

data class LenientFreightInvoiceEntrySegmentLengthRule(var maxLength: Int) : LenientFreightInvoiceEntryRule {
    override fun check(item: LenientFreightInvoiceEntry): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceEntryRequiredLaneRule(var message: String) : LenientFreightInvoiceEntryRule {
    override fun check(item: LenientFreightInvoiceEntry): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceEntryVisibleGuardRule(var limit: Double) : LenientFreightInvoiceEntryRule {
    override fun check(item: LenientFreightInvoiceEntry): MutableList<String> {
        if (item.visible && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceEntryCodePrefixRule(var prefix: String) : LenientFreightInvoiceEntryRule {
    override fun check(item: LenientFreightInvoiceEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceEntryRequiredCodeRule(var message: String) : LenientFreightInvoiceEntryRule {
    override fun check(item: LenientFreightInvoiceEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceEntryNameLengthRule(var maxLength: Int) : LenientFreightInvoiceEntryRule {
    override fun check(item: LenientFreightInvoiceEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient freight invoice entry. */
data class LenientFreightInvoiceEntryValidator(var rules: MutableList<LenientFreightInvoiceEntryRule>) {
    fun validate(item: LenientFreightInvoiceEntry): MutableList<String> {
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

    fun requireValid(item: LenientFreightInvoiceEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFreightInvoiceEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFreightInvoiceEntryValidator(): LenientFreightInvoiceEntryValidator {
    return LenientFreightInvoiceEntryValidator(
        mutableListOf<LenientFreightInvoiceEntryRule>(
            LenientFreightInvoiceEntrySegmentLengthRule(86400),
            LenientFreightInvoiceEntryRequiredLaneRule("lenient freight invoice entry needs an entry"),
            LenientFreightInvoiceEntryVisibleGuardRule(6.4),
            LenientFreightInvoiceEntryCodePrefixRule("REF"),
            LenientFreightInvoiceEntryRequiredCodeRule("lenient freight invoice entry needs an entry"),
            LenientFreightInvoiceEntryNameLengthRule(64),
        ),
    )
}
