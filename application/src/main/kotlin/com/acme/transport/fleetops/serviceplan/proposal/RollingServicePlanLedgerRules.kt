package com.acme.transport.fleetops.serviceplan.proposal

/** A rule a rolling service plan ledger must pass. Check returns its problems, or none. */
interface RollingServicePlanLedgerRule {
    fun check(item: RollingServicePlanLedger): MutableList<String>
}

data class RollingServicePlanLedgerRetriesRangeRule(var min: Int, var max: Int) : RollingServicePlanLedgerRule {
    override fun check(item: RollingServicePlanLedger): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingServicePlanLedgerNameLengthRule(var maxLength: Int) : RollingServicePlanLedgerRule {
    override fun check(item: RollingServicePlanLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingServicePlanLedgerRequiredCodeRule(var message: String) : RollingServicePlanLedgerRule {
    override fun check(item: RollingServicePlanLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingServicePlanLedgerCodeLengthRule(var maxLength: Int) : RollingServicePlanLedgerRule {
    override fun check(item: RollingServicePlanLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling service plan ledger. */
data class RollingServicePlanLedgerValidator(var rules: MutableList<RollingServicePlanLedgerRule>) {
    fun validate(item: RollingServicePlanLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingServicePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingServicePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingServicePlanLedgerValidator(): RollingServicePlanLedgerValidator {
    return RollingServicePlanLedgerValidator(
        mutableListOf<RollingServicePlanLedgerRule>(
            RollingServicePlanLedgerRetriesRangeRule(64, 255),
            RollingServicePlanLedgerNameLengthRule(86400),
            RollingServicePlanLedgerRequiredCodeRule("rolling service plan ledger needs an entry"),
            RollingServicePlanLedgerCodeLengthRule(1000),
        ),
    )
}
