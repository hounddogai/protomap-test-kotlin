package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice ledger must pass. Check returns its problems, or none. */
interface FreightInvoiceLedgerRule {
    fun check(item: FreightInvoiceLedger): MutableList<String>
}

data class FreightInvoiceLedgerCodePrefixRule(var prefix: String) : FreightInvoiceLedgerRule {
    override fun check(item: FreightInvoiceLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceLedgerAllowedOwnerIdRule(var allowed: MutableList<String>) : FreightInvoiceLedgerRule {
    override fun check(item: FreightInvoiceLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceLedgerCodeLengthRule(var maxLength: Int) : FreightInvoiceLedgerRule {
    override fun check(item: FreightInvoiceLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice ledger. */
data class FreightInvoiceLedgerValidator(var rules: MutableList<FreightInvoiceLedgerRule>) {
    fun validate(item: FreightInvoiceLedger): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceLedgerValidator(): FreightInvoiceLedgerValidator {
    return FreightInvoiceLedgerValidator(
        mutableListOf<FreightInvoiceLedgerRule>(
            FreightInvoiceLedgerCodePrefixRule("REF"),
            FreightInvoiceLedgerAllowedOwnerIdRule(mutableListOf<String>()),
            FreightInvoiceLedgerCodeLengthRule(1000),
        ),
    )
}
