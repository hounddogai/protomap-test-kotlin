package com.acme.transport.lastmile.locker.budget

/** A rule a scheduled locker budget must pass. Check returns its problems, or none. */
interface ScheduledLockerBudgetRule {
    fun check(item: ScheduledLockerBudget): MutableList<String>
}

data class ScheduledLockerBudgetCodePrefixRule(var prefix: String) : ScheduledLockerBudgetRule {
    override fun check(item: ScheduledLockerBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerBudgetRequiredTimezoneRule(var message: String) : ScheduledLockerBudgetRule {
    override fun check(item: ScheduledLockerBudget): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerBudgetRetriesRangeRule(var min: Int, var max: Int) : ScheduledLockerBudgetRule {
    override fun check(item: ScheduledLockerBudget): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class ScheduledLockerBudgetTimezoneLengthRule(var maxLength: Int) : ScheduledLockerBudgetRule {
    override fun check(item: ScheduledLockerBudget): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled locker budget. */
data class ScheduledLockerBudgetValidator(var rules: MutableList<ScheduledLockerBudgetRule>) {
    fun validate(item: ScheduledLockerBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLockerBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLockerBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLockerBudgetValidator(): ScheduledLockerBudgetValidator {
    return ScheduledLockerBudgetValidator(
        mutableListOf<ScheduledLockerBudgetRule>(
            ScheduledLockerBudgetCodePrefixRule("Q"),
            ScheduledLockerBudgetRequiredTimezoneRule("scheduled locker budget needs a value"),
            ScheduledLockerBudgetRetriesRangeRule(16, 3600),
            ScheduledLockerBudgetTimezoneLengthRule(16),
        ),
    )
}
