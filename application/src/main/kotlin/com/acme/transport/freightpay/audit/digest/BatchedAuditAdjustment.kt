package com.acme.transport.freightpay.audit.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched audit adjustment is in its life cycle. */
enum class BatchedAuditAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched audit adjustment as the audit team keeps it. */
data class BatchedAuditAdjustment(
    var code: String,
    var title: String,
    var approved: Boolean,
    var driverId: String,
    var attempts: Int,
    var region: String,
    var odometer: Int,
    var summary: String,
    var ratio: Double,
    var status: BatchedAuditAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAuditAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAuditAdjustmentStatus): Boolean {
        when (status) {
            BatchedAuditAdjustmentStatus.PLANNED -> {
                when (next) {
                    BatchedAuditAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedAuditAdjustmentStatus.SCHEDULED -> return next == BatchedAuditAdjustmentStatus.COMPLETED || next == BatchedAuditAdjustmentStatus.CANCELLED
            BatchedAuditAdjustmentStatus.IN_PROGRESS -> return next == BatchedAuditAdjustmentStatus.COMPLETED || next == BatchedAuditAdjustmentStatus.CANCELLED
            BatchedAuditAdjustmentStatus.COMPLETED -> {
                when (next) {
                    BatchedAuditAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAuditAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("batched audit adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched audit adjustment"} $code"

    fun projectedValue(): Double = round((attempts.toDouble() + ratio) * 100) / 100

    fun snapshot(): BatchedAuditAdjustment {
        return BatchedAuditAdjustment(
            code = code,
            title = title,
            approved = approved,
            driverId = driverId,
            attempts = attempts,
            region = region,
            odometer = odometer,
            summary = summary,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched audit adjustment in status planned. */
fun newBatchedAuditAdjustment(code: String, title: String): BatchedAuditAdjustment {
    return BatchedAuditAdjustment(
        code = code,
        title = title,
        approved = false,
        driverId = "",
        attempts = 0,
        region = "",
        odometer = 0,
        summary = "",
        ratio = 0.0,
        status = BatchedAuditAdjustmentStatus.PLANNED,
    )
}
