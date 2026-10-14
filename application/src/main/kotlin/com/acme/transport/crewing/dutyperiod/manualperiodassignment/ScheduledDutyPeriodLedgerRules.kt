package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.kit.text.normalize

/** A rule a scheduled duty period ledger must pass. Check returns its problems, or none. */
interface ScheduledDutyPeriodLedgerRule {
    fun check(item: ScheduledDutyPeriodLedger): MutableList<String>
}

data class ScheduledDutyPeriodLedgerRequiredCodeRule(var message: String) : ScheduledDutyPeriodLedgerRule {
    override fun check(item: ScheduledDutyPeriodLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDutyPeriodLedgerCodeLengthRule(var maxLength: Int) : ScheduledDutyPeriodLedgerRule {
    override fun check(item: ScheduledDutyPeriodLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDutyPeriodLedgerScoreRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDutyPeriodLedgerRule {
    override fun check(item: ScheduledDutyPeriodLedger): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ScheduledDutyPeriodLedgerAllowedRegionRule(
    var allowed: MutableList<String>,
) : ScheduledDutyPeriodLedgerRule {
    override fun check(item: ScheduledDutyPeriodLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDutyPeriodLedgerDepotLengthRule(var maxLength: Int) : ScheduledDutyPeriodLedgerRule {
    override fun check(item: ScheduledDutyPeriodLedger): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDutyPeriodLedgerNameLengthRule(var maxLength: Int) : ScheduledDutyPeriodLedgerRule {
    override fun check(item: ScheduledDutyPeriodLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled duty period ledger. */
data class ScheduledDutyPeriodLedgerValidator(var rules: MutableList<ScheduledDutyPeriodLedgerRule>) {
    fun validate(item: ScheduledDutyPeriodLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDutyPeriodLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDutyPeriodLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDutyPeriodLedgerValidator(): ScheduledDutyPeriodLedgerValidator {
    return ScheduledDutyPeriodLedgerValidator(
        mutableListOf<ScheduledDutyPeriodLedgerRule>(
            ScheduledDutyPeriodLedgerRequiredCodeRule("scheduled duty period ledger needs an entry"),
            ScheduledDutyPeriodLedgerCodeLengthRule(3600),
            ScheduledDutyPeriodLedgerScoreRangeRule(9.0, 4781.1),
            ScheduledDutyPeriodLedgerAllowedRegionRule(mutableListOf<String>("batched", "regional", "pending")),
            ScheduledDutyPeriodLedgerDepotLengthRule(80),
            ScheduledDutyPeriodLedgerNameLengthRule(16),
        ),
    )
}
