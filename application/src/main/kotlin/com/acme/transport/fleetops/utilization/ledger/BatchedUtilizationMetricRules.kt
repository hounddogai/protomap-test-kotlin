package com.acme.transport.fleetops.utilization.ledger

/** A rule a batched utilization metric must pass. Check returns its problems, or none. */
interface BatchedUtilizationMetricRule {
    fun check(item: BatchedUtilizationMetric): MutableList<String>
}

data class BatchedUtilizationMetricCodeLengthRule(var maxLength: Int) : BatchedUtilizationMetricRule {
    override fun check(item: BatchedUtilizationMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationMetricCodePrefixRule(var prefix: String) : BatchedUtilizationMetricRule {
    override fun check(item: BatchedUtilizationMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationMetricUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedUtilizationMetricRule {
    override fun check(item: BatchedUtilizationMetric): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationMetricTitleLengthRule(var maxLength: Int) : BatchedUtilizationMetricRule {
    override fun check(item: BatchedUtilizationMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationMetricRequiredNameRule(var message: String) : BatchedUtilizationMetricRule {
    override fun check(item: BatchedUtilizationMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched utilization metric. */
data class BatchedUtilizationMetricValidator(var rules: MutableList<BatchedUtilizationMetricRule>) {
    fun validate(item: BatchedUtilizationMetric): MutableList<String> {
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

    fun requireValid(item: BatchedUtilizationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedUtilizationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedUtilizationMetricValidator(): BatchedUtilizationMetricValidator {
    return BatchedUtilizationMetricValidator(
        mutableListOf<BatchedUtilizationMetricRule>(
            BatchedUtilizationMetricCodeLengthRule(80),
            BatchedUtilizationMetricCodePrefixRule("REF"),
            BatchedUtilizationMetricUpdatedAtRangeRule(64, 3600),
            BatchedUtilizationMetricTitleLengthRule(64),
            BatchedUtilizationMetricRequiredNameRule("batched utilization metric needs this field"),
        ),
    )
}
