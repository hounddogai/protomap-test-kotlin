package com.acme.transport.network.cutoff.claim

/** A rule a cutoff ledger must pass. Check returns its problems, or none. */
interface CutoffLedgerRule {
    fun check(item: CutoffLedger): MutableList<String>
}

data class CutoffLedgerPriorityRangeRule(var min: Int, var max: Int) : CutoffLedgerRule {
    override fun check(item: CutoffLedger): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class CutoffLedgerRatioRangeRule(var min: Double, var max: Double) : CutoffLedgerRule {
    override fun check(item: CutoffLedger): MutableList<String> {
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

data class CutoffLedgerRequiredNameRule(var message: String) : CutoffLedgerRule {
    override fun check(item: CutoffLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff ledger. */
data class CutoffLedgerValidator(var rules: MutableList<CutoffLedgerRule>) {
    fun validate(item: CutoffLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffLedgerValidator(): CutoffLedgerValidator {
    return CutoffLedgerValidator(
        mutableListOf<CutoffLedgerRule>(
            CutoffLedgerPriorityRangeRule(255, 255),
            CutoffLedgerRatioRangeRule(3.2, 4787.4),
            CutoffLedgerRequiredNameRule("cutoff ledger needs this field"),
        ),
    )
}
