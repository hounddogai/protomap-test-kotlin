package com.acme.transport.safety.incident.incidentreservation

/** A rule a batched incident plan must pass. Check returns its problems, or none. */
interface BatchedIncidentPlanRule {
    fun check(item: BatchedIncidentPlan): MutableList<String>
}

data class BatchedIncidentPlanRouteCodeLengthRule(var maxLength: Int) : BatchedIncidentPlanRule {
    override fun check(item: BatchedIncidentPlan): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentPlanDepotLengthRule(var maxLength: Int) : BatchedIncidentPlanRule {
    override fun check(item: BatchedIncidentPlan): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentPlanWindowDaysRangeRule(var min: Int, var max: Int) : BatchedIncidentPlanRule {
    override fun check(item: BatchedIncidentPlan): MutableList<String> {
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

data class BatchedIncidentPlanRequiredRouteCodeRule(var message: String) : BatchedIncidentPlanRule {
    override fun check(item: BatchedIncidentPlan): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentPlanRequiredCodeRule(var message: String) : BatchedIncidentPlanRule {
    override fun check(item: BatchedIncidentPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentPlanBudgetRangeRule(var min: Double, var max: Double) : BatchedIncidentPlanRule {
    override fun check(item: BatchedIncidentPlan): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched incident plan. */
data class BatchedIncidentPlanValidator(var rules: MutableList<BatchedIncidentPlanRule>) {
    fun validate(item: BatchedIncidentPlan): MutableList<String> {
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

    fun requireValid(item: BatchedIncidentPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedIncidentPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedIncidentPlanValidator(): BatchedIncidentPlanValidator {
    return BatchedIncidentPlanValidator(
        mutableListOf<BatchedIncidentPlanRule>(
            BatchedIncidentPlanRouteCodeLengthRule(3600),
            BatchedIncidentPlanDepotLengthRule(3600),
            BatchedIncidentPlanWindowDaysRangeRule(255, 86400),
            BatchedIncidentPlanRequiredRouteCodeRule("batched incident plan needs an entry"),
            BatchedIncidentPlanRequiredCodeRule("batched incident plan needs a value"),
            BatchedIncidentPlanBudgetRangeRule(7.2, 1476.0),
        ),
    )
}
