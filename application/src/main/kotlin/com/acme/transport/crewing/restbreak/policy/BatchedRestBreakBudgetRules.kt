package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize

/** A rule a batched rest break budget must pass. Check returns its problems, or none. */
interface BatchedRestBreakBudgetRule {
    fun check(item: BatchedRestBreakBudget): MutableList<String>
}

data class BatchedRestBreakBudgetAllowedPhoneRule(
    var allowed: MutableList<String>,
) : BatchedRestBreakBudgetRule {
    override fun check(item: BatchedRestBreakBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakBudgetRequiredPhoneRule(var message: String) : BatchedRestBreakBudgetRule {
    override fun check(item: BatchedRestBreakBudget): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakBudgetRequiredNameRule(var message: String) : BatchedRestBreakBudgetRule {
    override fun check(item: BatchedRestBreakBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched rest break budget. */
data class BatchedRestBreakBudgetValidator(var rules: MutableList<BatchedRestBreakBudgetRule>) {
    fun validate(item: BatchedRestBreakBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRestBreakBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRestBreakBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRestBreakBudgetValidator(): BatchedRestBreakBudgetValidator {
    return BatchedRestBreakBudgetValidator(
        mutableListOf<BatchedRestBreakBudgetRule>(
            BatchedRestBreakBudgetAllowedPhoneRule(mutableListOf<String>()),
            BatchedRestBreakBudgetRequiredPhoneRule("batched rest break budget needs a value"),
            BatchedRestBreakBudgetRequiredNameRule("batched rest break budget needs a value"),
        ),
    )
}
