package com.acme.transport.fleetops.utilization.utilizationledger

/** A rule a batched utilization budget must pass. Check returns its problems, or none. */
interface BatchedUtilizationBudgetRule {
    fun check(item: BatchedUtilizationBudget): MutableList<String>
}

data class BatchedUtilizationBudgetRequiredChannelRule(var message: String) : BatchedUtilizationBudgetRule {
    override fun check(item: BatchedUtilizationBudget): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationBudgetRetriesRangeRule(var min: Int, var max: Int) : BatchedUtilizationBudgetRule {
    override fun check(item: BatchedUtilizationBudget): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class BatchedUtilizationBudgetChannelLengthRule(var maxLength: Int) : BatchedUtilizationBudgetRule {
    override fun check(item: BatchedUtilizationBudget): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationBudgetRequiredCodeRule(var message: String) : BatchedUtilizationBudgetRule {
    override fun check(item: BatchedUtilizationBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationBudgetRequiredShiftCodeRule(var message: String) : BatchedUtilizationBudgetRule {
    override fun check(item: BatchedUtilizationBudget): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched utilization budget. */
data class BatchedUtilizationBudgetValidator(var rules: MutableList<BatchedUtilizationBudgetRule>) {
    fun validate(item: BatchedUtilizationBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedUtilizationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedUtilizationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedUtilizationBudgetValidator(): BatchedUtilizationBudgetValidator {
    return BatchedUtilizationBudgetValidator(
        mutableListOf<BatchedUtilizationBudgetRule>(
            BatchedUtilizationBudgetRequiredChannelRule("batched utilization budget needs an entry"),
            BatchedUtilizationBudgetRetriesRangeRule(64, 3600),
            BatchedUtilizationBudgetChannelLengthRule(86400),
            BatchedUtilizationBudgetRequiredCodeRule("batched utilization budget needs a value"),
            BatchedUtilizationBudgetRequiredShiftCodeRule("batched utilization budget needs a value"),
        ),
    )
}
