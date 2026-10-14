package com.acme.transport.emissions.idling.quota

/** A rule a global idling ledger must pass. Check returns its problems, or none. */
interface GlobalIdlingLedgerRule {
    fun check(item: GlobalIdlingLedger): MutableList<String>
}

data class GlobalIdlingLedgerQuantityRangeRule(var min: Int, var max: Int) : GlobalIdlingLedgerRule {
    override fun check(item: GlobalIdlingLedger): MutableList<String> {
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

data class GlobalIdlingLedgerRequiredCodeRule(var message: String) : GlobalIdlingLedgerRule {
    override fun check(item: GlobalIdlingLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingLedgerCodeLengthRule(var maxLength: Int) : GlobalIdlingLedgerRule {
    override fun check(item: GlobalIdlingLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global idling ledger. */
data class GlobalIdlingLedgerValidator(var rules: MutableList<GlobalIdlingLedgerRule>) {
    fun validate(item: GlobalIdlingLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalIdlingLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalIdlingLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalIdlingLedgerValidator(): GlobalIdlingLedgerValidator {
    return GlobalIdlingLedgerValidator(
        mutableListOf<GlobalIdlingLedgerRule>(
            GlobalIdlingLedgerQuantityRangeRule(64, 120),
            GlobalIdlingLedgerRequiredCodeRule("global idling ledger needs a value"),
            GlobalIdlingLedgerCodeLengthRule(64),
        ),
    )
}
