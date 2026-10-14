package com.acme.transport.fleetops.serviceplan.variance

/** A rule a pending service plan ledger must pass. Check returns its problems, or none. */
interface PendingServicePlanLedgerRule {
    fun check(item: PendingServicePlanLedger): MutableList<String>
}

data class PendingServicePlanLedgerRequiredDriverIdRule(var message: String) : PendingServicePlanLedgerRule {
    override fun check(item: PendingServicePlanLedger): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanLedgerCodePrefixRule(var prefix: String) : PendingServicePlanLedgerRule {
    override fun check(item: PendingServicePlanLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanLedgerPriorityRangeRule(
    var min: Int,
    var max: Int,
) : PendingServicePlanLedgerRule {
    override fun check(item: PendingServicePlanLedger): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanLedgerAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : PendingServicePlanLedgerRule {
    override fun check(item: PendingServicePlanLedger): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class PendingServicePlanLedgerLabelLengthRule(var maxLength: Int) : PendingServicePlanLedgerRule {
    override fun check(item: PendingServicePlanLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending service plan ledger. */
data class PendingServicePlanLedgerValidator(var rules: MutableList<PendingServicePlanLedgerRule>) {
    fun validate(item: PendingServicePlanLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingServicePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServicePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServicePlanLedgerValidator(): PendingServicePlanLedgerValidator {
    return PendingServicePlanLedgerValidator(
        mutableListOf<PendingServicePlanLedgerRule>(
            PendingServicePlanLedgerRequiredDriverIdRule("pending service plan ledger needs this field"),
            PendingServicePlanLedgerCodePrefixRule("X"),
            PendingServicePlanLedgerPriorityRangeRule(120, 1000),
            PendingServicePlanLedgerAttemptsRangeRule(64, 64),
            PendingServicePlanLedgerLabelLengthRule(1000),
        ),
    )
}
