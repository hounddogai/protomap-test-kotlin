package com.acme.transport.crewing.restbreak.baseline

/** A rule a scheduled rest break budget must pass. Check returns its problems, or none. */
interface ScheduledRestBreakBudgetRule {
    fun check(item: ScheduledRestBreakBudget): MutableList<String>
}

data class ScheduledRestBreakBudgetCodeLengthRule(var maxLength: Int) : ScheduledRestBreakBudgetRule {
    override fun check(item: ScheduledRestBreakBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakBudgetTitleLengthRule(var maxLength: Int) : ScheduledRestBreakBudgetRule {
    override fun check(item: ScheduledRestBreakBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakBudgetFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledRestBreakBudgetRule {
    override fun check(item: ScheduledRestBreakBudget): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ScheduledRestBreakBudgetRequiredTitleRule(var message: String) : ScheduledRestBreakBudgetRule {
    override fun check(item: ScheduledRestBreakBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakBudgetApprovedGuardRule(var limit: Double) : ScheduledRestBreakBudgetRule {
    override fun check(item: ScheduledRestBreakBudget): MutableList<String> {
        if (item.approved && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakBudgetWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRestBreakBudgetRule {
    override fun check(item: ScheduledRestBreakBudget): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled rest break budget. */
data class ScheduledRestBreakBudgetValidator(var rules: MutableList<ScheduledRestBreakBudgetRule>) {
    fun validate(item: ScheduledRestBreakBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRestBreakBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRestBreakBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRestBreakBudgetValidator(): ScheduledRestBreakBudgetValidator {
    return ScheduledRestBreakBudgetValidator(
        mutableListOf<ScheduledRestBreakBudgetRule>(
            ScheduledRestBreakBudgetCodeLengthRule(120),
            ScheduledRestBreakBudgetTitleLengthRule(32),
            ScheduledRestBreakBudgetFuelLitersRangeRule(2.1, 1204.8),
            ScheduledRestBreakBudgetRequiredTitleRule("scheduled rest break budget needs this field"),
            ScheduledRestBreakBudgetApprovedGuardRule(9.7),
            ScheduledRestBreakBudgetWindowDaysRangeRule(80, 1000),
        ),
    )
}
