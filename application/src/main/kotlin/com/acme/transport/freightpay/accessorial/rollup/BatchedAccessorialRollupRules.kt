package com.acme.transport.freightpay.accessorial.rollup

/** A rule a batched accessorial rollup must pass. Check returns its problems, or none. */
interface BatchedAccessorialRollupRule {
    fun check(item: BatchedAccessorialRollup): MutableList<String>
}

data class BatchedAccessorialRollupRequiredTitleRule(var message: String) : BatchedAccessorialRollupRule {
    override fun check(item: BatchedAccessorialRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialRollupCodeLengthRule(var maxLength: Int) : BatchedAccessorialRollupRule {
    override fun check(item: BatchedAccessorialRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialRollupCostRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAccessorialRollupRule {
    override fun check(item: BatchedAccessorialRollup): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialRollupStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedAccessorialRollupRule {
    override fun check(item: BatchedAccessorialRollup): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class BatchedAccessorialRollupRequiredSummaryRule(var message: String) : BatchedAccessorialRollupRule {
    override fun check(item: BatchedAccessorialRollup): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialRollupDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAccessorialRollupRule {
    override fun check(item: BatchedAccessorialRollup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched accessorial rollup. */
data class BatchedAccessorialRollupValidator(var rules: MutableList<BatchedAccessorialRollupRule>) {
    fun validate(item: BatchedAccessorialRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAccessorialRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAccessorialRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAccessorialRollupValidator(): BatchedAccessorialRollupValidator {
    return BatchedAccessorialRollupValidator(
        mutableListOf<BatchedAccessorialRollupRule>(
            BatchedAccessorialRollupRequiredTitleRule("batched accessorial rollup needs an entry"),
            BatchedAccessorialRollupCodeLengthRule(120),
            BatchedAccessorialRollupCostRangeRule(6.0, 1840.3),
            BatchedAccessorialRollupStartsAtRangeRule(16, 120),
            BatchedAccessorialRollupRequiredSummaryRule("batched accessorial rollup needs an entry"),
            BatchedAccessorialRollupDiscountRangeRule(4.2, 3720.2),
        ),
    )
}
