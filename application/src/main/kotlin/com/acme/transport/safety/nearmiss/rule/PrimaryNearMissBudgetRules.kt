package com.acme.transport.safety.nearmiss.rule

/** A rule a primary near miss budget must pass. Check returns its problems, or none. */
interface PrimaryNearMissBudgetRule {
    fun check(item: PrimaryNearMissBudget): MutableList<String>
}

data class PrimaryNearMissBudgetRequiredTimezoneRule(var message: String) : PrimaryNearMissBudgetRule {
    override fun check(item: PrimaryNearMissBudget): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissBudgetDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryNearMissBudgetRule {
    override fun check(item: PrimaryNearMissBudget): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class PrimaryNearMissBudgetCapacityRangeRule(var min: Int, var max: Int) : PrimaryNearMissBudgetRule {
    override fun check(item: PrimaryNearMissBudget): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary near miss budget. */
data class PrimaryNearMissBudgetValidator(var rules: MutableList<PrimaryNearMissBudgetRule>) {
    fun validate(item: PrimaryNearMissBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryNearMissBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNearMissBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNearMissBudgetValidator(): PrimaryNearMissBudgetValidator {
    return PrimaryNearMissBudgetValidator(
        mutableListOf<PrimaryNearMissBudgetRule>(
            PrimaryNearMissBudgetRequiredTimezoneRule("primary near miss budget needs a value"),
            PrimaryNearMissBudgetDistanceKmRangeRule(4.1, 1166.0),
            PrimaryNearMissBudgetCapacityRangeRule(16, 64),
        ),
    )
}
