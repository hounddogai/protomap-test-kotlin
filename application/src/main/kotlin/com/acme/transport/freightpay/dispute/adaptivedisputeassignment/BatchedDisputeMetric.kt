package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dispute metric is in its life cycle. */
enum class BatchedDisputeMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched dispute metric as the dispute team keeps it. */
data class BatchedDisputeMetric(
    var code: String,
    var name: String,
    var active: Boolean,
    var budget: Double,
    var trailerId: String,
    var timezone: String,
    var minimum: Double,
    var ownerId: String,
    var reviewer: String,
    var status: BatchedDisputeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDisputeMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDisputeMetricStatus): Boolean {
        when (status) {
            BatchedDisputeMetricStatus.OPEN -> return next == BatchedDisputeMetricStatus.IN_REVIEW
            BatchedDisputeMetricStatus.IN_REVIEW -> {
                when (next) {
                    BatchedDisputeMetricStatus.REOPENED -> return true
                }
                return false
            }
            BatchedDisputeMetricStatus.RESOLVED -> return next == BatchedDisputeMetricStatus.ARCHIVED
            BatchedDisputeMetricStatus.REOPENED -> return next == BatchedDisputeMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedDisputeMetricStatus) {
        if (!canMoveTo(next)) {
            error("batched dispute metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round(budget * minimum * 100) / 100

    fun snapshot(): BatchedDisputeMetric {
        return BatchedDisputeMetric(
            code = code,
            name = name,
            active = active,
            budget = budget,
            trailerId = trailerId,
            timezone = timezone,
            minimum = minimum,
            ownerId = ownerId,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched dispute metric in status open. */
fun newBatchedDisputeMetric(code: String, name: String): BatchedDisputeMetric {
    return BatchedDisputeMetric(
        code = code,
        name = name,
        active = false,
        budget = 0.0,
        trailerId = "",
        timezone = "",
        minimum = 0.0,
        ownerId = "",
        reviewer = "",
        status = BatchedDisputeMetricStatus.OPEN,
    )
}
