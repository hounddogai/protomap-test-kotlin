package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize

/** A rule a relay ledger must pass. Check returns its problems, or none. */
interface RelayLedgerRule {
    fun check(item: RelayLedger): MutableList<String>
}

data class RelayLedgerCodeLengthRule(var maxLength: Int) : RelayLedgerRule {
    override fun check(item: RelayLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayLedgerAllowedCategoryRule(var allowed: MutableList<String>) : RelayLedgerRule {
    override fun check(item: RelayLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RelayLedgerRequiredLabelRule(var message: String) : RelayLedgerRule {
    override fun check(item: RelayLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay ledger. */
data class RelayLedgerValidator(var rules: MutableList<RelayLedgerRule>) {
    fun validate(item: RelayLedger): MutableList<String> {
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

    fun requireValid(item: RelayLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayLedgerValidator(): RelayLedgerValidator {
    return RelayLedgerValidator(
        mutableListOf<RelayLedgerRule>(
            RelayLedgerCodeLengthRule(32),
            RelayLedgerAllowedCategoryRule(mutableListOf<String>("tiered", "legacy", "tiered")),
            RelayLedgerRequiredLabelRule("relay ledger needs a value"),
        ),
    )
}
