package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

/** A rule a batched accessorial metric must pass. Check returns its problems, or none. */
interface BatchedAccessorialMetricRule {
    fun check(item: BatchedAccessorialMetric): MutableList<String>
}

data class BatchedAccessorialMetricQuantityRangeRule(
    var min: Int,
    var max: Int,
) : BatchedAccessorialMetricRule {
    override fun check(item: BatchedAccessorialMetric): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialMetricRequiredCodeRule(var message: String) : BatchedAccessorialMetricRule {
    override fun check(item: BatchedAccessorialMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialMetricRequiredLabelRule(var message: String) : BatchedAccessorialMetricRule {
    override fun check(item: BatchedAccessorialMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialMetricDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAccessorialMetricRule {
    override fun check(item: BatchedAccessorialMetric): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched accessorial metric. */
data class BatchedAccessorialMetricValidator(var rules: MutableList<BatchedAccessorialMetricRule>) {
    fun validate(item: BatchedAccessorialMetric): MutableList<String> {
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

    fun requireValid(item: BatchedAccessorialMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAccessorialMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAccessorialMetricValidator(): BatchedAccessorialMetricValidator {
    return BatchedAccessorialMetricValidator(
        mutableListOf<BatchedAccessorialMetricRule>(
            BatchedAccessorialMetricQuantityRangeRule(16, 16),
            BatchedAccessorialMetricRequiredCodeRule("batched accessorial metric needs a value"),
            BatchedAccessorialMetricRequiredLabelRule("batched accessorial metric needs a value"),
            BatchedAccessorialMetricDistanceKmRangeRule(8.6, 4105.1),
        ),
    )
}
