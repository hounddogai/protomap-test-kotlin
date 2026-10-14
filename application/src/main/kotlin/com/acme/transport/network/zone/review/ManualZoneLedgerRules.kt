package com.acme.transport.network.zone.review

import com.acme.transport.kit.text.normalize

/** A rule a manual zone ledger must pass. Check returns its problems, or none. */
interface ManualZoneLedgerRule {
    fun check(item: ManualZoneLedger): MutableList<String>
}

data class ManualZoneLedgerQuantityRangeRule(var min: Int, var max: Int) : ManualZoneLedgerRule {
    override fun check(item: ManualZoneLedger): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ManualZoneLedgerRequiredLabelRule(var message: String) : ManualZoneLedgerRule {
    override fun check(item: ManualZoneLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneLedgerCreatedAtRangeRule(var min: Int, var max: Int) : ManualZoneLedgerRule {
    override fun check(item: ManualZoneLedger): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class ManualZoneLedgerStartsAtRangeRule(var min: Int, var max: Int) : ManualZoneLedgerRule {
    override fun check(item: ManualZoneLedger): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ManualZoneLedgerAllowedCodeRule(var allowed: MutableList<String>) : ManualZoneLedgerRule {
    override fun check(item: ManualZoneLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual zone ledger. */
data class ManualZoneLedgerValidator(var rules: MutableList<ManualZoneLedgerRule>) {
    fun validate(item: ManualZoneLedger): MutableList<String> {
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

    fun requireValid(item: ManualZoneLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualZoneLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultManualZoneLedgerValidator(): ManualZoneLedgerValidator {
    return ManualZoneLedgerValidator(
        mutableListOf<ManualZoneLedgerRule>(
            ManualZoneLedgerQuantityRangeRule(16, 255),
            ManualZoneLedgerRequiredLabelRule("manual zone ledger needs an entry"),
            ManualZoneLedgerCreatedAtRangeRule(255, 1000),
            ManualZoneLedgerStartsAtRangeRule(16, 32),
            ManualZoneLedgerAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
