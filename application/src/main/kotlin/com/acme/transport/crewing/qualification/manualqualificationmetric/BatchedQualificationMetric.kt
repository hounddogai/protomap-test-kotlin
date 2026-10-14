package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.text.normalize

// The batched qualification metric records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a batched qualification metric is in its life cycle. */
enum class BatchedQualificationMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched qualification metric, with what the planners need to act on it. */
data class BatchedQualificationMetric(
    var code: String,
    var label: String,
    var summary: String,
    var driverId: String,
    var archived: Boolean,
    var routeCode: String,
    var dock: String,
    var score: Double,
    var status: BatchedQualificationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationMetricStatus): Boolean {
        when (status) {
            BatchedQualificationMetricStatus.PROPOSED -> return next == BatchedQualificationMetricStatus.REJECTED || next == BatchedQualificationMetricStatus.WITHDRAWN
            BatchedQualificationMetricStatus.APPROVED -> {
                when (next) {
                    BatchedQualificationMetricStatus.REJECTED -> return true
                }
                return false
            }
            BatchedQualificationMetricStatus.REJECTED -> return next == BatchedQualificationMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedQualificationMetricStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = score

    fun snapshot(): BatchedQualificationMetric {
        return BatchedQualificationMetric(
            code = code,
            label = label,
            summary = summary,
            driverId = driverId,
            archived = archived,
            routeCode = routeCode,
            dock = dock,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched qualification metric in status proposed. */
fun newBatchedQualificationMetric(code: String, label: String): BatchedQualificationMetric {
    return BatchedQualificationMetric(
        code = code,
        label = label,
        summary = "",
        driverId = "",
        archived = false,
        routeCode = "",
        dock = "",
        score = 0.0,
        status = BatchedQualificationMetricStatus.PROPOSED,
    )
}
