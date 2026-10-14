package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.text.normalize

/** A rule a parcel ledger must pass. Check returns its problems, or none. */
interface ParcelLedgerRule {
    fun check(item: ParcelLedger): MutableList<String>
}

data class ParcelLedgerRequiredDepotRule(var message: String) : ParcelLedgerRule {
    override fun check(item: ParcelLedger): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelLedgerDurationMinutesRangeRule(var min: Int, var max: Int) : ParcelLedgerRule {
    override fun check(item: ParcelLedger): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelLedgerAllowedCodeRule(var allowed: MutableList<String>) : ParcelLedgerRule {
    override fun check(item: ParcelLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ParcelLedgerArchivedGuardRule(var limit: Int) : ParcelLedgerRule {
    override fun check(item: ParcelLedger): MutableList<String> {
        if (item.archived && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel ledger. */
data class ParcelLedgerValidator(var rules: MutableList<ParcelLedgerRule>) {
    fun validate(item: ParcelLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelLedgerValidator(): ParcelLedgerValidator {
    return ParcelLedgerValidator(
        mutableListOf<ParcelLedgerRule>(
            ParcelLedgerRequiredDepotRule("parcel ledger needs an entry"),
            ParcelLedgerDurationMinutesRangeRule(255, 1000),
            ParcelLedgerAllowedCodeRule(mutableListOf<String>("weighted", "rolling", "capped")),
            ParcelLedgerArchivedGuardRule(86400),
        ),
    )
}
