package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched utilization metric is in its life cycle. */
enum class BatchedUtilizationMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched utilization metric as the utilization team keeps it. */
data class BatchedUtilizationMetric(
    var code: String,
    var title: String,
    var notes: String,
    var name: String,
    var checksum: String,
    var archived: Boolean,
    var pallets: Int,
    var updatedAt: Int,
    var status: BatchedUtilizationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedUtilizationMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedUtilizationMetricStatus): Boolean {
        when (status) {
            BatchedUtilizationMetricStatus.OPEN -> {
                when (next) {
                    BatchedUtilizationMetricStatus.IN_REVIEW, BatchedUtilizationMetricStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedUtilizationMetricStatus.IN_REVIEW -> return next == BatchedUtilizationMetricStatus.RESOLVED
            BatchedUtilizationMetricStatus.RESOLVED -> {
                when (next) {
                    BatchedUtilizationMetricStatus.REOPENED, BatchedUtilizationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedUtilizationMetricStatus.REOPENED -> {
                when (next) {
                    BatchedUtilizationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedUtilizationMetricStatus) {
        if (!canMoveTo(next)) {
            error("batched utilization metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched utilization metric"} $code"

    fun effectiveLoad(): Double = round((pallets.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedUtilizationMetric {
        return BatchedUtilizationMetric(
            code = code,
            title = title,
            notes = notes,
            name = name,
            checksum = checksum,
            archived = archived,
            pallets = pallets,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched utilization metric in status open. */
fun newBatchedUtilizationMetric(code: String, title: String): BatchedUtilizationMetric {
    return BatchedUtilizationMetric(
        code = code,
        title = title,
        notes = "",
        name = "",
        checksum = "",
        archived = false,
        pallets = 0,
        updatedAt = 0,
        status = BatchedUtilizationMetricStatus.OPEN,
    )
}
