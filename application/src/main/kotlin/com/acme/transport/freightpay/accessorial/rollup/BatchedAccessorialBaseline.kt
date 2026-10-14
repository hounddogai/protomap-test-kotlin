package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched accessorial baseline is in its life cycle. */
enum class BatchedAccessorialBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched accessorial baseline. Its code is unique within a tenant. */
data class BatchedAccessorialBaseline(
    var code: String,
    var label: String,
    var trailerId: String,
    var category: String,
    var title: String,
    var carrier: String,
    var currency: String,
    var durationMinutes: Int,
    var threshold: Double,
    var status: BatchedAccessorialBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAccessorialBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAccessorialBaselineStatus): Boolean {
        when (status) {
            BatchedAccessorialBaselineStatus.PLANNED -> return next == BatchedAccessorialBaselineStatus.SCHEDULED || next == BatchedAccessorialBaselineStatus.COMPLETED
            BatchedAccessorialBaselineStatus.SCHEDULED -> {
                when (next) {
                    BatchedAccessorialBaselineStatus.IN_PROGRESS, BatchedAccessorialBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedAccessorialBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedAccessorialBaselineStatus.COMPLETED, BatchedAccessorialBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedAccessorialBaselineStatus.COMPLETED -> return next == BatchedAccessorialBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedAccessorialBaselineStatus) {
        if (!canMoveTo(next)) {
            error("batched accessorial baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched accessorial baseline"} $code"

    fun projectedExposure(): Double = round((threshold + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): BatchedAccessorialBaseline {
        return BatchedAccessorialBaseline(
            code = code,
            label = label,
            trailerId = trailerId,
            category = category,
            title = title,
            carrier = carrier,
            currency = currency,
            durationMinutes = durationMinutes,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched accessorial baseline in status planned. */
fun newBatchedAccessorialBaseline(code: String, label: String): BatchedAccessorialBaseline {
    return BatchedAccessorialBaseline(
        code = code,
        label = label,
        trailerId = "",
        category = "",
        title = "",
        carrier = "",
        currency = "",
        durationMinutes = 0,
        threshold = 0.0,
        status = BatchedAccessorialBaselineStatus.PLANNED,
    )
}
