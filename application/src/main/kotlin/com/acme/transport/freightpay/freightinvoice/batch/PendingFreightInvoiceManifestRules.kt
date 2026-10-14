package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.kit.text.normalize

/** A rule a pending freight invoice manifest must pass. Check returns its problems, or none. */
interface PendingFreightInvoiceManifestRule {
    fun check(item: PendingFreightInvoiceManifest): MutableList<String>
}

data class PendingFreightInvoiceManifestRequiredCodeRule(
    var message: String,
) : PendingFreightInvoiceManifestRule {
    override fun check(item: PendingFreightInvoiceManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceManifestLabelLengthRule(
    var maxLength: Int,
) : PendingFreightInvoiceManifestRule {
    override fun check(item: PendingFreightInvoiceManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceManifestCodePrefixRule(var prefix: String) : PendingFreightInvoiceManifestRule {
    override fun check(item: PendingFreightInvoiceManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceManifestNotesLengthRule(
    var maxLength: Int,
) : PendingFreightInvoiceManifestRule {
    override fun check(item: PendingFreightInvoiceManifest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceManifestRequiredLabelRule(
    var message: String,
) : PendingFreightInvoiceManifestRule {
    override fun check(item: PendingFreightInvoiceManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceManifestAllowedNotesRule(
    var allowed: MutableList<String>,
) : PendingFreightInvoiceManifestRule {
    override fun check(item: PendingFreightInvoiceManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending freight invoice manifest. */
data class PendingFreightInvoiceManifestValidator(var rules: MutableList<PendingFreightInvoiceManifestRule>) {
    fun validate(item: PendingFreightInvoiceManifest): MutableList<String> {
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

    fun requireValid(item: PendingFreightInvoiceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFreightInvoiceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFreightInvoiceManifestValidator(): PendingFreightInvoiceManifestValidator {
    return PendingFreightInvoiceManifestValidator(
        mutableListOf<PendingFreightInvoiceManifestRule>(
            PendingFreightInvoiceManifestRequiredCodeRule("pending freight invoice manifest needs an entry"),
            PendingFreightInvoiceManifestLabelLengthRule(255),
            PendingFreightInvoiceManifestCodePrefixRule("REF"),
            PendingFreightInvoiceManifestNotesLengthRule(120),
            PendingFreightInvoiceManifestRequiredLabelRule("pending freight invoice manifest needs this field"),
            PendingFreightInvoiceManifestAllowedNotesRule(mutableListOf<String>("default", "regional")),
        ),
    )
}
