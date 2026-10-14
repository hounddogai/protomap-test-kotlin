package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.kit.text.normalize

/** A rule a scheduled freight invoice snapshot must pass. Check returns its problems, or none. */
interface ScheduledFreightInvoiceSnapshotRule {
    fun check(item: ScheduledFreightInvoiceSnapshot): MutableList<String>
}

data class ScheduledFreightInvoiceSnapshotTrailerIdLengthRule(
    var maxLength: Int,
) : ScheduledFreightInvoiceSnapshotRule {
    override fun check(item: ScheduledFreightInvoiceSnapshot): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFreightInvoiceSnapshotRequiredTitleRule(
    var message: String,
) : ScheduledFreightInvoiceSnapshotRule {
    override fun check(item: ScheduledFreightInvoiceSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFreightInvoiceSnapshotAllowedLabelRule(
    var allowed: MutableList<String>,
) : ScheduledFreightInvoiceSnapshotRule {
    override fun check(item: ScheduledFreightInvoiceSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFreightInvoiceSnapshotRequiredCodeRule(
    var message: String,
) : ScheduledFreightInvoiceSnapshotRule {
    override fun check(item: ScheduledFreightInvoiceSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFreightInvoiceSnapshotRequiredCountryRule(
    var message: String,
) : ScheduledFreightInvoiceSnapshotRule {
    override fun check(item: ScheduledFreightInvoiceSnapshot): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled freight invoice snapshot. */
data class ScheduledFreightInvoiceSnapshotValidator(
    var rules: MutableList<ScheduledFreightInvoiceSnapshotRule>,
) {
    fun validate(item: ScheduledFreightInvoiceSnapshot): MutableList<String> {
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

    fun requireValid(item: ScheduledFreightInvoiceSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFreightInvoiceSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFreightInvoiceSnapshotValidator(): ScheduledFreightInvoiceSnapshotValidator {
    return ScheduledFreightInvoiceSnapshotValidator(
        mutableListOf<ScheduledFreightInvoiceSnapshotRule>(
            ScheduledFreightInvoiceSnapshotTrailerIdLengthRule(120),
            ScheduledFreightInvoiceSnapshotRequiredTitleRule(
                "scheduled freight invoice snapshot needs an entry",
            ),
            ScheduledFreightInvoiceSnapshotAllowedLabelRule(mutableListOf<String>("scheduled")),
            ScheduledFreightInvoiceSnapshotRequiredCodeRule("scheduled freight invoice snapshot needs an entry"),
            ScheduledFreightInvoiceSnapshotRequiredCountryRule(
                "scheduled freight invoice snapshot needs a value",
            ),
        ),
    )
}
