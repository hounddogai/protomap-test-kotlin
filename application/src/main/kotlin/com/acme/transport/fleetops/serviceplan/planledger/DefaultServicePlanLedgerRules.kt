package com.acme.transport.fleetops.serviceplan.planledger

/** A rule a default service plan ledger must pass. Check returns its problems, or none. */
interface DefaultServicePlanLedgerRule {
    fun check(item: DefaultServicePlanLedger): MutableList<String>
}

data class DefaultServicePlanLedgerRequiredContactEmailRule(
    var message: String,
) : DefaultServicePlanLedgerRule {
    override fun check(item: DefaultServicePlanLedger): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanLedgerRequiredCodeRule(var message: String) : DefaultServicePlanLedgerRule {
    override fun check(item: DefaultServicePlanLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanLedgerSequenceRangeRule(
    var min: Int,
    var max: Int,
) : DefaultServicePlanLedgerRule {
    override fun check(item: DefaultServicePlanLedger): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanLedgerRequiredRegionRule(var message: String) : DefaultServicePlanLedgerRule {
    override fun check(item: DefaultServicePlanLedger): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanLedgerCodeLengthRule(var maxLength: Int) : DefaultServicePlanLedgerRule {
    override fun check(item: DefaultServicePlanLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default service plan ledger. */
data class DefaultServicePlanLedgerValidator(var rules: MutableList<DefaultServicePlanLedgerRule>) {
    fun validate(item: DefaultServicePlanLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultServicePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultServicePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultServicePlanLedgerValidator(): DefaultServicePlanLedgerValidator {
    return DefaultServicePlanLedgerValidator(
        mutableListOf<DefaultServicePlanLedgerRule>(
            DefaultServicePlanLedgerRequiredContactEmailRule("default service plan ledger needs this field"),
            DefaultServicePlanLedgerRequiredCodeRule("default service plan ledger needs an entry"),
            DefaultServicePlanLedgerSequenceRangeRule(86400, 86400),
            DefaultServicePlanLedgerRequiredRegionRule("default service plan ledger needs an entry"),
            DefaultServicePlanLedgerCodeLengthRule(1000),
        ),
    )
}
