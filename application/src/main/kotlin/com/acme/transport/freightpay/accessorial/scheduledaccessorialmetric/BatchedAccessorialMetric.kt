package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched accessorial metric records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a batched accessorial metric is in its life cycle. */
enum class BatchedAccessorialMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched accessorial metric. Its code is unique within a tenant. */
data class BatchedAccessorialMetric(
    var code: String,
    var label: String,
    var quantity: Int,
    var distanceKm: Double,
    var threshold: Double,
    var status: BatchedAccessorialMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAccessorialMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAccessorialMetricStatus): Boolean {
        when (status) {
            BatchedAccessorialMetricStatus.IDLE -> return next == BatchedAccessorialMetricStatus.WARMING || next == BatchedAccessorialMetricStatus.DRAINING
            BatchedAccessorialMetricStatus.WARMING -> return next == BatchedAccessorialMetricStatus.READY || next == BatchedAccessorialMetricStatus.STOPPED
            BatchedAccessorialMetricStatus.READY -> return next == BatchedAccessorialMetricStatus.DRAINING || next == BatchedAccessorialMetricStatus.STOPPED
            BatchedAccessorialMetricStatus.DRAINING -> {
                when (next) {
                    BatchedAccessorialMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAccessorialMetricStatus) {
        if (!canMoveTo(next)) {
            error("batched accessorial metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((threshold - distanceKm) * 100) / 100

    fun snapshot(): BatchedAccessorialMetric {
        return BatchedAccessorialMetric(
            code = code,
            label = label,
            quantity = quantity,
            distanceKm = distanceKm,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched accessorial metric in status idle. */
fun newBatchedAccessorialMetric(code: String, label: String): BatchedAccessorialMetric {
    return BatchedAccessorialMetric(
        code = code,
        label = label,
        quantity = 0,
        distanceKm = 0.0,
        threshold = 0.0,
        status = BatchedAccessorialMetricStatus.IDLE,
    )
}
