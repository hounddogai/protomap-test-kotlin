package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.text.normalize

/** A rule a batched qualification metric must pass. Check returns its problems, or none. */
interface BatchedQualificationMetricRule {
    fun check(item: BatchedQualificationMetric): MutableList<String>
}

data class BatchedQualificationMetricRequiredSummaryRule(var message: String) : BatchedQualificationMetricRule {
    override fun check(item: BatchedQualificationMetric): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationMetricRequiredLabelRule(var message: String) : BatchedQualificationMetricRule {
    override fun check(item: BatchedQualificationMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationMetricArchivedGuardRule(var limit: Double) : BatchedQualificationMetricRule {
    override fun check(item: BatchedQualificationMetric): MutableList<String> {
        if (item.archived && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationMetricAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : BatchedQualificationMetricRule {
    override fun check(item: BatchedQualificationMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationMetricRequiredDriverIdRule(
    var message: String,
) : BatchedQualificationMetricRule {
    override fun check(item: BatchedQualificationMetric): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification metric. */
data class BatchedQualificationMetricValidator(var rules: MutableList<BatchedQualificationMetricRule>) {
    fun validate(item: BatchedQualificationMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedQualificationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationMetricValidator(): BatchedQualificationMetricValidator {
    return BatchedQualificationMetricValidator(
        mutableListOf<BatchedQualificationMetricRule>(
            BatchedQualificationMetricRequiredSummaryRule("batched qualification metric needs an entry"),
            BatchedQualificationMetricRequiredLabelRule("batched qualification metric needs an entry"),
            BatchedQualificationMetricArchivedGuardRule(4.6),
            BatchedQualificationMetricAllowedRouteCodeRule(
                mutableListOf<String>("lenient", "manual", "default"),
            ),
            BatchedQualificationMetricRequiredDriverIdRule("batched qualification metric needs an entry"),
        ),
    )
}
