package com.acme.transport.fleetops.utilization.budget

/** A rule a rolling utilization budget must pass. Check returns its problems, or none. */
interface RollingUtilizationBudgetRule {
    fun check(item: RollingUtilizationBudget): MutableList<String>
}

data class RollingUtilizationBudgetFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingUtilizationBudgetRule {
    override fun check(item: RollingUtilizationBudget): MutableList<String> {
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

data class RollingUtilizationBudgetRequiredCodeRule(var message: String) : RollingUtilizationBudgetRule {
    override fun check(item: RollingUtilizationBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationBudgetTitleLengthRule(var maxLength: Int) : RollingUtilizationBudgetRule {
    override fun check(item: RollingUtilizationBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling utilization budget. */
data class RollingUtilizationBudgetValidator(var rules: MutableList<RollingUtilizationBudgetRule>) {
    fun validate(item: RollingUtilizationBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingUtilizationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingUtilizationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingUtilizationBudgetValidator(): RollingUtilizationBudgetValidator {
    return RollingUtilizationBudgetValidator(
        mutableListOf<RollingUtilizationBudgetRule>(
            RollingUtilizationBudgetFuelLitersRangeRule(8.9, 597.7),
            RollingUtilizationBudgetRequiredCodeRule("rolling utilization budget needs an entry"),
            RollingUtilizationBudgetTitleLengthRule(32),
        ),
    )
}
