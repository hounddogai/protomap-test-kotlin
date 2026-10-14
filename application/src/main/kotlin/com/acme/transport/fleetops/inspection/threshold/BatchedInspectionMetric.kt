package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.normalize

/** Where a batched inspection metric is in its life cycle. */
enum class BatchedInspectionMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched inspection metric as the inspection team keeps it. */
data class BatchedInspectionMetric(
    var code: String,
    var title: String,
    var summary: String,
    var lane: String,
    var margin: Double,
    var status: BatchedInspectionMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedInspectionMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedInspectionMetricStatus): Boolean {
        when (status) {
            BatchedInspectionMetricStatus.REQUESTED -> return next == BatchedInspectionMetricStatus.GRANTED || next == BatchedInspectionMetricStatus.REVOKED
            BatchedInspectionMetricStatus.GRANTED -> {
                when (next) {
                    BatchedInspectionMetricStatus.REVOKED, BatchedInspectionMetricStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedInspectionMetricStatus.REVOKED -> {
                when (next) {
                    BatchedInspectionMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedInspectionMetricStatus) {
        if (!canMoveTo(next)) {
            error("batched inspection metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched inspection metric"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): BatchedInspectionMetric {
        return BatchedInspectionMetric(
            code = code,
            title = title,
            summary = summary,
            lane = lane,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched inspection metric in status requested. */
fun newBatchedInspectionMetric(code: String, title: String): BatchedInspectionMetric {
    return BatchedInspectionMetric(
        code = code,
        title = title,
        summary = "",
        lane = "",
        margin = 0.0,
        status = BatchedInspectionMetricStatus.REQUESTED,
    )
}
