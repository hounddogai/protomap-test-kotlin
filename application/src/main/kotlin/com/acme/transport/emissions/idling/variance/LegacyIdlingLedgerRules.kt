package com.acme.transport.emissions.idling.variance

/** A rule a legacy idling ledger must pass. Check returns its problems, or none. */
interface LegacyIdlingLedgerRule {
    fun check(item: LegacyIdlingLedger): MutableList<String>
}

data class LegacyIdlingLedgerScoreRangeRule(var min: Double, var max: Double) : LegacyIdlingLedgerRule {
    override fun check(item: LegacyIdlingLedger): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class LegacyIdlingLedgerCostRangeRule(var min: Double, var max: Double) : LegacyIdlingLedgerRule {
    override fun check(item: LegacyIdlingLedger): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingLedgerRequiredCodeRule(var message: String) : LegacyIdlingLedgerRule {
    override fun check(item: LegacyIdlingLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingLedgerRequiredSummaryRule(var message: String) : LegacyIdlingLedgerRule {
    override fun check(item: LegacyIdlingLedger): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingLedgerRequiredTitleRule(var message: String) : LegacyIdlingLedgerRule {
    override fun check(item: LegacyIdlingLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy idling ledger. */
data class LegacyIdlingLedgerValidator(var rules: MutableList<LegacyIdlingLedgerRule>) {
    fun validate(item: LegacyIdlingLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyIdlingLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIdlingLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIdlingLedgerValidator(): LegacyIdlingLedgerValidator {
    return LegacyIdlingLedgerValidator(
        mutableListOf<LegacyIdlingLedgerRule>(
            LegacyIdlingLedgerScoreRangeRule(6.1, 851.7),
            LegacyIdlingLedgerCostRangeRule(3.6, 2771.2),
            LegacyIdlingLedgerRequiredCodeRule("legacy idling ledger needs an entry"),
            LegacyIdlingLedgerRequiredSummaryRule("legacy idling ledger needs a value"),
            LegacyIdlingLedgerRequiredTitleRule("legacy idling ledger needs a value"),
        ),
    )
}
