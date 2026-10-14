package com.acme.transport.network.servicearea.recorditem

/** A rule a batched service area budget must pass. Check returns its problems, or none. */
interface BatchedServiceAreaBudgetRule {
    fun check(item: BatchedServiceAreaBudget): MutableList<String>
}

data class BatchedServiceAreaBudgetRequiredContactEmailRule(
    var message: String,
) : BatchedServiceAreaBudgetRule {
    override fun check(item: BatchedServiceAreaBudget): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaBudgetOdometerRangeRule(
    var min: Int,
    var max: Int,
) : BatchedServiceAreaBudgetRule {
    override fun check(item: BatchedServiceAreaBudget): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class BatchedServiceAreaBudgetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedServiceAreaBudgetRule {
    override fun check(item: BatchedServiceAreaBudget): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaBudgetRequiredOwnerIdRule(var message: String) : BatchedServiceAreaBudgetRule {
    override fun check(item: BatchedServiceAreaBudget): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service area budget. */
data class BatchedServiceAreaBudgetValidator(var rules: MutableList<BatchedServiceAreaBudgetRule>) {
    fun validate(item: BatchedServiceAreaBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServiceAreaBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServiceAreaBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServiceAreaBudgetValidator(): BatchedServiceAreaBudgetValidator {
    return BatchedServiceAreaBudgetValidator(
        mutableListOf<BatchedServiceAreaBudgetRule>(
            BatchedServiceAreaBudgetRequiredContactEmailRule("batched service area budget needs a value"),
            BatchedServiceAreaBudgetOdometerRangeRule(64, 3600),
            BatchedServiceAreaBudgetThresholdRangeRule(5.7, 3181.4),
            BatchedServiceAreaBudgetRequiredOwnerIdRule("batched service area budget needs this field"),
        ),
    )
}
