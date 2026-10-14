package com.acme.transport.customs.hold.variance

/** A rule a batched hold variance must pass. Check returns its problems, or none. */
interface BatchedHoldVarianceRule {
    fun check(item: BatchedHoldVariance): MutableList<String>
}

data class BatchedHoldVarianceCodeLengthRule(var maxLength: Int) : BatchedHoldVarianceRule {
    override fun check(item: BatchedHoldVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedHoldVarianceCostRangeRule(var min: Double, var max: Double) : BatchedHoldVarianceRule {
    override fun check(item: BatchedHoldVariance): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class BatchedHoldVarianceRequiredCodeRule(var message: String) : BatchedHoldVarianceRule {
    override fun check(item: BatchedHoldVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched hold variance. */
data class BatchedHoldVarianceValidator(var rules: MutableList<BatchedHoldVarianceRule>) {
    fun validate(item: BatchedHoldVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedHoldVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedHoldVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedHoldVarianceValidator(): BatchedHoldVarianceValidator {
    return BatchedHoldVarianceValidator(
        mutableListOf<BatchedHoldVarianceRule>(
            BatchedHoldVarianceCodeLengthRule(3600),
            BatchedHoldVarianceCostRangeRule(3.4, 827.9),
            BatchedHoldVarianceRequiredCodeRule("batched hold variance needs an entry"),
        ),
    )
}
