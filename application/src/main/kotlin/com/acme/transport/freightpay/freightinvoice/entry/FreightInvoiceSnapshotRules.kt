package com.acme.transport.freightpay.freightinvoice.entry

/** A rule a freight invoice snapshot must pass. Check returns its problems, or none. */
interface FreightInvoiceSnapshotRule {
    fun check(item: FreightInvoiceSnapshot): MutableList<String>
}

data class FreightInvoiceSnapshotNameLengthRule(var maxLength: Int) : FreightInvoiceSnapshotRule {
    override fun check(item: FreightInvoiceSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceSnapshotCodePrefixRule(var prefix: String) : FreightInvoiceSnapshotRule {
    override fun check(item: FreightInvoiceSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceSnapshotSummaryLengthRule(var maxLength: Int) : FreightInvoiceSnapshotRule {
    override fun check(item: FreightInvoiceSnapshot): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceSnapshotRequiredNameRule(var message: String) : FreightInvoiceSnapshotRule {
    override fun check(item: FreightInvoiceSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice snapshot. */
data class FreightInvoiceSnapshotValidator(var rules: MutableList<FreightInvoiceSnapshotRule>) {
    fun validate(item: FreightInvoiceSnapshot): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceSnapshotValidator(): FreightInvoiceSnapshotValidator {
    return FreightInvoiceSnapshotValidator(
        mutableListOf<FreightInvoiceSnapshotRule>(
            FreightInvoiceSnapshotNameLengthRule(16),
            FreightInvoiceSnapshotCodePrefixRule("Q"),
            FreightInvoiceSnapshotSummaryLengthRule(86400),
            FreightInvoiceSnapshotRequiredNameRule("freight invoice snapshot needs this field"),
        ),
    )
}
