package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.normalize

/** A rule a roster ledger must pass. Check returns its problems, or none. */
interface RosterLedgerRule {
    fun check(item: RosterLedger): MutableList<String>
}

data class RosterLedgerAllowedReviewerRule(var allowed: MutableList<String>) : RosterLedgerRule {
    override fun check(item: RosterLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RosterLedgerLabelLengthRule(var maxLength: Int) : RosterLedgerRule {
    override fun check(item: RosterLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterLedgerMinimumRangeRule(var min: Double, var max: Double) : RosterLedgerRule {
    override fun check(item: RosterLedger): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a roster ledger. */
data class RosterLedgerValidator(var rules: MutableList<RosterLedgerRule>) {
    fun validate(item: RosterLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterLedgerValidator(): RosterLedgerValidator {
    return RosterLedgerValidator(
        mutableListOf<RosterLedgerRule>(
            RosterLedgerAllowedReviewerRule(mutableListOf<String>("lenient")),
            RosterLedgerLabelLengthRule(86400),
            RosterLedgerMinimumRangeRule(8.0, 2360.0),
        ),
    )
}
