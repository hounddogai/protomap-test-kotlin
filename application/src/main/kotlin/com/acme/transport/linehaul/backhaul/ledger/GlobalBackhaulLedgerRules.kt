package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.normalize

/** A rule a global backhaul ledger must pass. Check returns its problems, or none. */
interface GlobalBackhaulLedgerRule {
    fun check(item: GlobalBackhaulLedger): MutableList<String>
}

data class GlobalBackhaulLedgerRatioRangeRule(var min: Double, var max: Double) : GlobalBackhaulLedgerRule {
    override fun check(item: GlobalBackhaulLedger): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class GlobalBackhaulLedgerSummaryLengthRule(var maxLength: Int) : GlobalBackhaulLedgerRule {
    override fun check(item: GlobalBackhaulLedger): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBackhaulLedgerCountryLengthRule(var maxLength: Int) : GlobalBackhaulLedgerRule {
    override fun check(item: GlobalBackhaulLedger): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBackhaulLedgerAllowedCountryRule(var allowed: MutableList<String>) : GlobalBackhaulLedgerRule {
    override fun check(item: GlobalBackhaulLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBackhaulLedgerRequiredNameRule(var message: String) : GlobalBackhaulLedgerRule {
    override fun check(item: GlobalBackhaulLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global backhaul ledger. */
data class GlobalBackhaulLedgerValidator(var rules: MutableList<GlobalBackhaulLedgerRule>) {
    fun validate(item: GlobalBackhaulLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalBackhaulLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBackhaulLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBackhaulLedgerValidator(): GlobalBackhaulLedgerValidator {
    return GlobalBackhaulLedgerValidator(
        mutableListOf<GlobalBackhaulLedgerRule>(
            GlobalBackhaulLedgerRatioRangeRule(3.2, 2750.9),
            GlobalBackhaulLedgerSummaryLengthRule(80),
            GlobalBackhaulLedgerCountryLengthRule(3600),
            GlobalBackhaulLedgerAllowedCountryRule(mutableListOf<String>("scheduled", "fallback")),
            GlobalBackhaulLedgerRequiredNameRule("global backhaul ledger needs an entry"),
        ),
    )
}
