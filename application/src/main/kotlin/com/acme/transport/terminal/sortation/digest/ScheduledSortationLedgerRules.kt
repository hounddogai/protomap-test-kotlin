package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.text.normalize

/** A rule a scheduled sortation ledger must pass. Check returns its problems, or none. */
interface ScheduledSortationLedgerRule {
    fun check(item: ScheduledSortationLedger): MutableList<String>
}

data class ScheduledSortationLedgerLabelLengthRule(var maxLength: Int) : ScheduledSortationLedgerRule {
    override fun check(item: ScheduledSortationLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationLedgerRequiredLocaleRule(var message: String) : ScheduledSortationLedgerRule {
    override fun check(item: ScheduledSortationLedger): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationLedgerAllowedLabelRule(
    var allowed: MutableList<String>,
) : ScheduledSortationLedgerRule {
    override fun check(item: ScheduledSortationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationLedgerRequiredLabelRule(var message: String) : ScheduledSortationLedgerRule {
    override fun check(item: ScheduledSortationLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled sortation ledger. */
data class ScheduledSortationLedgerValidator(var rules: MutableList<ScheduledSortationLedgerRule>) {
    fun validate(item: ScheduledSortationLedger): MutableList<String> {
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

    fun requireValid(item: ScheduledSortationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSortationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSortationLedgerValidator(): ScheduledSortationLedgerValidator {
    return ScheduledSortationLedgerValidator(
        mutableListOf<ScheduledSortationLedgerRule>(
            ScheduledSortationLedgerLabelLengthRule(80),
            ScheduledSortationLedgerRequiredLocaleRule("scheduled sortation ledger needs this field"),
            ScheduledSortationLedgerAllowedLabelRule(mutableListOf<String>("tiered", "scheduled")),
            ScheduledSortationLedgerRequiredLabelRule("scheduled sortation ledger needs a value"),
        ),
    )
}
