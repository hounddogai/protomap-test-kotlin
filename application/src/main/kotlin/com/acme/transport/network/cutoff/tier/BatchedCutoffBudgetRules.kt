package com.acme.transport.network.cutoff.tier

/** A rule a batched cutoff budget must pass. Check returns its problems, or none. */
interface BatchedCutoffBudgetRule {
    fun check(item: BatchedCutoffBudget): MutableList<String>
}

data class BatchedCutoffBudgetRequiredContactEmailRule(var message: String) : BatchedCutoffBudgetRule {
    override fun check(item: BatchedCutoffBudget): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffBudgetLocaleLengthRule(var maxLength: Int) : BatchedCutoffBudgetRule {
    override fun check(item: BatchedCutoffBudget): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffBudgetRequiredCodeRule(var message: String) : BatchedCutoffBudgetRule {
    override fun check(item: BatchedCutoffBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cutoff budget. */
data class BatchedCutoffBudgetValidator(var rules: MutableList<BatchedCutoffBudgetRule>) {
    fun validate(item: BatchedCutoffBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCutoffBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCutoffBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCutoffBudgetValidator(): BatchedCutoffBudgetValidator {
    return BatchedCutoffBudgetValidator(
        mutableListOf<BatchedCutoffBudgetRule>(
            BatchedCutoffBudgetRequiredContactEmailRule("batched cutoff budget needs a value"),
            BatchedCutoffBudgetLocaleLengthRule(1000),
            BatchedCutoffBudgetRequiredCodeRule("batched cutoff budget needs an entry"),
        ),
    )
}
