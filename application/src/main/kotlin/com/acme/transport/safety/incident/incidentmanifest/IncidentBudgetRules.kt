package com.acme.transport.safety.incident.incidentmanifest

/** A rule a incident budget must pass. Check returns its problems, or none. */
interface IncidentBudgetRule {
    fun check(item: IncidentBudget): MutableList<String>
}

data class IncidentBudgetActiveGuardRule(var limit: Int) : IncidentBudgetRule {
    override fun check(item: IncidentBudget): MutableList<String> {
        if (item.active && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class IncidentBudgetThresholdRangeRule(var min: Double, var max: Double) : IncidentBudgetRule {
    override fun check(item: IncidentBudget): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class IncidentBudgetCapacityRangeRule(var min: Int, var max: Int) : IncidentBudgetRule {
    override fun check(item: IncidentBudget): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a incident budget. */
data class IncidentBudgetValidator(var rules: MutableList<IncidentBudgetRule>) {
    fun validate(item: IncidentBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentBudgetValidator(): IncidentBudgetValidator {
    return IncidentBudgetValidator(
        mutableListOf<IncidentBudgetRule>(
            IncidentBudgetActiveGuardRule(32),
            IncidentBudgetThresholdRangeRule(9.5, 1316.7),
            IncidentBudgetCapacityRangeRule(16, 32),
        ),
    )
}
