package com.acme.transport.crewing.dutyperiod.ledger

/** A rule a duty period ledger must pass. Check returns its problems, or none. */
interface DutyPeriodLedgerRule {
    fun check(item: DutyPeriodLedger): MutableList<String>
}

data class DutyPeriodLedgerRequiredCountryRule(var message: String) : DutyPeriodLedgerRule {
    override fun check(item: DutyPeriodLedger): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodLedgerRequiredVehicleIdRule(var message: String) : DutyPeriodLedgerRule {
    override fun check(item: DutyPeriodLedger): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodLedgerRequiredNameRule(var message: String) : DutyPeriodLedgerRule {
    override fun check(item: DutyPeriodLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodLedgerNameLengthRule(var maxLength: Int) : DutyPeriodLedgerRule {
    override fun check(item: DutyPeriodLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodLedgerCodeLengthRule(var maxLength: Int) : DutyPeriodLedgerRule {
    override fun check(item: DutyPeriodLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period ledger. */
data class DutyPeriodLedgerValidator(var rules: MutableList<DutyPeriodLedgerRule>) {
    fun validate(item: DutyPeriodLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodLedgerValidator(): DutyPeriodLedgerValidator {
    return DutyPeriodLedgerValidator(
        mutableListOf<DutyPeriodLedgerRule>(
            DutyPeriodLedgerRequiredCountryRule("duty period ledger needs a value"),
            DutyPeriodLedgerRequiredVehicleIdRule("duty period ledger needs this field"),
            DutyPeriodLedgerRequiredNameRule("duty period ledger needs this field"),
            DutyPeriodLedgerNameLengthRule(64),
            DutyPeriodLedgerCodeLengthRule(16),
        ),
    )
}
