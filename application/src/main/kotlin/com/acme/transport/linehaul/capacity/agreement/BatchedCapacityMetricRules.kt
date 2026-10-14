package com.acme.transport.linehaul.capacity.agreement

/** A rule a batched capacity metric must pass. Check returns its problems, or none. */
interface BatchedCapacityMetricRule {
    fun check(item: BatchedCapacityMetric): MutableList<String>
}

data class BatchedCapacityMetricNameLengthRule(var maxLength: Int) : BatchedCapacityMetricRule {
    override fun check(item: BatchedCapacityMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityMetricTitleLengthRule(var maxLength: Int) : BatchedCapacityMetricRule {
    override fun check(item: BatchedCapacityMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityMetricVisibleGuardRule(var limit: Int) : BatchedCapacityMetricRule {
    override fun check(item: BatchedCapacityMetric): MutableList<String> {
        if (item.visible && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityMetricRequiredTitleRule(var message: String) : BatchedCapacityMetricRule {
    override fun check(item: BatchedCapacityMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityMetricRequiredNameRule(var message: String) : BatchedCapacityMetricRule {
    override fun check(item: BatchedCapacityMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityMetricCodeLengthRule(var maxLength: Int) : BatchedCapacityMetricRule {
    override fun check(item: BatchedCapacityMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity metric. */
data class BatchedCapacityMetricValidator(var rules: MutableList<BatchedCapacityMetricRule>) {
    fun validate(item: BatchedCapacityMetric): MutableList<String> {
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

    fun requireValid(item: BatchedCapacityMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacityMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacityMetricValidator(): BatchedCapacityMetricValidator {
    return BatchedCapacityMetricValidator(
        mutableListOf<BatchedCapacityMetricRule>(
            BatchedCapacityMetricNameLengthRule(80),
            BatchedCapacityMetricTitleLengthRule(86400),
            BatchedCapacityMetricVisibleGuardRule(64),
            BatchedCapacityMetricRequiredTitleRule("batched capacity metric needs a value"),
            BatchedCapacityMetricRequiredNameRule("batched capacity metric needs a value"),
            BatchedCapacityMetricCodeLengthRule(64),
        ),
    )
}
