package com.acme.transport.lastmile.drop.dropbundle

/** A rule a batched drop budget must pass. Check returns its problems, or none. */
interface BatchedDropBudgetRule {
    fun check(item: BatchedDropBudget): MutableList<String>
}

data class BatchedDropBudgetRequiredSummaryRule(var message: String) : BatchedDropBudgetRule {
    override fun check(item: BatchedDropBudget): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropBudgetUnitLengthRule(var maxLength: Int) : BatchedDropBudgetRule {
    override fun check(item: BatchedDropBudget): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropBudgetRequiredLabelRule(var message: String) : BatchedDropBudgetRule {
    override fun check(item: BatchedDropBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched drop budget. */
data class BatchedDropBudgetValidator(var rules: MutableList<BatchedDropBudgetRule>) {
    fun validate(item: BatchedDropBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedDropBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDropBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDropBudgetValidator(): BatchedDropBudgetValidator {
    return BatchedDropBudgetValidator(
        mutableListOf<BatchedDropBudgetRule>(
            BatchedDropBudgetRequiredSummaryRule("batched drop budget needs a value"),
            BatchedDropBudgetUnitLengthRule(3600),
            BatchedDropBudgetRequiredLabelRule("batched drop budget needs an entry"),
        ),
    )
}
