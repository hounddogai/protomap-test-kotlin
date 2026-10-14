package com.acme.transport.fleetops.serviceplan.entry

/** A rule a scheduled service plan budget must pass. Check returns its problems, or none. */
interface ScheduledServicePlanBudgetRule {
    fun check(item: ScheduledServicePlanBudget): MutableList<String>
}

data class ScheduledServicePlanBudgetStopCountRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledServicePlanBudgetRule {
    override fun check(item: ScheduledServicePlanBudget): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanBudgetNameLengthRule(var maxLength: Int) : ScheduledServicePlanBudgetRule {
    override fun check(item: ScheduledServicePlanBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanBudgetRequiredCodeRule(var message: String) : ScheduledServicePlanBudgetRule {
    override fun check(item: ScheduledServicePlanBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServicePlanBudgetCodePrefixRule(var prefix: String) : ScheduledServicePlanBudgetRule {
    override fun check(item: ScheduledServicePlanBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled service plan budget. */
data class ScheduledServicePlanBudgetValidator(var rules: MutableList<ScheduledServicePlanBudgetRule>) {
    fun validate(item: ScheduledServicePlanBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledServicePlanBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledServicePlanBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledServicePlanBudgetValidator(): ScheduledServicePlanBudgetValidator {
    return ScheduledServicePlanBudgetValidator(
        mutableListOf<ScheduledServicePlanBudgetRule>(
            ScheduledServicePlanBudgetStopCountRangeRule(64, 1000),
            ScheduledServicePlanBudgetNameLengthRule(255),
            ScheduledServicePlanBudgetRequiredCodeRule("scheduled service plan budget needs an entry"),
            ScheduledServicePlanBudgetCodePrefixRule("ACM"),
        ),
    )
}
