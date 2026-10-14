package com.acme.transport.fleetops.serviceplan.ledger

/** A rule a fallback service plan ledger must pass. Check returns its problems, or none. */
interface FallbackServicePlanLedgerRule {
    fun check(item: FallbackServicePlanLedger): MutableList<String>
}

data class FallbackServicePlanLedgerCodePrefixRule(var prefix: String) : FallbackServicePlanLedgerRule {
    override fun check(item: FallbackServicePlanLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanLedgerRequiredTitleRule(var message: String) : FallbackServicePlanLedgerRule {
    override fun check(item: FallbackServicePlanLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanLedgerRequiredCodeRule(var message: String) : FallbackServicePlanLedgerRule {
    override fun check(item: FallbackServicePlanLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service plan ledger. */
data class FallbackServicePlanLedgerValidator(var rules: MutableList<FallbackServicePlanLedgerRule>) {
    fun validate(item: FallbackServicePlanLedger): MutableList<String> {
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

    fun requireValid(item: FallbackServicePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServicePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServicePlanLedgerValidator(): FallbackServicePlanLedgerValidator {
    return FallbackServicePlanLedgerValidator(
        mutableListOf<FallbackServicePlanLedgerRule>(
            FallbackServicePlanLedgerCodePrefixRule("ACM"),
            FallbackServicePlanLedgerRequiredTitleRule("fallback service plan ledger needs a value"),
            FallbackServicePlanLedgerRequiredCodeRule("fallback service plan ledger needs an entry"),
        ),
    )
}
