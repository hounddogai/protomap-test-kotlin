package com.acme.transport.terminal.dwell.dwellreview

/** A rule a pending dwell ledger must pass. Check returns its problems, or none. */
interface PendingDwellLedgerRule {
    fun check(item: PendingDwellLedger): MutableList<String>
}

data class PendingDwellLedgerBudgetRangeRule(var min: Double, var max: Double) : PendingDwellLedgerRule {
    override fun check(item: PendingDwellLedger): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class PendingDwellLedgerNameLengthRule(var maxLength: Int) : PendingDwellLedgerRule {
    override fun check(item: PendingDwellLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDwellLedgerRequiredCodeRule(var message: String) : PendingDwellLedgerRule {
    override fun check(item: PendingDwellLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDwellLedgerRequiredExternalRefRule(var message: String) : PendingDwellLedgerRule {
    override fun check(item: PendingDwellLedger): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dwell ledger. */
data class PendingDwellLedgerValidator(var rules: MutableList<PendingDwellLedgerRule>) {
    fun validate(item: PendingDwellLedger): MutableList<String> {
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

    fun requireValid(item: PendingDwellLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDwellLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDwellLedgerValidator(): PendingDwellLedgerValidator {
    return PendingDwellLedgerValidator(
        mutableListOf<PendingDwellLedgerRule>(
            PendingDwellLedgerBudgetRangeRule(0.8, 275.6),
            PendingDwellLedgerNameLengthRule(120),
            PendingDwellLedgerRequiredCodeRule("pending dwell ledger needs a value"),
            PendingDwellLedgerRequiredExternalRefRule("pending dwell ledger needs an entry"),
        ),
    )
}
