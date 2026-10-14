package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.text.normalize

/** A rule a drop ledger must pass. Check returns its problems, or none. */
interface DropLedgerRule {
    fun check(item: DropLedger): MutableList<String>
}

data class DropLedgerPalletsRangeRule(var min: Int, var max: Int) : DropLedgerRule {
    override fun check(item: DropLedger): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropLedgerAllowedSegmentRule(var allowed: MutableList<String>) : DropLedgerRule {
    override fun check(item: DropLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DropLedgerSequenceRangeRule(var min: Int, var max: Int) : DropLedgerRule {
    override fun check(item: DropLedger): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class DropLedgerRequiredNameRule(var message: String) : DropLedgerRule {
    override fun check(item: DropLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop ledger. */
data class DropLedgerValidator(var rules: MutableList<DropLedgerRule>) {
    fun validate(item: DropLedger): MutableList<String> {
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

    fun requireValid(item: DropLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDropLedgerValidator(): DropLedgerValidator {
    return DropLedgerValidator(
        mutableListOf<DropLedgerRule>(
            DropLedgerPalletsRangeRule(80, 120),
            DropLedgerAllowedSegmentRule(mutableListOf<String>()),
            DropLedgerSequenceRangeRule(32, 1000),
            DropLedgerRequiredNameRule("drop ledger needs an entry"),
        ),
    )
}
