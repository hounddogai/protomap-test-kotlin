package com.acme.transport.fleetops.serviceplan.ledger

/** A rule a primary service plan ledger must pass. Check returns its problems, or none. */
interface PrimaryServicePlanLedgerRule {
    fun check(item: PrimaryServicePlanLedger): MutableList<String>
}

data class PrimaryServicePlanLedgerRequiredDescriptionRule(var message: String) : PrimaryServicePlanLedgerRule {
    override fun check(item: PrimaryServicePlanLedger): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanLedgerRegionLengthRule(var maxLength: Int) : PrimaryServicePlanLedgerRule {
    override fun check(item: PrimaryServicePlanLedger): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanLedgerCodePrefixRule(var prefix: String) : PrimaryServicePlanLedgerRule {
    override fun check(item: PrimaryServicePlanLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service plan ledger. */
data class PrimaryServicePlanLedgerValidator(var rules: MutableList<PrimaryServicePlanLedgerRule>) {
    fun validate(item: PrimaryServicePlanLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryServicePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServicePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServicePlanLedgerValidator(): PrimaryServicePlanLedgerValidator {
    return PrimaryServicePlanLedgerValidator(
        mutableListOf<PrimaryServicePlanLedgerRule>(
            PrimaryServicePlanLedgerRequiredDescriptionRule("primary service plan ledger needs an entry"),
            PrimaryServicePlanLedgerRegionLengthRule(16),
            PrimaryServicePlanLedgerCodePrefixRule("Q"),
        ),
    )
}
