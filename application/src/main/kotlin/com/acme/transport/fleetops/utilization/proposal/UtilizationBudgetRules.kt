package com.acme.transport.fleetops.utilization.proposal

/** A rule a utilization budget must pass. Check returns its problems, or none. */
interface UtilizationBudgetRule {
    fun check(item: UtilizationBudget): MutableList<String>
}

data class UtilizationBudgetRequiredUnitRule(var message: String) : UtilizationBudgetRule {
    override fun check(item: UtilizationBudget): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBudgetStopCountRangeRule(var min: Int, var max: Int) : UtilizationBudgetRule {
    override fun check(item: UtilizationBudget): MutableList<String> {
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

data class UtilizationBudgetCategoryLengthRule(var maxLength: Int) : UtilizationBudgetRule {
    override fun check(item: UtilizationBudget): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBudgetMinimumRangeRule(var min: Double, var max: Double) : UtilizationBudgetRule {
    override fun check(item: UtilizationBudget): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization budget. */
data class UtilizationBudgetValidator(var rules: MutableList<UtilizationBudgetRule>) {
    fun validate(item: UtilizationBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationBudgetValidator(): UtilizationBudgetValidator {
    return UtilizationBudgetValidator(
        mutableListOf<UtilizationBudgetRule>(
            UtilizationBudgetRequiredUnitRule("utilization budget needs an entry"),
            UtilizationBudgetStopCountRangeRule(64, 64),
            UtilizationBudgetCategoryLengthRule(86400),
            UtilizationBudgetMinimumRangeRule(1.5, 2313.5),
        ),
    )
}
