package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

/** A rule a batched dispute metric must pass. Check returns its problems, or none. */
interface BatchedDisputeMetricRule {
    fun check(item: BatchedDisputeMetric): MutableList<String>
}

data class BatchedDisputeMetricRequiredCodeRule(var message: String) : BatchedDisputeMetricRule {
    override fun check(item: BatchedDisputeMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeMetricTimezoneLengthRule(var maxLength: Int) : BatchedDisputeMetricRule {
    override fun check(item: BatchedDisputeMetric): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeMetricCodeLengthRule(var maxLength: Int) : BatchedDisputeMetricRule {
    override fun check(item: BatchedDisputeMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeMetricActiveGuardRule(var limit: Double) : BatchedDisputeMetricRule {
    override fun check(item: BatchedDisputeMetric): MutableList<String> {
        if (item.active && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeMetricTrailerIdLengthRule(var maxLength: Int) : BatchedDisputeMetricRule {
    override fun check(item: BatchedDisputeMetric): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeMetricNameLengthRule(var maxLength: Int) : BatchedDisputeMetricRule {
    override fun check(item: BatchedDisputeMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dispute metric. */
data class BatchedDisputeMetricValidator(var rules: MutableList<BatchedDisputeMetricRule>) {
    fun validate(item: BatchedDisputeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedDisputeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDisputeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDisputeMetricValidator(): BatchedDisputeMetricValidator {
    return BatchedDisputeMetricValidator(
        mutableListOf<BatchedDisputeMetricRule>(
            BatchedDisputeMetricRequiredCodeRule("batched dispute metric needs this field"),
            BatchedDisputeMetricTimezoneLengthRule(1000),
            BatchedDisputeMetricCodeLengthRule(120),
            BatchedDisputeMetricActiveGuardRule(2.5),
            BatchedDisputeMetricTrailerIdLengthRule(120),
            BatchedDisputeMetricNameLengthRule(16),
        ),
    )
}
