package com.acme.transport.crewing.dutyperiod.periodassignment

/** A rule a manual duty period ledger must pass. Check returns its problems, or none. */
interface ManualDutyPeriodLedgerRule {
    fun check(item: ManualDutyPeriodLedger): MutableList<String>
}

data class ManualDutyPeriodLedgerCodeLengthRule(var maxLength: Int) : ManualDutyPeriodLedgerRule {
    override fun check(item: ManualDutyPeriodLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodLedgerRequiredNotesRule(var message: String) : ManualDutyPeriodLedgerRule {
    override fun check(item: ManualDutyPeriodLedger): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodLedgerRequiredLaneRule(var message: String) : ManualDutyPeriodLedgerRule {
    override fun check(item: ManualDutyPeriodLedger): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodLedgerNotesLengthRule(var maxLength: Int) : ManualDutyPeriodLedgerRule {
    override fun check(item: ManualDutyPeriodLedger): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodLedgerStartsAtRangeRule(var min: Int, var max: Int) : ManualDutyPeriodLedgerRule {
    override fun check(item: ManualDutyPeriodLedger): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual duty period ledger. */
data class ManualDutyPeriodLedgerValidator(var rules: MutableList<ManualDutyPeriodLedgerRule>) {
    fun validate(item: ManualDutyPeriodLedger): MutableList<String> {
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

    fun requireValid(item: ManualDutyPeriodLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDutyPeriodLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDutyPeriodLedgerValidator(): ManualDutyPeriodLedgerValidator {
    return ManualDutyPeriodLedgerValidator(
        mutableListOf<ManualDutyPeriodLedgerRule>(
            ManualDutyPeriodLedgerCodeLengthRule(120),
            ManualDutyPeriodLedgerRequiredNotesRule("manual duty period ledger needs a value"),
            ManualDutyPeriodLedgerRequiredLaneRule("manual duty period ledger needs a value"),
            ManualDutyPeriodLedgerNotesLengthRule(255),
            ManualDutyPeriodLedgerStartsAtRangeRule(255, 255),
        ),
    )
}
