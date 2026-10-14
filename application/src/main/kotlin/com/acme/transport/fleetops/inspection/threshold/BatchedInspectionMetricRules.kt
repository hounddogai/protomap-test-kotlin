package com.acme.transport.fleetops.inspection.threshold

/** A rule a batched inspection metric must pass. Check returns its problems, or none. */
interface BatchedInspectionMetricRule {
    fun check(item: BatchedInspectionMetric): MutableList<String>
}

data class BatchedInspectionMetricCodePrefixRule(var prefix: String) : BatchedInspectionMetricRule {
    override fun check(item: BatchedInspectionMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionMetricTitleLengthRule(var maxLength: Int) : BatchedInspectionMetricRule {
    override fun check(item: BatchedInspectionMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionMetricSummaryLengthRule(var maxLength: Int) : BatchedInspectionMetricRule {
    override fun check(item: BatchedInspectionMetric): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionMetricRequiredTitleRule(var message: String) : BatchedInspectionMetricRule {
    override fun check(item: BatchedInspectionMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched inspection metric. */
data class BatchedInspectionMetricValidator(var rules: MutableList<BatchedInspectionMetricRule>) {
    fun validate(item: BatchedInspectionMetric): MutableList<String> {
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

    fun requireValid(item: BatchedInspectionMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedInspectionMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedInspectionMetricValidator(): BatchedInspectionMetricValidator {
    return BatchedInspectionMetricValidator(
        mutableListOf<BatchedInspectionMetricRule>(
            BatchedInspectionMetricCodePrefixRule("OPS"),
            BatchedInspectionMetricTitleLengthRule(255),
            BatchedInspectionMetricSummaryLengthRule(86400),
            BatchedInspectionMetricRequiredTitleRule("batched inspection metric needs an entry"),
        ),
    )
}
