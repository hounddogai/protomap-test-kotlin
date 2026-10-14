package com.acme.transport.emissions.emissionfactor.request

/** A rule a batched emission factor batch must pass. Check returns its problems, or none. */
interface BatchedEmissionFactorBatchRule {
    fun check(item: BatchedEmissionFactorBatch): MutableList<String>
}

data class BatchedEmissionFactorBatchNameLengthRule(var maxLength: Int) : BatchedEmissionFactorBatchRule {
    override fun check(item: BatchedEmissionFactorBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorBatchCodePrefixRule(var prefix: String) : BatchedEmissionFactorBatchRule {
    override fun check(item: BatchedEmissionFactorBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorBatchThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedEmissionFactorBatchRule {
    override fun check(item: BatchedEmissionFactorBatch): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorBatchRequiredSummaryRule(var message: String) : BatchedEmissionFactorBatchRule {
    override fun check(item: BatchedEmissionFactorBatch): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorBatchSummaryLengthRule(var maxLength: Int) : BatchedEmissionFactorBatchRule {
    override fun check(item: BatchedEmissionFactorBatch): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorBatchRequiredCodeRule(var message: String) : BatchedEmissionFactorBatchRule {
    override fun check(item: BatchedEmissionFactorBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched emission factor batch. */
data class BatchedEmissionFactorBatchValidator(var rules: MutableList<BatchedEmissionFactorBatchRule>) {
    fun validate(item: BatchedEmissionFactorBatch): MutableList<String> {
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

    fun requireValid(item: BatchedEmissionFactorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedEmissionFactorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedEmissionFactorBatchValidator(): BatchedEmissionFactorBatchValidator {
    return BatchedEmissionFactorBatchValidator(
        mutableListOf<BatchedEmissionFactorBatchRule>(
            BatchedEmissionFactorBatchNameLengthRule(80),
            BatchedEmissionFactorBatchCodePrefixRule("Q"),
            BatchedEmissionFactorBatchThresholdRangeRule(9.8, 2460.6),
            BatchedEmissionFactorBatchRequiredSummaryRule("batched emission factor batch needs an entry"),
            BatchedEmissionFactorBatchSummaryLengthRule(32),
            BatchedEmissionFactorBatchRequiredCodeRule("batched emission factor batch needs a value"),
        ),
    )
}
