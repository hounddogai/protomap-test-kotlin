package com.acme.transport.network.hub.draft

/** A rule a global hub ledger must pass. Check returns its problems, or none. */
interface GlobalHubLedgerRule {
    fun check(item: GlobalHubLedger): MutableList<String>
}

data class GlobalHubLedgerRequiredCountryRule(var message: String) : GlobalHubLedgerRule {
    override fun check(item: GlobalHubLedger): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubLedgerCreatedAtRangeRule(var min: Int, var max: Int) : GlobalHubLedgerRule {
    override fun check(item: GlobalHubLedger): MutableList<String> {
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

data class GlobalHubLedgerRequiredCodeRule(var message: String) : GlobalHubLedgerRule {
    override fun check(item: GlobalHubLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubLedgerQuantityRangeRule(var min: Int, var max: Int) : GlobalHubLedgerRule {
    override fun check(item: GlobalHubLedger): MutableList<String> {
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

/** Runs every rule of a global hub ledger. */
data class GlobalHubLedgerValidator(var rules: MutableList<GlobalHubLedgerRule>) {
    fun validate(item: GlobalHubLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalHubLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalHubLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalHubLedgerValidator(): GlobalHubLedgerValidator {
    return GlobalHubLedgerValidator(
        mutableListOf<GlobalHubLedgerRule>(
            GlobalHubLedgerRequiredCountryRule("global hub ledger needs this field"),
            GlobalHubLedgerCreatedAtRangeRule(16, 32),
            GlobalHubLedgerRequiredCodeRule("global hub ledger needs an entry"),
            GlobalHubLedgerQuantityRangeRule(16, 255),
        ),
    )
}
