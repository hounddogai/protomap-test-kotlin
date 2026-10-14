package com.acme.transport.crewing.dutyperiod.perioddraft

/** A rule a pending duty period budget must pass. Check returns its problems, or none. */
interface PendingDutyPeriodBudgetRule {
    fun check(item: PendingDutyPeriodBudget): MutableList<String>
}

data class PendingDutyPeriodBudgetStopCountRangeRule(var min: Int, var max: Int) : PendingDutyPeriodBudgetRule {
    override fun check(item: PendingDutyPeriodBudget): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class PendingDutyPeriodBudgetCodePrefixRule(var prefix: String) : PendingDutyPeriodBudgetRule {
    override fun check(item: PendingDutyPeriodBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodBudgetMarginRangeRule(
    var min: Double,
    var max: Double,
) : PendingDutyPeriodBudgetRule {
    override fun check(item: PendingDutyPeriodBudget): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending duty period budget. */
data class PendingDutyPeriodBudgetValidator(var rules: MutableList<PendingDutyPeriodBudgetRule>) {
    fun validate(item: PendingDutyPeriodBudget): MutableList<String> {
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

    fun requireValid(item: PendingDutyPeriodBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodBudgetValidator(): PendingDutyPeriodBudgetValidator {
    return PendingDutyPeriodBudgetValidator(
        mutableListOf<PendingDutyPeriodBudgetRule>(
            PendingDutyPeriodBudgetStopCountRangeRule(32, 80),
            PendingDutyPeriodBudgetCodePrefixRule("X"),
            PendingDutyPeriodBudgetMarginRangeRule(9.4, 4881.0),
        ),
    )
}
