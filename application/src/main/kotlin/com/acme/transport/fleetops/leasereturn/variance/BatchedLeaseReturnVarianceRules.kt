package com.acme.transport.fleetops.leasereturn.variance

/** A rule a batched lease return variance must pass. Check returns its problems, or none. */
interface BatchedLeaseReturnVarianceRule {
    fun check(item: BatchedLeaseReturnVariance): MutableList<String>
}

data class BatchedLeaseReturnVarianceCodeLengthRule(var maxLength: Int) : BatchedLeaseReturnVarianceRule {
    override fun check(item: BatchedLeaseReturnVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnVarianceRequiredCodeRule(var message: String) : BatchedLeaseReturnVarianceRule {
    override fun check(item: BatchedLeaseReturnVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnVarianceGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : BatchedLeaseReturnVarianceRule {
    override fun check(item: BatchedLeaseReturnVariance): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class BatchedLeaseReturnVarianceCodePrefixRule(var prefix: String) : BatchedLeaseReturnVarianceRule {
    override fun check(item: BatchedLeaseReturnVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnVarianceLabelLengthRule(var maxLength: Int) : BatchedLeaseReturnVarianceRule {
    override fun check(item: BatchedLeaseReturnVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnVarianceBudgetRangeRule(
    var min: Double,
    var max: Double,
) : BatchedLeaseReturnVarianceRule {
    override fun check(item: BatchedLeaseReturnVariance): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched lease return variance. */
data class BatchedLeaseReturnVarianceValidator(var rules: MutableList<BatchedLeaseReturnVarianceRule>) {
    fun validate(item: BatchedLeaseReturnVariance): MutableList<String> {
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

    fun requireValid(item: BatchedLeaseReturnVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLeaseReturnVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLeaseReturnVarianceValidator(): BatchedLeaseReturnVarianceValidator {
    return BatchedLeaseReturnVarianceValidator(
        mutableListOf<BatchedLeaseReturnVarianceRule>(
            BatchedLeaseReturnVarianceCodeLengthRule(120),
            BatchedLeaseReturnVarianceRequiredCodeRule("batched lease return variance needs a value"),
            BatchedLeaseReturnVarianceGraceDaysRangeRule(120, 1000),
            BatchedLeaseReturnVarianceCodePrefixRule("ACM"),
            BatchedLeaseReturnVarianceLabelLengthRule(120),
            BatchedLeaseReturnVarianceBudgetRangeRule(1.1, 3070.7),
        ),
    )
}
