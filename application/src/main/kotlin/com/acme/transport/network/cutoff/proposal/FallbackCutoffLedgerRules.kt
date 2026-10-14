package com.acme.transport.network.cutoff.proposal

/** A rule a fallback cutoff ledger must pass. Check returns its problems, or none. */
interface FallbackCutoffLedgerRule {
    fun check(item: FallbackCutoffLedger): MutableList<String>
}

data class FallbackCutoffLedgerRequiredContactEmailRule(var message: String) : FallbackCutoffLedgerRule {
    override fun check(item: FallbackCutoffLedger): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffLedgerStopCountRangeRule(var min: Int, var max: Int) : FallbackCutoffLedgerRule {
    override fun check(item: FallbackCutoffLedger): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class FallbackCutoffLedgerEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackCutoffLedgerRule {
    override fun check(item: FallbackCutoffLedger): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class FallbackCutoffLedgerRateRangeRule(var min: Double, var max: Double) : FallbackCutoffLedgerRule {
    override fun check(item: FallbackCutoffLedger): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class FallbackCutoffLedgerRequiredTitleRule(var message: String) : FallbackCutoffLedgerRule {
    override fun check(item: FallbackCutoffLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffLedgerRequiredCodeRule(var message: String) : FallbackCutoffLedgerRule {
    override fun check(item: FallbackCutoffLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback cutoff ledger. */
data class FallbackCutoffLedgerValidator(var rules: MutableList<FallbackCutoffLedgerRule>) {
    fun validate(item: FallbackCutoffLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackCutoffLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCutoffLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCutoffLedgerValidator(): FallbackCutoffLedgerValidator {
    return FallbackCutoffLedgerValidator(
        mutableListOf<FallbackCutoffLedgerRule>(
            FallbackCutoffLedgerRequiredContactEmailRule("fallback cutoff ledger needs a value"),
            FallbackCutoffLedgerStopCountRangeRule(120, 3600),
            FallbackCutoffLedgerEtaMinutesRangeRule(255, 1000),
            FallbackCutoffLedgerRateRangeRule(8.8, 3168.8),
            FallbackCutoffLedgerRequiredTitleRule("fallback cutoff ledger needs an entry"),
            FallbackCutoffLedgerRequiredCodeRule("fallback cutoff ledger needs a value"),
        ),
    )
}
