package com.acme.transport.freightpay.dispute.assignment

/** A rule a batched dispute batch must pass. Check returns its problems, or none. */
interface BatchedDisputeBatchRule {
    fun check(item: BatchedDisputeBatch): MutableList<String>
}

data class BatchedDisputeBatchMaximumRangeRule(var min: Double, var max: Double) : BatchedDisputeBatchRule {
    override fun check(item: BatchedDisputeBatch): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class BatchedDisputeBatchSequenceRangeRule(var min: Int, var max: Int) : BatchedDisputeBatchRule {
    override fun check(item: BatchedDisputeBatch): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class BatchedDisputeBatchAmountRangeRule(var min: Double, var max: Double) : BatchedDisputeBatchRule {
    override fun check(item: BatchedDisputeBatch): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dispute batch. */
data class BatchedDisputeBatchValidator(var rules: MutableList<BatchedDisputeBatchRule>) {
    fun validate(item: BatchedDisputeBatch): MutableList<String> {
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

    fun requireValid(item: BatchedDisputeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDisputeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDisputeBatchValidator(): BatchedDisputeBatchValidator {
    return BatchedDisputeBatchValidator(
        mutableListOf<BatchedDisputeBatchRule>(
            BatchedDisputeBatchMaximumRangeRule(2.2, 1812.5),
            BatchedDisputeBatchSequenceRangeRule(1000, 3600),
            BatchedDisputeBatchAmountRangeRule(9.8, 399.3),
        ),
    )
}
